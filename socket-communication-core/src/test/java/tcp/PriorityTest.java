package tcp;

import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;

import java.io.IOException;

public class PriorityTest implements PacketListener {

    public static void main(String[] args) throws IOException {
        SocketManager.initialize();

        SocketServer server1 = SocketAPI.getFactory(ServerProtocol.TCP).create("localhost", 8281, true);
        SocketServer server2 = SocketAPI.getFactory(ServerProtocol.TCP).create("localhost", 8282, true);

        server1.registerListener(new FirstTest());
        server2.registerListener(new FirstTest());

        server1.open();
        server2.open();

        server2.registerListener(new PriorityTest());

        server1.sendDataFuture(server2.getHost(), SocketAPI.createPacketSend());
    }

    @PacketHandler(priority = 3)
    public void receive1(PacketMessage msg) {
        System.out.println("Received 1");
    }

    @PacketHandler(priority = 0)
    public void receive0(PacketMessage msg) {
        System.out.println("Received 0");
    }

    @PacketHandler(priority = 2)
    public void receive2(PacketMessage msg) {
        System.out.println("Received 2");
    }


}
