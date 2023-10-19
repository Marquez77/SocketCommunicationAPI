package me.marquez.socket;

import me.marquez.socket.data.SocketFactory;
import me.marquez.socket.data.SocketServer;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSocketFactory implements SocketFactory {

    private final Map<SocketAddress, SocketServer> serverMap = new ConcurrentHashMap<>();

    @Override
    public SocketServer createOrGet(SocketAddress host, boolean debug) {
        return serverMap.getOrDefault(host, create(host, debug));
    }

    protected abstract SocketServer createServer(SocketAddress host, boolean debug);

    @Override
    public SocketServer create(SocketAddress host, boolean debug) {
        SocketServer server = createServer(host, debug);
        serverMap.put(host, server);
        return server;
    }
}
