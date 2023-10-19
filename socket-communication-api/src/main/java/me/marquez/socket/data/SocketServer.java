package me.marquez.socket.data;

import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.entity.PacketReceive;
import me.marquez.socket.packet.entity.PacketSend;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public interface SocketServer {

    void open() throws IOException;

    void close() throws IOException;

    void registerListener(PacketListener listener);

    CompletableFuture<Void> sendDataFuture(SocketAddress address, PacketSend send_packet);

    default void sendData(SocketAddress address, PacketSend send_packet) {
        sendDataFuture(address, send_packet).join();
    }

    CompletableFuture<PacketReceive> sendDataAndReceive(SocketAddress address, PacketSend send_packet, boolean resend);

    default CompletableFuture<PacketReceive> sendDataAndReceive(SocketAddress address, PacketSend send_packet) {
        return sendDataAndReceive(address, send_packet, false);
    }

}
