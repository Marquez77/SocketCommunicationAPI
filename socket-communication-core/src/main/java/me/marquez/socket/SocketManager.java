package me.marquez.socket;

import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.packet.entity.PacketSendImpl;
import me.marquez.socket.tcp.TCPSocketFactory;
import me.marquez.socket.udp.UDPSocketFactory;

public class SocketManager {

    public static void initialize() {
        SocketAPI.register(ServerProtocol.TCP, new TCPSocketFactory());
        SocketAPI.register(ServerProtocol.UDP, new UDPSocketFactory());

        SocketAPI.setCreateFunction(PacketSendImpl::new);

    }

}
