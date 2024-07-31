package udp;

import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.data.SocketServer;

import java.util.concurrent.CompletableFuture;

public class ManyServerTest {

    // 로컬 테스트 시 UDPEchoServer 에서
    // private static final AtomicInteger identifier = new AtomicInteger(0);
    // 부분을
    // private final AtomicInteger identifier = new AtomicInteger(0);
    // 으로 수정 후 테스트

    public static void main(String[] args) throws Exception {
        SocketManager.initialize();
        SocketServer server1 = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8281);
        SocketServer server2 = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8282, true);
        SocketServer server3 = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8283, true);
        server1.open();
        server2.open();
        server3.open();

        long sendTime = System.currentTimeMillis() + 1000;

        var send = SocketAPI.createPacketSend("TEST");
        CompletableFuture.runAsync(() -> {
            while(System.currentTimeMillis() < sendTime);
            server2.sendDataFuture(server1.getHost(), send).whenComplete((packetResponse, throwable) -> {
                if(throwable != null) {
                    throwable.printStackTrace();
                    return;
                }
                System.out.println("Server2 response: " + packetResponse);
            });
        });
        while(System.currentTimeMillis() < sendTime);
        server3.sendDataFuture(server1.getHost(), send).whenComplete((packetResponse, throwable) -> {
            if(throwable != null) {
                throwable.printStackTrace();
                return;
            }
            System.out.println("Server3 response: " + packetResponse);
        });
    }
}
