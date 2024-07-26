package me.marquez.socket.tcp;

import me.marquez.socket.AbstractSocketFactory;
import me.marquez.socket.data.SocketServer;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class TCPSocketFactory extends AbstractSocketFactory {
    @Override
    public @NotNull SocketServer create(SocketAddress host, boolean debug, int threadPoolSize, int maximumQueuePerTarget) {
        return new TCPServer(host, debug);
    }
}
