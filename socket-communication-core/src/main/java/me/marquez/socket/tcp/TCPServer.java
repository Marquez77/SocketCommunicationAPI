package me.marquez.socket.tcp;

import me.marquez.socket.AbstractSocketServer;
import me.marquez.socket.packet.entity.impl.PacketReceive;
import me.marquez.socket.packet.entity.impl.PacketSend;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

// TODO: 2023-10-19 implementation
public class TCPServer extends AbstractSocketServer {
    public TCPServer(SocketAddress host, boolean debug) {
        super(host, debug);
    }

    @Override
    public void open() {
        
    }

    @Override
    public void close() {

    }

    @Override
    public CompletableFuture<Void> sendDataFuture(SocketAddress address, PacketSend send_packet) {
        return null;
    }

    @Override
    public CompletableFuture<PacketReceive> sendDataAndReceive(SocketAddress address, PacketSend send_packet) {
        return null;
    }
}