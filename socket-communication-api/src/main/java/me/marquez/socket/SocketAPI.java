package me.marquez.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SocketAPI {

    public static final Logger LOGGER = LoggerFactory.getLogger("SocketAPI");

    private static final Map<ServerProtocol, SocketFactory> factories = new HashMap<>();

    protected static void register(ServerProtocol protocol, SocketFactory factory) {
        factories.put(protocol, factory);
    }

    public static SocketFactory getFactory(ServerProtocol protocol) {
        return factories.get(protocol);
    }
}
