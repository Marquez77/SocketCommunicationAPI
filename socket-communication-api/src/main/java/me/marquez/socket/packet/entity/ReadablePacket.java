package me.marquez.socket.packet.entity;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public interface ReadablePacket {
    int getCursor();
    void setCursor(int cursor);

    @NonNull String[] getIdentifiers();

    boolean hasNext();
    byte[] next() throws NullPointerException, ArrayIndexOutOfBoundsException;
    byte[] prev() throws NullPointerException, ArrayIndexOutOfBoundsException;
    byte nextByte() throws NullPointerException, ArrayIndexOutOfBoundsException;
    char nextChar() throws NullPointerException, ArrayIndexOutOfBoundsException;
    short nextShort() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException;
    int nextInt() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException;
    long nextLong() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException;
    float nextFloat() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException;
    double nextDouble() throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException;

    @Nullable String nextString();
    @Nullable BigInteger nextBigInteger();
    @Nullable BigDecimal nextBigDecimal();
    @Nullable UUID nextUUID();

    @Nullable Object nextObject() throws IOException, ClassNotFoundException;
    @Nullable <T> T nextObject(Class<? extends T> clazz) throws IOException, ClassNotFoundException, ClassCastException;
}
