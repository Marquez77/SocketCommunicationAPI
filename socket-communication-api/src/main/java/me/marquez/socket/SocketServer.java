package me.marquez.socket;

import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.entity.impl.PacketReceive;
import me.marquez.socket.packet.entity.impl.PacketSend;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public interface SocketServer {

    void open();

    void close();

    void registerListener(PacketListener listener);

    CompletableFuture<Void> sendDataFuture(SocketAddress address, PacketSend send_packet);

    default void sendData(SocketAddress address, PacketSend send_packet) {
        sendDataFuture(address, send_packet).join();
    }

    CompletableFuture<PacketReceive> sendDataAndReceive(SocketAddress address, PacketSend send_packet);

}
