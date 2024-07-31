package me.marquez.socket.tcp;

import me.marquez.socket.AbstractSocketServer;
import me.marquez.socket.SocketAPI;
import me.marquez.socket.packet.entity.PacketReceive;
import me.marquez.socket.packet.entity.PacketReceiveImpl;
import me.marquez.socket.packet.entity.PacketSend;
import me.marquez.socket.queue.ExecutionQueuePool;
import me.marquez.socket.queue.SocketThreadFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class TCPServer extends AbstractSocketServer {

    private final int threadPoolSize;
    private final int maximumQueuePerTarget;
    private ExecutionQueuePool receiveThreadPool;
    private WebSocketServer server;
    private final Map<SocketAddress, WebSocket> clients = new ConcurrentHashMap<>();
    private final Map<WebSocket, SocketAddress> clientAddressMap = new ConcurrentHashMap<>();

    protected TCPServer(SocketAddress host, boolean debug, int threadPoolSize, int maximumQueuePerTarget) {
        super(host, debug);
        this.threadPoolSize = threadPoolSize;
        this.maximumQueuePerTarget = maximumQueuePerTarget;
        initThreadPool();
    }

    private void initThreadPool() {
        if(receiveThreadPool != null)
            receiveThreadPool.shutdownNow();
        receiveThreadPool = new ExecutionQueuePool("Receive", threadPoolSize, maximumQueuePerTarget);
        info("Thread pool initialized (threadPoolSize={}, maximumQueuePerTarget={})", threadPoolSize, maximumQueuePerTarget);
    }

    private void printSendingThreadPoolStatus(boolean detail) {
//        info("[Sending thread pool] idle queues: {}/{}", sendThreadPool.getEmptyQueues(), sendThreadPool.size());
//        if(detail) info("[Sending thread pool] └ Queue sizes: {}", Arrays.toString(sendThreadPool.getQueueSizes()));
    }

    private void printReceivingThreadPoolStatus(boolean detail) {
        info("[Receiving thread pool] idle queues: {}/{}", receiveThreadPool.getEmptyQueues(), receiveThreadPool.size());
        if(detail) info("[Receiving thread pool] └ Queue sizes: {}", Arrays.toString(receiveThreadPool.getQueueSizes()));
    }

    @Override
    public void open() {
        server = new WebSocketServer((InetSocketAddress)host) {

            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                info("[{}<-{}] Opened connection", host, webSocket.getRemoteSocketAddress());
            }

            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                info("[{}<-{}] Closed connection: {}, {}, {}", host, webSocket.getRemoteSocketAddress(), i, s, b);
            }

            @Override
            public void onMessage(WebSocket webSocket, String s) {
                long id = (long)(Math.random()*(1000000000000000L));
                info("[{}<-{}] Received message [{}]: {}", host, webSocket.getRemoteSocketAddress(), id, trim(s));
                printReceivingThreadPoolStatus(false);
                receiveThreadPool.submit(id, webSocket.getRemoteSocketAddress(), () -> {
                    PacketReceive receive = PacketReceiveImpl.of(s);
                    if (receive.getIdentifiers().length == 1 &&
                            receive.getIdentifiers()[0].equalsIgnoreCase("TCP-Server-Open")) {
                        String hostname = receive.nextString();
                        var address = new InetSocketAddress(webSocket.getRemoteSocketAddress().getHostString(), receive.nextInt());
                        clients.put(address, webSocket);
                        clientAddressMap.put(webSocket, address);
                        info("[{}<-{}] Received connection hostname: {}", host, webSocket.getRemoteSocketAddress(), address);
                    } else {
                        var address = clientAddressMap.get(webSocket);
                        if(address == null)
                            return;
                        info("[{}<-{}] Received data [{}]: {}", host, address, id, trim(s));
                        printReceivingThreadPoolStatus(true);
                        onReceive(address, receive, null);
                    }
                }, null);
            }

            @Override
            public void onError(WebSocket webSocket, Exception e) {
                error(e);
                info("[{}<-{}] Error on connection: {}", host, webSocket.getRemoteSocketAddress(), e.getMessage());
            }

            @Override
            public void onStart() {
                info("Server started on {}", host);
            }

        };
        server.setConnectionLostTimeout(1);
        server.start();
    }

    @Override
    public boolean isOpen() {
        return server != null;
    }

    @Override
    public void close() {
        try {
            server.stop(0, "Closed by offered API");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.server = null;
    }

    @Override
    public void sendData(SocketAddress address, PacketSend send_packet) {
        sendDataFuture(address, send_packet);
    }

    @Override
    public CompletableFuture<Boolean> sendDataFuture(SocketAddress address, PacketSend send_packet) {
        var str = send_packet.toString();
        info("[{}->{}] Sending data: {}", host, address, trim(str));
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        WebSocket client = clients.computeIfAbsent(address, a -> {
            URI uri = null;
            try {
                uri = new URI("ws://" + address.toString().replace("/",""));
            } catch (URISyntaxException e) {
                error(e);
            }
            if(uri == null)
                return null;
            info("[{}->{}] Connecting to {}", host, address, uri);
            WebSocketClient wsClient = new WebSocketClient(uri) {

                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    info("[{}->{}] Opened connection", host, address);
                    PacketSend send = SocketAPI.createPacketSend("TCP-Server-Open");
                    send.appendString(((InetSocketAddress)host).getHostString());
                    send.appendInt(((InetSocketAddress)host).getPort());
                    send(send.toString());
                    future.complete(true);
                }

                @Override
                public void onMessage(String s) {
                    long id = (long)(Math.random()*(1000000000000000L));
                    info("[{}<-{}] Received data [{}]: {}", host, address, id, trim(s));
                    printReceivingThreadPoolStatus(false);
                    receiveThreadPool.submit(id, address, () -> {
                        printReceivingThreadPoolStatus(true);
                        PacketReceive receive = PacketReceiveImpl.of(s);
                        onReceive(address, receive, null);
                    }, null);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    info("[{}->{}] Closed connection: {}, {}, {}", host, address, i, s, b);
                    future.complete(false);
                }

                @Override
                public void onError(Exception e) {
                    info("[{}->{}] Error on connection: {}", host, address, e.getMessage());
                    future.complete(false);
                }
            };
            wsClient.setConnectionLostTimeout(1);
            wsClient.connect();
            if(future.join())
                return wsClient;
            return null;
        });
        if(client == null)
            return CompletableFuture.completedFuture(false);
        if(!client.isOpen() || client.isClosed()) {
            info("[{}->{}] Connection is not open", host, address);
            clients.remove(address);
            return sendDataFuture(address, send_packet);
        }
        client.send(str);
        info("[{}->{}] Sent data: {}", host, address, trim(str));
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<PacketReceive> sendDataAndReceive(SocketAddress address, PacketSend send_packet, boolean resend) {
        throw new UnsupportedOperationException("Not supported in TCP server");
    }

}
