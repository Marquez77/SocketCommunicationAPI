package me.marquez.socket.data;

import lombok.NonNull;

import java.net.*;

public interface SocketFactory {

    @NonNull SocketServer create(SocketAddress host, boolean debug);

    @NonNull SocketServer createOrGet(SocketAddress host, boolean debug);

    default @NonNull SocketServer create(String host, int port, boolean debug) throws UnknownHostException, SocketException {
        return create(getSocketAddress(host, port), debug);
    }

    default @NonNull SocketServer createOrGet(String host, int port, boolean debug) throws UnknownHostException, SocketException {
        return createOrGet(getSocketAddress(host, port), debug);
    }

    default SocketAddress getSocketAddress(String host, int port) throws UnknownHostException, SocketException {
        InetAddress address = host == null ? InetAddress.getLocalHost() : InetAddress.getByName(host);
        NetworkInterface ethernet = NetworkInterface.getByInetAddress(address);
        if(ethernet == null)
            throw new IllegalArgumentException("invalid host: " + host);
        InetAddress hostAddress = ethernet.getInetAddresses().nextElement();
        return new InetSocketAddress(hostAddress, port);
    }

    default SocketServer create(String host, int port) throws UnknownHostException, SocketException {
        return create(host, port, false);
    }

    default SocketServer create(int port, boolean debug) throws UnknownHostException, SocketException {
        return create(null, port, debug);
    }

    default SocketServer create(int port) throws UnknownHostException, SocketException {
        return create(port, false);
    }

}
