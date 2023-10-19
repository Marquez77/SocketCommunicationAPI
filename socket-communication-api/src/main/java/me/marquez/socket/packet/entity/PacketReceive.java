package me.marquez.socket.packet.entity;

public interface PacketReceive extends ReadablePacket, Cloneable {
    PacketReceive clonePacket();
}
