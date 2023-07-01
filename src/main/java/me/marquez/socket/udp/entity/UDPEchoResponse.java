package me.marquez.socket.udp.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.NonNull;

public class UDPEchoResponse extends UDPEchoData{

    public UDPEchoResponse(Object... data) {
        super(data);
    }

    public static UDPEchoResponse of(String json) {
        JsonArray array = parser.parse(json).getAsJsonArray();
        UDPEchoResponse response = new UDPEchoResponse();
        array.forEach(jsonElement -> response.append(jsonElement.getAsString()));
        return response;
    }

    @NonNull
    @Override
    public UDPEchoResponse append(@NonNull Object data) {
        this.data.add(data);
        return this;
    }
}
