package me.marquez.socket.packet.entity;

import me.marquez.socket.utils.SerializeUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Function;

public class PacketSendImpl extends AbstractPacketData implements PacketSend {

    public PacketSendImpl(String... identifiers) {
        super(identifiers);
    }

    @Override
    public String[] getIdentifiers() {
        return this.identifiers;
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

    private void append(Function<Object, byte[]> serializeFunction, Object data) {
        if(data instanceof Byte b) {
            append((byte)b);
        }else if(data instanceof Boolean b) {
            append((boolean)b);
        }else if(data instanceof Character c) {
            append((char)c);
        }else if(data instanceof Short s) {
            append((short)s);
        }else if(data instanceof Integer i) {
            append((int)i);
        }else if(data instanceof Long l) {
            append((long)l);
        }else if(data instanceof Float f) {
            append((float)f);
        }else if(data instanceof Double d) {
            append((double)d);
        }else if(data instanceof String str) {
            append(str);
        }else if(data instanceof BigInteger bi) {
            append(bi);
        }else if(data instanceof BigDecimal bd) {
            append(bd);
        }else if(data instanceof UUID uuid) {
            append(uuid);
        }else {
            append(serializeFunction.apply(data));
        }
    }

    @Override
    public WritablePacket append(Object... data) {
        for (Object datum : data)
            append(SerializeUtil::objectToByteArray, datum);
        return this;
    }

    @Override
    public WritablePacket append(Function<Object, byte[]> serializeFunction, Object... data) {
        for (Object datum : data)
            append(serializeFunction, datum);
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
