package me.marquez.socket.tcp;

import me.marquez.socket.AbstractSocketFactory;
import me.marquez.socket.data.SocketServer;

import java.net.SocketAddress;

public class TCPSocketFactory extends AbstractSocketFactory {
    @Override
    public SocketServer createServer(SocketAddress host, boolean debug) {
        return new TCPServer(host, debug);
    }
}
