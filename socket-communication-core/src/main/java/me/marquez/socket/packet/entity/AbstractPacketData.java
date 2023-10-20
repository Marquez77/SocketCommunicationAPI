package me.marquez.socket.packet.entity;

import me.marquez.socket.utils.SerializeUtil;

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
            string  .append(SerializeUtil.encode(bytes))
                    .append(ETB);
        });
        if(string.charAt(string.length()-1) != STX)
            string.setLength(string.length()-1);
        return string.toString();
    }
}
