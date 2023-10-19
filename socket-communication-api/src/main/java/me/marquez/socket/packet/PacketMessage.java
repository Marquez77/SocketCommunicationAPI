package me.marquez.socket.packet;

import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.entity.PacketReceive;
import me.marquez.socket.packet.entity.PacketResponse;

import java.net.SocketAddress;

/**
 * The packet message for handling socket communication.
 * @param local_server the socket server that received packet
 * @param origin_server_address the socket server address that sent packet
 * @param received_packet received packet
 * @param response_packet packets to be sent in response
 */
public record PacketMessage(
        SocketServer local_server,
        SocketAddress origin_server_address,
        PacketReceive received_packet,
        PacketResponse response_packet
) {
    @Override
    public String toString() {
        return String.format("{origin_server_address=%s, received_packet=%s, response_packet=%s}", origin_server_address, received_packet, response_packet);
    }
}

