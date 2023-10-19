package me.marquez.socket.udp;

import lombok.AllArgsConstructor;
import me.marquez.socket.AbstractSocketServer;
import me.marquez.socket.SocketAPI;
import me.marquez.socket.packet.entity.impl.PacketResponse;
import me.marquez.socket.packet.entity.impl.PacketSend;
import me.marquez.socket.udp.exception.DataLossException;
import me.marquez.socket.udp.exception.NoEchoDataException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPEchoServer extends AbstractSocketServer {

    private static final long TIMEOUT = 30; //UDP 응답 최대 대기 시간 (초)
    private static final AtomicInteger identifier = new AtomicInteger(0); //동시성 문제 해결을 위해 원자성을 보장하는 Atomic 클래스 사용

    private DatagramSocket serverSocket;

    public UDPEchoServer(SocketAddress host, boolean debug) {
        super(host, debug);
    }

    private long makeId() {
        return System.currentTimeMillis()*10 + identifier.getAndIncrement();
    }


    //identifier, response future
    private final Map<Long, SentData> echoMap = new ConcurrentHashMap<>();
    private final Map<Long, CompletableFuture<Void>> bigDataBeginMap = new ConcurrentHashMap<>();
    private final Map<Long, BigData> bigDataMap = new ConcurrentHashMap<>(); //identifier, bigData

    private final ExecutorService mainThreadPool = Executors.newSingleThreadExecutor();
    private final ExecutorService receiveThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService sendThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService waitingThreadPool = Executors.newCachedThreadPool();

    //IPv4의 UDP 데이터그램 페이로드 제한은 65535-28 = 65507 byte
    private static final int MAX_PACKET_SIZE = 65507;

    @Override
    public void open() throws IOException {
        serverSocket = new DatagramSocket(host);
        mainThreadPool.submit(this::run);
    }

    @Override
    public void close() throws IOException {
        if(serverSocket.isClosed())
            return;
        serverSocket.close();
        mainThreadPool.shutdownNow();
        receiveThreadPool.shutdownNow();
        sendThreadPool.shutdownNow();
        waitingThreadPool.shutdownNow();
    }

    public void run() {
        SocketAPI.LOGGER.info("Starting udp echo server listening port on {}", host);
        while (!serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
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
            run();
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

    private String combineBigData(long id, BigData bigData) {
        return id + ";" + String.join("", bigData.data);
    }

    private void processBitData(InetAddress address, int port, String str) {
        //!index;id;bitData
        String[] split = str.substring(1).split(";", 3);
        int i = Integer.parseInt(split[0]);
        long id = Long.parseLong(split[1]);
        String data = split[2];
        BigData bigData = bigDataMap.getOrDefault(id, null);
        if(bigData == null) { // 시작 패킷이 먼저 오지 않은 경우 무시
            info("[{}:{}->CURRENT] Received invalid bit data: {}", address.getHostAddress(), port, trim(str));
        }else {
            bigData.threadPool.submit(() -> { //해당 BigData 의 싱글 쓰레드에서 처리
                info("[{}:{}->CURRENT] Received bit data: {}", address.getHostAddress(), port, trim(str));
                if (bigData.add(i, data)) { //데이터를 끝까지 받은 경우
                    String packet = combineBigData(id, bigData);
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
            bigDataMap.put(id, new BigData(length, waitingThreadPool) {
                @Override
                public void timeout() {
                    String data = combineBigData(id, this);
                    responseData(id, data, address, port, false);
                }
            });
            info("[{}:{}->CURRENT] Received big data begin: {}", address.getHostAddress(), port, trim(str));
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
        int receivedLength = 0;
        if(split[1].startsWith("l")) { //Response 데이터 인지 확인
            split = split[1].substring(1).split(";", 2);
            receivedLength = Integer.parseInt(split[0]);
        }
        if(bigDataBeginMap.containsKey(id)) { //BigData 시작 패킷일 경우 Future Complete
            info("[CURRENT->{}:{}->CURRENT] Received echo big data begin: {}", address.getHostAddress(), port, trim(str));
            bigDataBeginMap.computeIfPresent(id, (k, v) -> {
                v.complete(null);
                return null;
            });
        }else if(echoMap.containsKey(id)) { //이곳에서 보낸 데이터일 경우 Future Complete
            info("[CURRENT->{}:{}->CURRENT] Received echo data: {}", address.getHostAddress(), port, trim(str));
            final String[] finalSplit = split;
            final int finalReceivedLength = receivedLength;
            echoMap.computeIfPresent(id,(k, sentData) -> {
                UDPEchoResponse response = UDPEchoResponse.of(finalSplit[1]);
                int sentLength = sentData.data.toString().length();
                if(finalReceivedLength != sentLength) { //보낸 데이터와 받은 데이터 길이가 다를 경우
                    double loss = 1-(double)finalReceivedLength/sentLength;
                    if(sentData.resend) { //재전송
                        sendThreadPool.submit(() -> sendData(sentData.data, id, new InetSocketAddress(address, port), sentData.future, ""));
                        return sentData; //맵에서 지워지지 않게 하기
                    }else {
                        sentData.future.completeExceptionally(new DataLossException(sentLength, finalReceivedLength, loss));
                    }
                }else {
                    sentData.future.complete(response);
                }
                return null;
            });

        }else { //다른 곳에서 받은 데이터일 경우 데이터 되돌려주기
            if(receivedLength == 0) { //오류 방지, response 데이터가 아님을 확인
                String data = split[1];
                if (bigDataStart) {
                    data = "[]";
                } else {
                    info("[{}:{}->CURRENT] Received data: {}", address.getHostAddress(), port, trim(str));
                }
                responseData(id, data, address, port, !bigDataStart);
            }
        }
    }

    private void responseData(long id, String data, InetAddress address, int port, boolean handlingResponse) {
        UDPEchoResponse response = new UDPEchoResponse(); //반환 데이터 만들기
        InetSocketAddress host = new InetSocketAddress(address, port);
        if(handlingResponse) {
            UDPEchoSend send = UDPEchoSend.of(data); //수신 데이터 만들기
            handlers.forEach(udpMessageHandler -> udpMessageHandler.onReceive(host, send.clone(), response));
        }
        info("[CURRENT->{}:{}] Response data: {}", address.getHostAddress(), port, response);
        sendData(response, id, host, null, ("l" + data.length() + ";"));
    }

    private void sendData(UDPEchoData data, long id, SocketAddress host, CompletableFuture<?> future, String responseHeader) {
        String serializedData = data.toString();
        int length = serializedData.length();
        // 데이터가 최대 패킷 크기보다 클 경우 분할해서 전송
        if(length + 14 + 1 > MAX_PACKET_SIZE) { //id + ; = 14 + 1
            try {
                sendBigData(length, id, serializedData, host, future, responseHeader);
            } catch (Exception e) {
                if(future != null) future.completeExceptionally(e);
            }
        }else {
            sendData("", id, serializedData, host, future, responseHeader);
        }
    }

    private void sendData(String header, long finalId, String data, SocketAddress host, @Nullable CompletableFuture<?> future, String responseHeader) {
        byte[] buffer = (header + finalId + ";" + responseHeader + data).getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendData = new DatagramPacket(buffer, buffer.length, host);
        try {
            serverSocket.send(sendData);
        } catch (Exception e) {
            e.printStackTrace();
            if(future != null) future.completeExceptionally(e);
            else e.printStackTrace();
        }
    }
    private void sendBigData(int length, long finalId, String serializedData, SocketAddress host, CompletableFuture<?> future, String responseHeader) throws Exception{
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
            if(future.isDone()) break;
            String header = "!" + i + ";"; //!index;id ( id 는 sendData 에서 붙임 )
            int headerLength = header.length();
            bitSize = MAX_PACKET_SIZE-15-headerLength; //15 는 id + ; 의 길이
            endIndex = Math.min(index + bitSize, length);
            String bitData = serializedData.substring(index, endIndex);
            sendData(header, finalId, bitData, host, future, responseHeader);
//            if(i%2 == 0) Thread.sleep(1);
        }
    }
    private CompletableFuture<Void> sendBigDataBeginAndReceive(String header, long id, SocketAddress host) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        bigDataBeginMap.put(id, future);
        InetSocketAddress address = (InetSocketAddress)host;
        info("[CURRENT->{}:{}] Sent big data begin", address.getHostString(), address.getPort());
        final long finalId = id;
        sendThreadPool.submit(() -> sendData(header, finalId, "", host, future, ""));
        return future;
    }
    public CompletableFuture<UDPEchoResponse> sendDataAndReceive(SocketAddress host, final UDPEchoSend data) {
        return sendDataAndReceive(host, data, false);
    }
    public CompletableFuture<UDPEchoResponse> sendDataAndReceive(SocketAddress host, final UDPEchoSend data, boolean resend) {
        long id = makeId();
        while(echoMap.containsKey(id)) id = makeId();
        CompletableFuture<UDPEchoResponse> future = new CompletableFuture<>();
        echoMap.put(id, new SentData(data, future, resend));
        InetSocketAddress address = (InetSocketAddress)host;
        String str = data.toString();
        info("[CURRENT->{}:{}] Sent data: {}", address.getHostString(), address.getPort(), trim(str));
        final long finalId = id;
        sendThreadPool.submit(() -> sendData(data, finalId, host, future, ""));
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

    @AllArgsConstructor
    private static class SentData {
        final PacketSend data;
        final CompletableFuture<PacketResponse> future;
        final boolean resend;
    }

    private static abstract class BigData {
        final ExecutorService threadPool = Executors.newSingleThreadExecutor();
        int targetLength;
        int currentLength;
        String[] data;
        long timeout;

        public BigData(int length, ExecutorService executors) {
            this.targetLength = length;
            this.currentLength = 0;
            int size = (int)Math.ceil((double)length/MAX_PACKET_SIZE)+1;
            this.data = new String[size];
            Arrays.fill(data, "");
            executors.submit(this::checkReceiving);
        }

        public boolean add(int i, String data) {
            this.data[i] = data;
            currentLength += data.length();
            timeout += System.currentTimeMillis()+10L;
            return currentLength == targetLength;
        }

        public void checkReceiving() {
            timeout = System.currentTimeMillis()+100L;
            while(System.currentTimeMillis() < timeout);
            if(currentLength != targetLength) timeout();
        }

        abstract public void timeout();
    }
}
