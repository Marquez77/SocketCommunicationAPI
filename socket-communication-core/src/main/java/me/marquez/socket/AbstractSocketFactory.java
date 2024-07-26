package me.marquez.socket;

import lombok.NonNull;
import me.marquez.socket.data.SocketFactory;
import me.marquez.socket.data.SocketServer;
import org.jetbrains.annotations.NotNull;

import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSocketFactory implements SocketFactory {

    private final Map<SocketAddress, SocketServer> serverMap = new ConcurrentHashMap<>();

    private SocketAddress getSocketAddress(String host, int port) throws UnknownHostException, SocketException {
        InetAddress address = host == null ? InetAddress.getLocalHost() : InetAddress.getByName(host);
        InetAddress hostAddress = address;
        if(!address.isAnyLocalAddress()) {
            NetworkInterface ethernet = NetworkInterface.getByInetAddress(address);
            if (ethernet == null)
                throw new IllegalArgumentException("invalid host: " + host);
            hostAddress = ethernet.getInetAddresses().nextElement();
        }
        return new InetSocketAddress(hostAddress, port);
    }

    protected abstract SocketServer create(SocketAddress host, boolean debug, int threadPoolSize, int maximumQueuePerTarget) throws UnknownHostException, SocketException;

    @Override
    public @NotNull SocketServer create(String host, int port, boolean debug, int threadPoolSize, int maximumQueuePerTarget) throws UnknownHostException, SocketException {
        return create(getSocketAddress(host, port), debug, threadPoolSize, maximumQueuePerTarget);
    }

    @Override
    public @NonNull SocketServer createOrGet(String host, int port, boolean debug, int threadPoolSize, int maximumQueuePerTarget) throws UnknownHostException, SocketException {
        return createOrGet(getSocketAddress(host, port), debug, threadPoolSize, maximumQueuePerTarget);
    }

    public @NotNull SocketServer createOrGet(SocketAddress host, boolean debug, int threadPoolSize, int maximumQueuePerTarget) throws UnknownHostException, SocketException {
        SocketServer result = serverMap.compute(host, (key, value) -> {
            if(value == null || !value.isOpen()) {
                try {
                    return create(host, debug, threadPoolSize, maximumQueuePerTarget);
                } catch (UnknownHostException | SocketException e) {
                    SocketAPI.LOGGER.error("host: " + host, e);
                    return null;
                }
            }
            return value;
        });
        if(result == null)
            throw new SocketException();
        return result;
    }

}
