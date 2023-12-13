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

    default WritablePacket appendByte(Object b) {
        return append((byte)b);
    }
    default WritablePacket appendBytes(Object bytes) {
        return append((byte[])bytes);
    }
    default WritablePacket appendChar(Object c) {
        return append((char)c);
    }
    default WritablePacket appendShort(Object s) {
        return append((short)s);
    }
    default WritablePacket appendInt(Object i) {
        if(i instanceof Number n)
            return append(n.intValue());
        return append((int)i);
    }
    default WritablePacket appendLong(Object l) {
        if(l instanceof Number n)
            return append(n.longValue());
        return append((long)l);
    }
    default WritablePacket appendFloat(Object f) {
        if(f instanceof Number n)
            return append(n.floatValue());
        return append((float)f);
    }
    default WritablePacket appendDouble(Object d) {
        if(d instanceof Number n)
            return append(n.doubleValue());
        return append((double)d);
    }
    default WritablePacket appendString(Object s) {
        if(s instanceof String str)
            return append(str);
        return append(s.toString());
    }
    default WritablePacket appendUUID(Object uuid) {
        if(uuid instanceof UUID uuidInstance)
            return append(uuidInstance);
        return append(UUID.fromString(uuid.toString()));
    }
}
