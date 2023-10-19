package me.marquez.socket.udp;

import me.marquez.socket.AbstractSocketFactory;
import me.marquez.socket.data.SocketServer;

import java.net.SocketAddress;

public class UDPSocketFactory extends AbstractSocketFactory {

    @Override
    protected SocketServer createServer(SocketAddress host, boolean debug) {
        return new UDPEchoServer(host, debug);
    }
}
