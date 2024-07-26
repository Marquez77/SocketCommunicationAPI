import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTest {

    public static void main(String[] args) throws Exception {
        SocketManager.initialize();

        var server = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8381, true, 10, 5);
        var client = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8382);

        server.open();
        client.open();

        var listener = new PacketListener() {
            AtomicInteger i = new AtomicInteger(0);
            @PacketHandler(identifiers = "*")
            public void onTest(PacketMessage message) throws InterruptedException {
                i.incrementAndGet();
//                System.out.println(i.incrementAndGet());
//                Thread.sleep(1000);
            }
        };
        client.registerListener(listener);

        var send = SocketAPI.createPacketSend("test");

        long start = System.currentTimeMillis();
        for(int i = 0; i < 30; i++) {
            server.sendDataFuture(client.getHost(), send)
                    .completeOnTimeout(false, 100, TimeUnit.MILLISECONDS)
                    .whenComplete((result, throwable) -> {
                    });
        }
        System.out.println("Main thread is running " + (System.currentTimeMillis() - start) + "ms");

//        start = System.currentTimeMillis();
//        for(int i = 0; i < 30000; i++) {
//            server.sendDataAndReceive(client.getHost(), send, true);
//        }
//        System.out.println("Main thread is running " + (System.currentTimeMillis() - start) + "ms");
//
//        start = System.currentTimeMillis();
//        for(int i = 0; i < 30000; i++) {
//            server.sendDataFuture(client.getHost(), send);
//        }
//        System.out.println("Main thread is running " + (System.currentTimeMillis() - start) + "ms");
//
//        start = System.currentTimeMillis();
//        for(int i = 0; i < 30000; i++) {
//            server.sendDataFuture(client.getHost(), send).completeOnTimeout(false, 1000, TimeUnit.MILLISECONDS)
//                    .whenComplete((unused, throwable) -> {
////                        if(unused)
////                            System.out.println("Success");
////                        else
////                            System.out.println("Failed");
//                    });
//        }
//        System.out.println("Main thread is running " + (System.currentTimeMillis() - start) + "ms");

        Thread.sleep(10000);
        System.out.println(listener.i.get());

//        Thread.sleep(10000);
//
//
//        for(int i = 0; i < 30000; i++) {
//            server.sendDataAndReceive(client.getHost(), send, true);
//        }
    }

}
