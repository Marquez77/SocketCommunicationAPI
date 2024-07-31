package tcp;

import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;

public class FirstTest implements PacketListener {

    public static void main(String[] args) throws Exception{
        SocketManager.initialize();

        SocketServer server1 = SocketAPI.getFactory(ServerProtocol.TCP).create("localhost", 8281, true);
        SocketServer server2 = SocketAPI.getFactory(ServerProtocol.TCP).create("localhost", 8282, true);

        server1.registerListener(new FirstTest());
        server2.registerListener(new FirstTest());

        server1.open();
//        server2.open();

        Thread.sleep(1000);

        var send = SocketAPI.createPacketSend("TEST");
        send.appendString("HEllo");
        send.appendInt(1234);

        System.out.println("result: " + server1.sendDataFuture(server2.getHost(), send).join());

        System.out.println("result: " + server2.sendDataFuture(server1.getHost(), send).join());
    }

    @PacketHandler(identifiers = "TEST")
    public void onReceive(PacketMessage message) {
        var received = message.received_packet();
        System.out.println("Received from " + message.origin_server_address() + ": " + received.nextString() + " " + received.nextInt());
    }

}
