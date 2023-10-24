package me.marquez.socket.packet.entity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import java.util.function.Function;

public interface WritablePacket {
    WritablePacket setIdentifier(String... identifiers);
    WritablePacket append(Object... data);
    WritablePacket append(Function<Object, byte[]> serializeFunction, Object... data);
    WritablePacket append(byte b);
    WritablePacket append(byte[] bytes);
    WritablePacket append(boolean b);
    WritablePacket append(char c);
    WritablePacket append(short s);
    WritablePacket append(int i);
    WritablePacket append(long l);
    WritablePacket append(float f);
    WritablePacket append(double d);
    WritablePacket append(String str);
    WritablePacket append(BigInteger bi);
    WritablePacket append(BigDecimal bd);
    WritablePacket append(UUID uuid);
}
