package me.marquez.socket.packet;

import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.entity.PacketReceive;
import me.marquez.socket.packet.entity.PacketResponse;

public record PacketMessage(
        SocketServer server,
        PacketReceive received_packet,
        PacketResponse response_packet
) {
    @Override
    public String toString() {
        return String.format("{received_packet=%s, response_packet=%s}", received_packet, response_packet);
    }
}

