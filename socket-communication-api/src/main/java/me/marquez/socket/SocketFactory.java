package me.marquez.socket;

import me.marquez.socket.packet.PacketHandler;

import java.net.*;

public interface SocketFactory {

    SocketServer createSocketServer(SocketAddress host, boolean debug);

    default SocketServer create(String host, int port, boolean debug) throws UnknownHostException, SocketException {
        InetAddress address = host == null ? InetAddress.getLocalHost() : InetAddress.getByName(host);
        NetworkInterface ethernet = NetworkInterface.getByInetAddress(address);
        if(ethernet == null)
            throw new IllegalArgumentException("invalid host: " + host);
        InetAddress hostAddress = ethernet.getInetAddresses().nextElement();
        SocketAddress socketAddress = new InetSocketAddress(hostAddress, port);
        return createSocketServer(socketAddress, debug);
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
