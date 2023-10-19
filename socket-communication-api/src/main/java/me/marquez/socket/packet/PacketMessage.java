package me.marquez.socket.packet;

import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.entity.impl.PacketReceive;
import me.marquez.socket.packet.entity.impl.PacketResponse;

// TODO: 2023-10-19 override toString 
public record PacketMessage(
        SocketServer server,
        PacketReceive received_packet,
        PacketResponse response_packet
) {}

