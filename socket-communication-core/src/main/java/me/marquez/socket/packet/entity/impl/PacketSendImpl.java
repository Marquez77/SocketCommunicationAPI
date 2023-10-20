package me.marquez.socket.packet.entity.impl;

import me.marquez.socket.packet.entity.AbstractPacketData;
import me.marquez.socket.packet.entity.PacketSend;
import me.marquez.socket.packet.entity.WritablePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class PacketSendImpl extends AbstractPacketData implements PacketSend {

    public PacketSendImpl(String... identifiers) {
        super(identifiers);
    }

    @Override
    public WritablePacket setIdentifier(String... identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    @Override
    public WritablePacket append(byte[] bytes) {
        this.data.add(bytes);
        return this;
    }

    private void append(Object data) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(data);
            append(byteStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public WritablePacket append(Object... data) {
        for (Object datum : data)
            append(datum);
        return this;
    }

    @Override
    public WritablePacket append(byte b) {
        return append(new byte[] { b });
    }

    @Override
    public WritablePacket append(boolean b) {
        return append(new byte[] { (byte) (b ? 1 : 0) });
    }

    @Override
    public WritablePacket append(char c) {
        return append((byte)c);
    }

    @Override
    public WritablePacket append(short s) {
        byte[] array = new byte[Short.BYTES];
        ByteBuffer.wrap(array).putShort(s);
        return append(array);
    }

    @Override
    public WritablePacket append(int i) {
        return append(ByteBuffer.allocate(Integer.BYTES).putInt(i).array());
    }

    @Override
    public WritablePacket append(long l) {
        return append(ByteBuffer.allocate(Long.BYTES).putLong(l).array());
    }

    @Override
    public WritablePacket append(float f) {
        return append(ByteBuffer.allocate(Float.BYTES).putFloat(f).array());
    }

    @Override
    public WritablePacket append(double d) {
        return append(ByteBuffer.allocate(Double.BYTES).putDouble(d).array());
    }

    @Override
    public WritablePacket append(String str) {
        return append(str.getBytes());
    }

    @Override
    public WritablePacket append(BigInteger bi) {
        return append(bi.toByteArray());
    }

    @Override
    public WritablePacket append(BigDecimal bd) {
        return append(bd.toString());
    }

    @Override
    public WritablePacket append(UUID uuid) {
        return append(
                ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array()
        );
    }
}
