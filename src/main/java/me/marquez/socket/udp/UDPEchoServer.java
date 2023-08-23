package me.marquez.socket.udp;

import lombok.*;
import me.marquez.socket.udp.entity.UDPEchoData;
import me.marquez.socket.udp.entity.UDPEchoResponse;
import me.marquez.socket.udp.entity.UDPEchoSend;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPEchoServer extends Thread{

    private static final long TIMEOUT = 30; //UDP 응답 최대 대기 시간 (초)
    private static final AtomicInteger identifier = new AtomicInteger(0);

    private final int serverPort;
    private final Logger logger;

    @Setter
    private boolean debug;

    @Getter
    private DatagramSocket serverSocket;
    private final List<UDPMessageHandler> handlers;

    private long makeId() {
        return System.currentTimeMillis()*10 + identifier.getAndIncrement();
    }

    private void info(String s, Object o) {
        if(debug) logger.info(s, o);
    }

    private void info(String s, Object... o) {
        if(debug) logger.info(s, o);
    }

    public UDPEchoServer(int serverPort, Logger logger) throws IOException {
        this.serverPort = serverPort;
        this.logger = logger;
        this.handlers = new ArrayList<>();
        init();
    }

    private void init() throws IOException {
        serverSocket = new DatagramSocket(serverPort);
    }

    public void registerHandler(UDPMessageHandler handler) {
        handlers.add(handler);
    }

    //identifier, response future
    private final Map<Long, CompletableFuture<UDPEchoResponse>> echoMap = new ConcurrentHashMap<>();
    private final Map<Long, BigData> bigDataMap = new ConcurrentHashMap<>();

    private static class BigData {
        int targetLength;
        int currentLength;
        String[] data;

        public BigData(int length) {
            this.targetLength = length;
            this.currentLength = 0;
            int size = (int)Math.ceil((double)length/65000D);
            this.data = new String[size];
            Arrays.fill(data, "");
        }

        public boolean add(int i, String data) {
            this.data[i] = data;
            currentLength += data.length();
            return currentLength == targetLength;
        }
    }

    @Override
    public void run() {
        info("Starting udp echo server listening port on {}", serverPort);
        while (!serverSocket.isClosed() && !this.isInterrupted()) {
            try {
                byte[] buffer = new byte[serverSocket.getReceiveBufferSize()];
                final DatagramPacket receiveData = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(receiveData);
                Executors.newCachedThreadPool().submit(() -> onReceive(receiveData));
            }catch(IOException e) {
                if(e.getMessage().contains("socket closed")) logger.info("socket closed");
                else logger.warn("IOException: {}", e.getMessage());
            }
        }
        if(!this.isInterrupted()) {
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            start();
        }
    }

    private void onReceive(DatagramPacket receiveData) {

//                logger.info("Receiving: {}", receiveData.getData());
        String str = new String(receiveData.getData(), receiveData.getOffset(), receiveData.getLength(), StandardCharsets.UTF_8);

        InetAddress address = receiveData.getAddress();
        int port = receiveData.getPort();

        //1 + headerLength + 1 + i + 1 + 14 + 1 + data <= 65507
        if(str.startsWith("!")) {
            try {
                String[] split = str.substring(1).split(";", 4);
                int length = Integer.parseInt(split[0]);
                int i = Integer.parseInt(split[1]);
                long id = Long.parseLong(split[2]);
                String data = split[3];
                info("[{}:{}->CURRENT] Received bit data: {}", address.getHostAddress(), port, str);
                BigData bigData = bigDataMap.computeIfAbsent(id, k -> new BigData(length));
                if (!bigData.add(i, data)) return;
                bigDataMap.remove(id);
                str = id + ";" + String.join("", bigData.data);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        String[] split = str.split(";", 2);
        final long id = Long.parseLong(split[0]);
//                logger.info("[{}:{}] timestamp: {}", address.getHostAddress(), port, timestamp);

        if(echoMap.containsKey(id)) { //이곳에서 보낸 데이터일 경우 Future Complete
            info("[CURRENT->{}:{}->CURRENT] Received echo data: {}", address.getHostAddress(), port, str);
            echoMap.computeIfPresent(id,(k, v) -> {
                v.complete(UDPEchoResponse.of(split[1]));
                return null;
            });

        }else { //다른 곳에서 받은 데이터일 경우 데이터 되돌려주기
            info("[{}:{}->CURRENT] Received data: {}", address.getHostAddress(), port, str);

            Executors.newCachedThreadPool().submit(() -> { //onReceive 메소드 대기 중에도 통신 가능하도록 비동기 실행
                UDPEchoSend send = UDPEchoSend.of(split[1]); //수신 데이터 만들기
                UDPEchoResponse response = new UDPEchoResponse(); //반환 데이터 만들기
                InetSocketAddress host = new InetSocketAddress(address, port);
                handlers.forEach(udpMessageHandler -> udpMessageHandler.onReceive(host, send.clone(), response));
                info("[CURRENT->{}:{}] Response data: {}", address.getHostAddress(), port, response);
                sendData(response, id, host, null);
            });
        }
    }

    private static final int MAX_PACKET_SIZE = 65507;

    private void sendData(UDPEchoData data, long id, SocketAddress host, CompletableFuture<?> future) {
        String serializedData = data.toString();
        int length = serializedData.length();
        // 데이터가 최대 패킷 크기보다 클 경우 분할해서 전송
        if(length + 14 + 1 > MAX_PACKET_SIZE) { //id + ; = 14 + 1
            sendBigData(length, id, serializedData, host, future);
        }else {
            sendData("", id, serializedData, host, future);
        }
    }

    private void sendData(String header, long finalId, String data, SocketAddress host, @Nullable CompletableFuture<?> future) {
        byte[] buffer = (header + finalId + ";" + data).getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendData = new DatagramPacket(buffer, buffer.length, host);
        try {
            serverSocket.send(sendData);
        } catch (Exception e) {
            e.printStackTrace();
            if(future != null) future.completeExceptionally(e);
            else e.printStackTrace();
        }
    }
    private void sendBigData(int length, long finalId, String serializedData, SocketAddress host, CompletableFuture<?> future) {
        //1 + headerLength + 1 + i + 1 + 14 + 1 + data <= 65507
        int headerLength = String.valueOf(length).length();
        int defaultUnit = MAX_PACKET_SIZE-18-headerLength;
        for(int index = 0, i = 0;; i++) {
            int unit = defaultUnit - String.valueOf(i).length();
            String header = "!" + length + ";" + i + ";";
            int endIndex = Math.min(index + unit, length);
            String bitData = serializedData.substring(index, endIndex);
            sendData(header, finalId, bitData, host, future);
            if (endIndex == length) break;
            index += unit;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public CompletableFuture<UDPEchoResponse> sendDataAndReceive(SocketAddress host, final UDPEchoSend data) {
        long id = makeId();
        while(echoMap.containsKey(id)) id = makeId();
        CompletableFuture<UDPEchoResponse> future = new CompletableFuture<>();
        echoMap.put(id, future);
        InetSocketAddress address = (InetSocketAddress)host;
        info("[CURRENT->{}:{}] Sent data: {}", address.getHostString(), address.getPort(), data);
        final long finalId = id;
        Executors.newCachedThreadPool().submit(() -> sendData(data, finalId, host, future));
        return future;
    }

    public CompletableFuture<Void> sendData(SocketAddress host, final UDPEchoSend data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        sendDataAndReceive(host, data).orTimeout(TIMEOUT, TimeUnit.SECONDS)
        .whenCompleteAsync((result, throwable) -> {
//            logger.info("Compare data:\t \n\t\tSent:\t{}1\n\t\tReceive:\t{}1\nequals: {}", data, result, data.equals(result));
//            if(throwable == null && data.equals(result)) future.complete(null); //보낸 데이터와 받은 데이터가 일치 할 때
            if(throwable == null) future.complete(null);
            else future.completeExceptionally(new NoEchoDataException());
        });
        return future;
    }

    public boolean close() {
        if(serverSocket.isClosed()) return false;
        serverSocket.close();
        interrupt();
        return true;
    }
}
