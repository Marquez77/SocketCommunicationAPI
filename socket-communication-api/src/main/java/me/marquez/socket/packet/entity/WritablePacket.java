package me.marquez.socket.packet.entity;

import java.io.IOException;

public interface WritablePacket {
    WritablePacket setIdentifier(String... identifiers);
    WritablePacket append(Object... data) throws IOException;
}
