package me.marquez.socket.packet.entity;

public interface PacketReceive extends ReadablePacket, Cloneable {
    /**
     * Clone the packet instance with deep copy.
     * @return copied packet instance
     */
    PacketReceive clonePacket();

    /**
     * Convert to send packet
     * @return send packet instance
     */
    PacketSend toSendPacket();
}
