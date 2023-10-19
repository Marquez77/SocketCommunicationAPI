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
        NetworkInterface ethernet = NetworkInterface.getByInetAddress(address);
        if(ethernet == null)
            throw new IllegalArgumentException("invalid host: " + host);
        InetAddress hostAddress = ethernet.getInetAddresses().nextElement();
        return new InetSocketAddress(hostAddress, port);
    }

    protected abstract SocketServer create(SocketAddress host, boolean debug) throws UnknownHostException, SocketException;

    @Override
    public @NotNull SocketServer create(String host, int port, boolean debug) throws UnknownHostException, SocketException {
        return create(getSocketAddress(host, port), debug);
    }

    @Override
    public @NonNull SocketServer createOrGet(String host, int port, boolean debug) throws UnknownHostException, SocketException {
        return createOrGet(getSocketAddress(host, port), debug);
    }

    public @NotNull SocketServer createOrGet(SocketAddress host, boolean debug) throws UnknownHostException, SocketException {
        SocketServer result = serverMap.computeIfAbsent(host, key -> {
            try {
                return create(host, debug);
            }catch(UnknownHostException | SocketException e) {
                SocketAPI.LOGGER.trace("host: " + host, e);
                return null;
            }
        });
        if(result == null)
            throw new SocketException();
        return result;
    }

}
