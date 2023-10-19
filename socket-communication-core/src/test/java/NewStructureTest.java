import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;

import java.io.IOException;
import java.util.Arrays;

public class NewStructureTest {
    public static void main(String[] args) throws IOException {
        SocketManager.initialize();

        var udpFactory = SocketAPI.getFactory(ServerProtocol.UDP);
        if(udpFactory == null)
            return;

        SocketServer server1 = udpFactory.createOrGet("localhost", 8281, true);

        SocketServer server2 = udpFactory.createOrGet("localhost", 8282, true);

        server1.open();
        server2.open();

        server2.registerListener(new PacketListener() {
            @PacketHandler
            public void onReceivePacket(PacketMessage message) throws IOException {
                System.out.println("Receive: " + message.received_packet());
                System.out.println("r1: " + message.received_packet().nextString());
                System.out.println("r2: " + message.received_packet().nextString());
                message.response_packet().append("RESPONSE");
            }
        });

        var send = SocketAPI.createPacketSend();
        send.append("TEST한글")
            .append("QWER");
        System.out.println("send: " + send.toString().isEmpty() + " " + Arrays.toString(send.toString().getBytes()));
        server1.sendDataAndReceive(server2.getHost(), send)
                .whenComplete((packetReceive, throwable) -> {
                    System.out.println("and receive: " + packetReceive);
                    System.out.println("ar1: " + packetReceive.nextString());
                });

        SocketServer server3 = udpFactory.createOrGet("localhost", 8281, true);
        if(server3.isOpen()) {
            System.out.println("already open");
        }else {
            server3.open();
        }
    }
}
