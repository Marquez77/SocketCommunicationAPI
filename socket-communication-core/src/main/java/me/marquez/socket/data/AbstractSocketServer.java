package me.marquez.socket.data;

import me.marquez.socket.SocketServer;
import me.marquez.socket.packet.PacketHandler;
import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.PacketMessage;
import me.marquez.socket.packet.entity.impl.PacketReceive;
import me.marquez.socket.packet.entity.impl.PacketResponse;
import me.marquez.socket.udp.UDPEchoServer;
import me.marquez.socket.udp.UDPMessageHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSocketServer implements SocketServer {

    private final Map<PacketListener, Map<Method, PacketHandler>> listeners = new ConcurrentHashMap<>();

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

    public void onReceive(InetSocketAddress inetSocketAddress, PacketReceive receive, PacketResponse response) {
        String[] identifiers = receive.getIdentifiers();
        PacketMessage message = new PacketMessage(this, receive, response);
        listeners.forEach((listener, map) -> {
            map.forEach((method, handler) -> {
                if(Arrays.compare(identifiers, handler.identifiers()) == 0) {
                        System.out.println(method.getName());
                    try {
                        method.invoke(listener, message);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.getCause().printStackTrace();
                    }
                }
            });
        });
    }

}
