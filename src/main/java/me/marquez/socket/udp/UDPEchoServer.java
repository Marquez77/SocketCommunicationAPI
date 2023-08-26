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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPEchoServer extends Thread{

    private static final long TIMEOUT = 30; //UDP 응답 최대 대기 시간 (초)
    private static final AtomicInteger identifier = new AtomicInteger(0); //동시성 문제 해결을 위해 원자성을 보장하는 Atomic 클래스 사용

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
    private final Map<Long, CompletableFuture<Void>> bigDataBeginMap = new ConcurrentHashMap<>();
    private final Map<Long, BigData> bigDataMap = new ConcurrentHashMap<>(); //identifier, bigData

    private final ExecutorService receiveThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService sendThreadPool = Executors.newCachedThreadPool();

    //IPv4의 UDP 데이터그램 페이로드 제한은 65535-28 = 65507 byte
    private static final int MAX_PACKET_SIZE = 65507;

    private static class BigData {
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        int targetLength;
        int currentLength;
        String[] data;

        public BigData(int length) {
            this.targetLength = length;
            this.currentLength = 0;
            int size = (int)Math.ceil((double)length/MAX_PACKET_SIZE);
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
                receiveThreadPool.submit(() -> onReceiveData(receiveData));
            }catch(Exception e) {
                if(e.getMessage().contains("socket closed")) logger.info("socket closed");
                else logger.warn("IOException: {}", e.getMessage());
                e.printStackTrace();
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

    private void onReceiveData(DatagramPacket receiveData) {
        final String str = new String(receiveData.getData(), receiveData.getOffset(), receiveData.getLength(), StandardCharsets.UTF_8);
        InetAddress address = receiveData.getAddress();
        int port = receiveData.getPort();

        if(!checkBigData(address, port, str)) {
            processReceivedData(address, port, str, false);
        }
    }

    private void processBitData(InetAddress address, int port, String str) {
        //!index;id;bitData
        String[] split = str.substring(1).split(";", 3);
        int i = Integer.parseInt(split[0]);
        long id = Long.parseLong(split[1]);
        String data = split[2];
        BigData bigData = bigDataMap.getOrDefault(id, null);
        if(bigData == null) { // 시작 패킷이 먼저 오지 않은 경우 무시
            info("[{}:{}->CURRENT] Received invalid bit data: {}", address.getHostAddress(), port, str);
        }else {
            bigData.threadPool.submit(() -> { //해당 BigData 의 싱글 쓰레드에서 처리
                info("[{}:{}->CURRENT] Received bit data: {}", address.getHostAddress(), port, str);
                if (bigData.add(i, data)) { //데이터를 끝까지 받은 경우
                    String packet = id + ";" + String.join("", bigData.data);
                    processReceivedData(address, port, packet, false); //지금까지 받은 데이터 합쳐서 처리
                    bigDataMap.remove(id, bigData); //맵에서 삭제
                }
            });
        }
    }

    private boolean checkBigData(InetAddress address, int port, String str) {
        if(str.startsWith("!!")) { //!!packetSize;id (BigData 시작)
            String[] split = str.substring(2).split(";", 3);
            int length = Integer.parseInt(split[0]);
            long id = Long.parseLong(split[1]);
            bigDataMap.put(id, new BigData(length));
            info("[{}:{}->CURRENT] Received big data begin: {}", address.getHostAddress(), port, str);
            String packet = id + ";";
            receiveThreadPool.submit(() -> processReceivedData(address, port, packet, true));
        }else if(str.startsWith("!")) { //!index;id;bitData (BigData 조각 패킷)
            processBitData(address, port, str);
        }else {
            return false;
        }
        return true;
    }

    private void processReceivedData(InetAddress address, int port, String str, boolean bigDataStart) {
        String[] split = str.split(";", 2);
        final long id = Long.parseLong(split[0]);
//                logger.info("[{}:{}] timestamp: {}", address.getHostAddress(), port, timestamp);
        if(bigDataBeginMap.containsKey(id)) { //BigData 시작 패킷일 경우 Future Complete
            info("[CURRENT->{}:{}->CURRENT] Received echo big data begin: {}", address.getHostAddress(), port, str);
            bigDataBeginMap.computeIfPresent(id, (k, v) -> {
                v.complete(null);
                return null;
            });
        }else if(echoMap.containsKey(id)) { //이곳에서 보낸 데이터일 경우 Future Complete
            info("[CURRENT->{}:{}->CURRENT] Received echo data: {}", address.getHostAddress(), port, str);
            echoMap.computeIfPresent(id,(k, v) -> {
                v.complete(UDPEchoResponse.of(split[1]));
                return null;
            });

        }else { //다른 곳에서 받은 데이터일 경우 데이터 되돌려주기
            String data = split[1];
            if(bigDataStart) {
                data = "[]";
            }else {
                info("[{}:{}->CURRENT] Received data: {}", address.getHostAddress(), port, str);
            }
            UDPEchoSend send = UDPEchoSend.of(data); //수신 데이터 만들기
            UDPEchoResponse response = new UDPEchoResponse(); //반환 데이터 만들기
            InetSocketAddress host = new InetSocketAddress(address, port);
            if(!bigDataStart) handlers.forEach(udpMessageHandler -> udpMessageHandler.onReceive(host, send.clone(), response));
            info("[CURRENT->{}:{}] Response data: {}", address.getHostAddress(), port, response);
            sendData(response, id, host, null);
        }
    }

    private void sendData(UDPEchoData data, long id, SocketAddress host, CompletableFuture<?> future) {
        String serializedData = data.toString();
        int length = serializedData.length();
        // 데이터가 최대 패킷 크기보다 클 경우 분할해서 전송
        if(length + 14 + 1 > MAX_PACKET_SIZE) { //id + ; = 14 + 1
            try {
                sendBigData(length, id, serializedData, host, future);
            } catch (Exception e) {
                if(future != null) future.completeExceptionally(e);
            }
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
    private void sendBigData(int length, long finalId, String serializedData, SocketAddress host, CompletableFuture<?> future) throws Exception{
        /*
            bigData 전송 전 시작한다는 패킷 먼저 전송
            해당 패킷 응답 오면 bigData 전송 시작
         */
        //!!packetSize;id
        sendBigDataBeginAndReceive("!!" + length + ";", finalId, host).join();
        /*
            bitSize = headerLength + id + 1 + bitDataLength
            bitSize 를 계산해서 index ~ endIndex 단위로 패킷을 잘라서 전송
            bitSize 는 MAX_PACKET_SIZE 보다 작아야 함
         */
        int bitSize = 0;
        int endIndex = 0;
        for(int index = 0, i = 0; endIndex < length; i++, index += bitSize) {
            String header = "!" + i + ";"; //!index;id ( id 는 sendData 에서 붙임 )
            int headerLength = header.length();
            bitSize = MAX_PACKET_SIZE-15-headerLength; //15 는 id + ; 의 길이
            endIndex = Math.min(index + bitSize, length);
            String bitData = serializedData.substring(index, endIndex);
            sendData(header, finalId, bitData, host, future);
            if(i%2 == 0) Thread.sleep(1);
        }
    }
    private CompletableFuture<Void> sendBigDataBeginAndReceive(String header, long id, SocketAddress host) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        bigDataBeginMap.put(id, future);
        InetSocketAddress address = (InetSocketAddress)host;
        info("[CURRENT->{}:{}] Sent big data begin", address.getHostString(), address.getPort());
        final long finalId = id;
        sendThreadPool.submit(() -> sendData(header, finalId, "", host, future));
        return future;
    }
    public CompletableFuture<UDPEchoResponse> sendDataAndReceive(SocketAddress host, final UDPEchoSend data) {
        long id = makeId();
        while(echoMap.containsKey(id)) id = makeId();
        CompletableFuture<UDPEchoResponse> future = new CompletableFuture<>();
        echoMap.put(id, future);
        InetSocketAddress address = (InetSocketAddress)host;
        info("[CURRENT->{}:{}] Sent data: {}", address.getHostString(), address.getPort(), data);
        final long finalId = id;
        sendThreadPool.submit(() -> sendData(data, finalId, host, future));
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
        receiveThreadPool.shutdownNow();
        sendThreadPool.shutdownNow();
        interrupt();
        return true;
    }
}
