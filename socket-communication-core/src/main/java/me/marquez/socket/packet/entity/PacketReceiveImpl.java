package me.marquez.socket.packet.entity;

import me.marquez.socket.utils.SerializeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

public class PacketReceiveImpl extends AbstractPacketData implements PacketReceive {

    public PacketReceiveImpl(String... identifiers) {
        super(identifiers);
    }

    @NotNull
    @Override
    public String[] getIdentifiers() {
        return identifiers;
    }

    @Override
    public @Nullable Object nextObject() throws IOException, ClassNotFoundException {
        try {
            return SerializeUtil.byteArrayToObject(next());
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public <T> @Nullable T nextObject(Class<? extends T> clazz) throws IOException, ClassNotFoundException, ClassCastException {
        return Optional.ofNullable(nextObject())
                .map(clazz::cast)
                .orElse(null);
    }

    @Override
    public @Nullable Object nextKnownObject(Class<?> clazz) throws IOException, ClassNotFoundException {
        if(clazz.equals(Byte.class)) {
            return nextByte();
        }else if(clazz.equals(Boolean.class)) {
            return nextBoolean();
        }else if(clazz.equals(Character.class)) {
            return nextChar();
        }else if(clazz.equals(Short.class)) {
            return nextShort();
        }else if(clazz.equals(Integer.class)) {
            return nextInt();
        }else if(clazz.equals(Long.class)) {
            return nextLong();
        }else if(clazz.equals(Float.class)) {
            return nextFloat();
        }else if(clazz.equals(Double.class)) {
            return nextDouble();
        }else if(clazz.equals(String.class)) {
            return nextString();
        }else if(clazz.equals(BigInteger.class)) {
            return nextBigInteger();
        }else if(clazz.equals(BigDecimal.class)) {
            return nextBigDecimal();
        }else if(clazz.equals(UUID.class)) {
            return nextUUID();
        }else {
            return nextObject(clazz);
        }
    }

    @Override
    public boolean hasNext() {
        if(iter == null) iter = data.listIterator();
        return iter.hasNext();
    }

    @Override
    public byte[] next() throws NullPointerException, ArrayIndexOutOfBoundsException {
        if(!hasNext())
            throw new ArrayIndexOutOfBoundsException("no more next data.");
        return iter.next();
    }

    @Override
    public byte[] prev() throws NullPointerException, ArrayIndexOutOfBoundsException {
        if(iter == null) throw new NullPointerException("previous data does not exist.");
        return iter.previous();
    }

    @Override
    public int getCursor() {
        return iter.nextIndex();
    }

    @Override
    public void setCursor(int cursor) {
        iter = data.listIterator(cursor);
    }

    @Override
    public byte nextByte() throws NullPointerException {
        return next()[0];
    }

    @Override
    public boolean nextBoolean() throws NullPointerException, ArrayIndexOutOfBoundsException {
        return next()[0] != 0;
    }

    @Override
    public char nextChar() throws NullPointerException, ArrayIndexOutOfBoundsException {
        return (char)next()[0];
    }

    @Override
    public short nextShort() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException {
        return ByteBuffer.wrap(next()).getShort();
    }

    @Override
    public int nextInt() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException {
        return ByteBuffer.wrap(next()).getInt();
    }

    @Override
    public long nextLong() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException {
        return ByteBuffer.wrap(next()).getLong();
    }

    @Override
    public float nextFloat() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException {
        return ByteBuffer.wrap(next()).getFloat();
    }

    @Override
    public double nextDouble() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException {
        return ByteBuffer.wrap(next()).getDouble();
    }

    @Override
    public @Nullable String nextString() {
        try {
            return new String(next());
        } catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public @Nullable BigInteger nextBigInteger() {
        try {
            return new BigInteger(next());
        } catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public @Nullable BigDecimal nextBigDecimal() {
        return Optional.ofNullable(nextString()).map(BigDecimal::new).orElse(null);
    }

    @Override
    public @Nullable UUID nextUUID() {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(next());
            long most = buffer.getLong();
            long least = buffer.getLong();
            return new UUID(most, least);
        } catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public PacketReceive clonePacket() {
        try {
            return (PacketReceive) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public static PacketReceiveImpl of(String string) {
        String[] split = string.split(String.valueOf(STX), 2);
        String[] identifiers = split[0].split(String.valueOf(ETB));
        if(identifiers.length == 1 && identifiers[0].isEmpty())
            identifiers = new String[0];
        PacketReceiveImpl packet = new PacketReceiveImpl(identifiers);
        for (String s : split[1].split(String.valueOf(ETB))) {
            packet.data.add(SerializeUtil.decode(s));
        }
        return packet;
    }

    @Override
    public PacketSend toSendPacket() {
        PacketSendImpl send = new PacketSendImpl(identifiers);
        send.data.addAll(this.data);
        return send;
    }
}
