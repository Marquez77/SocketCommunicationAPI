package me.marquez.socket.packet.entity.impl;

import me.marquez.socket.packet.entity.WritablePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class PacketSend extends AbstractPacketData implements WritablePacket {

    public PacketSend(String... identifiers) {
        super(identifiers);
    }

    @Override
    public WritablePacket setIdentifier(String... identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    private void append(Object data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(data);
            this.data.add(byteStream.toByteArray());
        }
    }

    @Override
    public WritablePacket append(Object... data) throws IOException {
        for (Object datum : data)
            append(datum);
        return this;
    }
}
