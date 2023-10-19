package me.marquez.socket;

import lombok.NonNull;
import lombok.Setter;
import me.marquez.socket.data.ServerProtocol;
import me.marquez.socket.data.SocketFactory;
import me.marquez.socket.packet.entity.PacketSend;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SocketAPI {

    public static final Logger LOGGER = LoggerFactory.getLogger("SocketAPI");

    private static final Map<ServerProtocol, SocketFactory> factories = new HashMap<>();

    @Setter
    private static Function<String[], PacketSend> createFunction;

    protected static void register(ServerProtocol protocol, SocketFactory factory) {
        factories.put(protocol, factory);
    }

    @Nullable
    public static SocketFactory getFactory(ServerProtocol protocol) {
        return factories.getOrDefault(protocol, null);
    }

    public static PacketSend createPacketSend(@NonNull String... identifiers) {
        if(createFunction == null) {
            throw new RuntimeException("SocketAPI does not initialized!");
        }
        return createFunction.apply(identifiers);
    }

    public static PacketSend createPacketSend() {
        return createPacketSend(new String[0]);
    }
}
