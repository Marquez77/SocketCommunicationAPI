package me.marquez.socket.packet.entity;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

public abstract class AbstractPacketData {

    protected String[] identifiers;
    protected final LinkedList<byte[]> data;
    protected ListIterator<byte[]> iter;

    protected AbstractPacketData(String... identifiers) {
        this.data = new LinkedList<>();
        this.identifiers = Objects.requireNonNullElseGet(identifiers, () -> new String[0]);
    }

    protected static String encode(byte[] data) {
        char[] encoded = new char[data.length * 2];

        for(int i = 0; i < data.length; ++i) {
            encoded[2 * i] = Character.toUpperCase(Character.forDigit((data[i] & 240) >>> 4, 16));
            encoded[2 * i + 1] = Character.toUpperCase(Character.forDigit(data[i] & 15, 16));
        }

        return new String(encoded);
    }

    protected static byte[] decode(String hex) {
        byte[] decoded = new byte[hex.length() / 2];

        for(int i = 0; i < decoded.length; ++i) {
            decoded[i] = (byte)((Character.digit(hex.charAt(2 * i), 16) << 4) + Character.digit(hex.charAt(2 * i + 1), 16));
        }

        return decoded;
    }

    protected static final char STX = '\u0002';
    protected static final char ETB = '\u0017';

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        for (String identifier : identifiers) {
            string  .append(identifier)
                    .append(ETB);
        }
        string.append(STX);
        data.forEach(bytes -> {
            string  .append(encode(bytes))
                    .append(ETB);
        });
        string.setLength(string.length()-1);
        return string.toString();
    }
}
