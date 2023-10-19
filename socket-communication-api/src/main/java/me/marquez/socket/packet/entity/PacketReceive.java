package me.marquez.socket.packet.entity;

public interface PacketReceive extends ReadablePacket, Cloneable {
    /**
     * Clone the packet instance with deep copy.
     * @return copied packet instance
     */
    PacketReceive clonePacket();
}
