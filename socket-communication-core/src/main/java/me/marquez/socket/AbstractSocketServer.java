package me.marquez.socket;

import lombok.Getter;
import lombok.Setter;
import me.marquez.socket.data.SocketServer;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;
import me.marquez.socket.packet.entity.PacketReceive;
import me.marquez.socket.packet.entity.PacketResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSocketServer implements SocketServer {

    @Getter
    protected final SocketAddress host;
    private final Map<PacketListener, Map<Method, PacketHandler>> listeners;

    @Setter
    private boolean debug;

    @Setter
    private int debuggingLength = 100;

    public AbstractSocketServer(SocketAddress host, boolean debug) {
        this.host = host;
        this.listeners = new ConcurrentHashMap<>();
        this.debug = debug;
    }

    protected void info(String s, Object o) {
        if(debug)
            SocketAPI.LOGGER.info(s, o);
    }

    protected void info(String s, Object... o) {
        if(debug)
            SocketAPI.LOGGER.info(s, o);
    }

    protected void error(Throwable e) {
        SocketAPI.LOGGER.error("", e);
    }

    protected String trim(String str) {
        return str.substring(0, Math.min(debuggingLength, str.length()));
    }

    @Override
    public void registerListener(PacketListener listener) {
        Map<Method, PacketHandler> map = new HashMap<>();
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if(!method.isAnnotationPresent(PacketHandler.class))
                continue;
            PacketHandler handler = method.getAnnotation(PacketHandler.class);
            method.setAccessible(true);
            map.put(method, handler);
        }
        if(map.isEmpty())
            return;
        listeners.put(listener, map);
    }

    protected void onReceive(SocketAddress socketAddress, PacketReceive receive_packet, PacketResponse response_packet) {
        String[] identifiers = receive_packet.getIdentifiers();
        listeners.forEach((listener, map) -> {
            map.forEach((method, handler) -> {
                if(Arrays.compare(identifiers, handler.identifiers()) == 0) {
                    SocketAPI.LOGGER.info("Execute packet handler: {}#{}", listener.getClass().getName(), method.getName());
                    PacketMessage message = new PacketMessage(this, socketAddress, receive_packet.clonePacket(), response_packet);
                    try {
                        method.invoke(listener, message);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        SocketAPI.LOGGER.error("invoke method with parameter: " + message, e.getCause());
                    }
                }
            });
        });
    }

}
