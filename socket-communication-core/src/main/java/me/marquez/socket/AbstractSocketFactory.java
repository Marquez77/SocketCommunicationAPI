package me.marquez.socket;

import me.marquez.socket.data.SocketFactory;
import me.marquez.socket.data.SocketServer;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSocketFactory implements SocketFactory {

    private final Map<SocketAddress, SocketServer> serverMap = new ConcurrentHashMap<>();

    @Override
    public @NotNull SocketServer createOrGet(SocketAddress host, boolean debug) {
        return serverMap.computeIfAbsent(host, key -> {
            return create(host, debug);
        });
    }

}
