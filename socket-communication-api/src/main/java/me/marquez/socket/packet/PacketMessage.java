package me.marquez.socket.packet;

import me.marquez.socket.SocketServer;
import me.marquez.socket.packet.entity.impl.PacketReceive;
import me.marquez.socket.packet.entity.impl.PacketResponse;

public record PacketMessage(
        SocketServer server,
        PacketReceive received_packet,
        PacketResponse response_packet
) {}

