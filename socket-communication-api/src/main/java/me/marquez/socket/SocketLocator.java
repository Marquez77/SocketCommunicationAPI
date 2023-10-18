package me.marquez.socket;

import java.util.HashMap;
import java.util.Map;

public class SocketLocator {

    private static final Map<ServerProtocol, SocketFactory> factories = new HashMap<>();

    public static void register(ServerProtocol protocol, SocketFactory factory) {
        factories.put(protocol, factory);
    }

    public static SocketFactory getFactory(ServerProtocol protocol) {
        return factories.get(protocol);
    }
}
