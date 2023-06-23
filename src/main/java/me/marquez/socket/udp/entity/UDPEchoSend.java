package me.marquez.socket.udp.entity;

import lombok.NonNull;

import java.util.Arrays;

public class UDPEchoSend extends UDPEchoData {

    public UDPEchoSend(Object... data) {
        super(data);
    }

    public UDPEchoSend(@NonNull String string) {
        this(Arrays.stream(string.split(", ")).map(Object.class::cast).toArray());
    }

    public UDPEchoSend clone() {
        var result = new UDPEchoSend();
        result.data.addAll(this.data);
        return result;
    }

    @NonNull
    @Override
    public UDPEchoSend append(@NonNull Object data) {
        this.data.add(data);
        return this;
    }

}
