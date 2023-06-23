package me.marquez.socket.udp.entity;

import lombok.NonNull;

import java.util.Arrays;

public class UDPEchoResponse extends UDPEchoData{

    public UDPEchoResponse(Object... data) {
        super(data);
    }

    public UDPEchoResponse(@NonNull String string) {
        this(Arrays.stream(string.split(", ")).map(Object.class::cast).toArray());
    }
    @NonNull
    @Override
    public UDPEchoResponse append(@NonNull Object data) {
        this.data.add(data);
        return this;
    }
}
