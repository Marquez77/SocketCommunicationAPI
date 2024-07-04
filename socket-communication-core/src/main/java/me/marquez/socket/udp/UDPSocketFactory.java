package me.marquez.socket.udp;

import me.marquez.socket.AbstractSocketFactory;
import me.marquez.socket.data.SocketServer;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class UDPSocketFactory extends AbstractSocketFactory {

    @Override
    public @NotNull SocketServer create(SocketAddress host, boolean debug, int threadPoolSize) {
        return new UDPEchoServer(host, debug, threadPoolSize);
    }
}
