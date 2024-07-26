import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class TimeoutTest {

    public static void main(String[] args) throws Exception {
        SocketManager.initialize();

        var socket1 = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8381, true);
        var socket2 = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8382, true);

        socket1.open();
        socket2.open();

        var packet = SocketAPI.createPacketSend();
        for(int i = 0; i < 30; i++) {
            socket1.sendDataFuture(socket2.getHost(), packet)
                    .whenComplete((result, throwable) -> {
                        System.out.println("Completed: " + result);
                        System.out.println(throwable);
                    });
            Thread.sleep(1500);
        }

        Thread.sleep(30*1000);
        socket1.sendDataFuture(socket2.getHost(), packet)
                .whenComplete((result, throwable) -> {
                    System.out.println("Completed: " + result);
                    System.out.println(throwable);
                });
    }

}
