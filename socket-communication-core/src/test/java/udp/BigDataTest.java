package udp;

import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;
import me.marquez.socket.packet.entity.PacketSend;

import java.net.InetSocketAddress;

public class BigDataTest implements PacketListener {
    private static long min = Long.MAX_VALUE;
    private static long max = Long.MIN_VALUE;
    private static long sum = 0;
    public static void main(String[] args) throws Exception {
        new BigDataTest().run();
    }
    public void run() throws Exception {
        SocketManager.initialize();

        SocketServer server = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8280);
//        server.setDebug(true);
        server.open();

        SocketServer server2 = SocketAPI.getFactory(ServerProtocol.UDP).create("localhost", 8281);
//        server2.setDebug(true);
        server2.registerListener(this);
        server2.open();

        PacketSend send = SocketAPI.createPacketSend();
        for(int i = 0; i < 5000000; i++) {
            send.append("aaaaaaaaaaaaaaaaaaaa");
        }
        int count = 10;
        for(int i = 0; i < count; i++) {
    //        System.out.println(send);
            System.out.println("length: " + send.toString().length());
            long start = System.currentTimeMillis();
            server.sendDataAndReceive(new InetSocketAddress("localhost", 8281), send, true)
                    .whenComplete((udpEchoResponse, throwable) -> {
                        if(throwable != null) {
                            throwable.printStackTrace();
                            return;
                        }
                        System.out.println("response: " + udpEchoResponse);
                        long time = (System.currentTimeMillis()-start);
                        System.out.println("time: " + time);
                        sum += time;
                        min = Math.min(min, time);
                        max = Math.max(max, time);
            }).exceptionally(throwable -> {
                System.out.println(throwable.getMessage());
                return null;
                    }).join();
        }
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("avg: " + (double)sum/count);
        System.exit(0);
    }

    @PacketHandler
    public void onReceive(PacketMessage message) {
        System.out.println("onReceive");
    }
}
