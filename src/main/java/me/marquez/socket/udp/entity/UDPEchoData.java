package me.marquez.socket.udp.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UDPEchoData {
    protected static final JsonParser parser = new JsonParser();
    
    protected final List<Object> data;
    private int index;

    public Object[] getData() {
        return data.toArray(Object[]::new);
    }

    protected UDPEchoData(Object... data) {
        this.data = new LinkedList<>();
        this.data.addAll(Arrays.stream(data).collect(Collectors.toList()));
    }

    @NonNull
    public UDPEchoData append(@NonNull Object data) {
        this.data.add(data);
        return this;
    }

    @Override
    public String toString() {
        JsonArray array = new JsonArray();
        data.stream().map(Object::toString).forEach(array::add);
        return array.toString();
    }

    public boolean hasNext() {
        return index < data.size();
    }

    @Nullable
    public Object next() {
        return hasNext() ? data.get(index++) : null;
    }

    @Nullable
    public String nextString() {
        return Stream.ofNullable(next()).map(Object::toString).findAny().orElse(null);
    }

    public int nextInt() {
        return Integer.parseInt(Optional.ofNullable(nextString()).orElse("0"));
    }

    public long nextLong() {
        return Long.parseLong(Optional.ofNullable(nextString()).orElse("0"));
    }

    public double nextDouble() {
        return Double.parseDouble(Optional.ofNullable(nextString()).orElse("0"));
    }

    public boolean nextBoolean() {
        return Boolean.parseBoolean(Optional.ofNullable(nextString()).orElse("false"));
    }

    @Nullable
    public UUID nextUUID() {
        return Stream.ofNullable(nextString()).map(UUID::fromString).findAny().orElse(null);
    }
}
