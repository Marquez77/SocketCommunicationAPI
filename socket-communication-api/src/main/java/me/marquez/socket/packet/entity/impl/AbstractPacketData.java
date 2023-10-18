package me.marquez.socket.packet.entity.impl;

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
}
