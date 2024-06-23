import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;

import java.util.concurrent.Executors;

public class StressTest {

    public static void main(String[] args) throws Exception {
        SocketManager.initialize();

        var server = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8381);
        var client = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8382);

        var send = SocketAPI.createPacketSend("test");

        long start = System.currentTimeMillis();
        for(int i = 0; i < 60000; i++) {
            server.sendDataFuture(client.getHost(), send);
        }
        System.out.println("Main thread is running " + (System.currentTimeMillis() - start) + "ms");

    }

}
