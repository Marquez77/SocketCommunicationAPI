package me.marquez.socket.udp.entity;

import com.google.gson.JsonArray;
import lombok.NonNull;

public class UDPEchoSend extends UDPEchoData {

    public UDPEchoSend(Object... data) {
        super(data);
    }

    public static UDPEchoSend of(String json) {
        JsonArray array = parser.parse(json).getAsJsonArray();
        UDPEchoSend send = new UDPEchoSend();
        array.forEach(jsonElement -> send.append(jsonElement.getAsString()));
        return send;
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
