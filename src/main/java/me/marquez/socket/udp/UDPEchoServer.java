package me.marquez.socket.udp;

import me.marquez.socket.udp.entity.UDPEchoResponse;
import me.marquez.socket.udp.entity.UDPEchoSend;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UDPEchoServer extends Thread{

    private static final long TIMEOUT = 30; //UDP 응답 최대 대기 시간 (초)

    private final int serverPort;
    private final Logger logger;
    private DatagramSocket serverSocket;
    private final List<UDPMessageHandler> handlers;

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

    //timestamp, response future
    private final Map<Long, CompletableFuture<UDPEchoResponse>> echoMap = new ConcurrentHashMap<>();

    @Override
    public void run() {
        logger.info("Starting udp echo server listening port on {}", serverPort);
        while (!serverSocket.isClosed() && !this.isInterrupted()) {
            byte[] buffer = new byte[2048];
            final DatagramPacket receiveData = new DatagramPacket(buffer, buffer.length);
            try {
                serverSocket.receive(receiveData);
//                logger.info("Receiving: {}", receiveData.getData());
                String str = new String(receiveData.getData(), receiveData.getOffset(), receiveData.getLength(), StandardCharsets.UTF_8);
                String[] split = str.split(";", 2);
                final long timestamp = Long.parseLong(split[0]);

                InetAddress address = receiveData.getAddress();
                int port = receiveData.getPort();
//                logger.info("[{}:{}] timestamp: {}", address.getHostAddress(), port, timestamp);

                if(echoMap.containsKey(timestamp)) { //이곳에서 보낸 데이터일 경우 Future Complete
                    logger.info("[CURRENT->{}:{}->CURRENT] Received echo data: {}", address.getHostAddress(), port, str);
                    echoMap.computeIfPresent(timestamp,(k, v) -> {
                        v.complete(new UDPEchoResponse(split[1]));
                        return null;
                    });

                }else { //다른 곳에서 받은 데이터일 경우 데이터 되돌려주기
                    logger.info("[{}:{}->CURRENT] Received data: {}", address.getHostAddress(), port, str);

                    Executors.newCachedThreadPool().submit(() -> { //onReceive 메소드 대기 중에도 통신 가능하도록 비동기 실행
                        UDPEchoSend send = new UDPEchoSend(split[1]); //수신 데이터 만들기
                        UDPEchoResponse response = new UDPEchoResponse(); //반환 데이터 만들기
                        handlers.forEach(udpMessageHandler -> udpMessageHandler.onReceive(new InetSocketAddress(address, port), send.clone(), response));
                        logger.info("[CURRENT->{}:{}] Response data: {}", address.getHostAddress(), port, response);

                        byte[] responseBuffer = (timestamp + ";" + response.toString()).getBytes(StandardCharsets.UTF_8);
                        DatagramPacket sendData = new DatagramPacket(responseBuffer, responseBuffer.length, address, port);
                        try {
                            serverSocket.send(sendData);
//                            logger.info("Sending: {}", sendData.getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
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

    public CompletableFuture<UDPEchoResponse> sendDataAndReceive(SocketAddress host, final UDPEchoSend data) {
        long timestamp = System.currentTimeMillis();
        CompletableFuture<UDPEchoResponse> future = new CompletableFuture<>();
        while(echoMap.containsKey(timestamp)) timestamp += 1L;
        echoMap.put(timestamp, future);
        InetSocketAddress address = (InetSocketAddress)host;
        logger.info("[CURRENT->{}:{}] Sent data: {}", address.getHostString(), address.getPort(), data);
        final long id = timestamp;
        Executors.newCachedThreadPool().submit(() -> {
            byte[] buffer = (id + ";" + data).getBytes(StandardCharsets.UTF_8);
            DatagramPacket sendData = new DatagramPacket(buffer, buffer.length, host);
            try {
                serverSocket.send(sendData);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> sendData(SocketAddress host, final UDPEchoSend data) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        sendDataAndReceive(host, data).orTimeout(TIMEOUT, TimeUnit.SECONDS)
        .whenCompleteAsync((result, throwable) -> {
//            logger.info("Compare data:\t \n\t\tSent:\t{}1\n\t\tReceive:\t{}1\nequals: {}", data, result, data.equals(result));
//            if(throwable == null && data.equals(result)) future.complete(null); //보낸 데이터와 받은 데이터가 일치 할 때
            if(throwable == null) future.complete(null);
            future.completeExceptionally(new NoEchoDataException());
        });
        return future;
    }

    public boolean close() {
        if(serverSocket.isClosed()) return false;
        serverSocket.close();
        return true;
    }
}
