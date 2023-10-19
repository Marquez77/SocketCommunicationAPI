package me.marquez.socket.packet;

import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.entity.PacketReceive;
import me.marquez.socket.packet.entity.PacketResponse;

/**
 * The packet message for handling socket communication.
 * @param server the socket server that received packet
 * @param received_packet received packet
 * @param response_packet packets to be sent in response
 */
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

