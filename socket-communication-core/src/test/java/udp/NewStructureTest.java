package udp;

import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

public class NewStructureTest {
    private static long start;
    public static void main(String[] args) throws IOException {
        SocketManager.initialize();

        var udpFactory = SocketAPI.getFactory(ServerProtocol.UDP);
        if(udpFactory == null)
            return;

        SocketServer server1 = udpFactory.createOrGet("localhost", 8081, false);

        SocketServer server2 = udpFactory.createOrGet("localhost", 8082, false);

        server1.open();
        server2.open();

        server2.registerListener(new PacketListener() {
            @PacketHandler(identifiers = {"TEST", "*"})
            public void onReceivePacket(PacketMessage message) throws IOException, ClassNotFoundException {
                System.out.println(System.currentTimeMillis() - start);
                System.out.println("Receive: " + message.received_packet());
                System.out.println("r1: " + message.received_packet().nextString());
                System.out.println("r2: " + message.received_packet().nextInt());
                System.out.println("r3: " + message.received_packet().nextUUID());
                System.out.println("r4: " + message.received_packet().nextObject(User.class));
                System.out.println("r5: " + message.received_packet().nextString());
                System.out.println("r6: " + message.received_packet().nextString());
                message.response_packet().append("RESPONSE");
            }
        });

        var send = SocketAPI.createPacketSend("TEST", "HI");
        send.append("TEST한글")
            .append(1234)
            .append(UUID.randomUUID())
            .append(new User(UUID.randomUUID(), "marquez", 100))
            .append("QWER")
            .append("ASDF");
        System.out.println("send: " + send.toString().isEmpty() + " " + Arrays.toString(send.toString().getBytes()));
        start = System.currentTimeMillis();
        server1.sendDataAndReceive(server2.getHost(), send)
                .whenComplete((packetReceive, throwable) -> {
                    System.out.println(System.currentTimeMillis() - start + "ms");
                    System.out.println("and receive: " + packetReceive);
                    System.out.println("ar1: " + packetReceive.nextString());
                });

//        SocketServer server3 = udpFactory.createOrGet("localhost", 881, true);
//        if(server3.isOpen()) {
//            System.out.println("already open");
//        }else {
//            server3.open();
//        }
    }

    public static record User(UUID uuid, String name, int level) implements Serializable {}
}
