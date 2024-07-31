package me.marquez.socket;

import lombok.AllArgsConstructor;
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
    private final Map<String, List<PacketMethod>> methods;

    @Setter
    private boolean debug;

    @Setter
    private int debuggingLength = 200;

    public AbstractSocketServer(SocketAddress host, boolean debug) {
        this.host = host;
        this.listeners = new ConcurrentHashMap<>();
        this.methods = new ConcurrentHashMap<>();
        this.debug = debug;
    }

    protected void info(String s, Object o) {
        if(debug)
            SocketAPI.LOGGER.info(s, o);
    }

    protected void info(String s, Object... o) {
        if(debug) {
            SocketAPI.LOGGER.info(s, o);
        }
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

            String key = handler.identifiers().length == 0 ? "*" : handler.identifiers()[0];
            methods.computeIfAbsent(key, k -> new ArrayList<>()).add(new PacketMethod(listener, method, handler));
        }
        if(map.isEmpty())
            return;
        listeners.put(listener, map);

        methods.forEach((key, list) -> {
            list.sort(Comparator.comparingInt(o -> o.handler.priority()));
        });
    }

    private boolean checkIdentifiers(String[] first, String[] second) {
        if(first == second)
            return true;
        for(int i = 0; i < first.length; i++) {
            if(second.length == i)
                return true;
            if(first[i].equals(second[i]))
                continue;
            if(second[i].equals("*"))
                continue;
            return false;
        }
        return true;
    }

    protected void onReceive(SocketAddress socketAddress, PacketReceive receive_packet, PacketResponse response_packet) {
        String[] identifiers = receive_packet.getIdentifiers();

        String key = identifiers.length == 0 ? "*" : identifiers[0];
        if(!methods.containsKey(key))
            return;
        var list = methods.get(key);
        PacketMessage message = new PacketMessage(this, socketAddress, receive_packet.clonePacket(), response_packet);
        list.forEach(packetMethod -> {
            if(checkIdentifiers(identifiers, packetMethod.handler.identifiers())) {
                try {
                    packetMethod.method.invoke(packetMethod.listener, message);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    SocketAPI.LOGGER.error("invoke method with parameter: " + message, e.getCause());
                }
            }
        });
    }

    @AllArgsConstructor
    private static class PacketMethod {
        PacketListener listener;
        Method method;
        PacketHandler handler;
    }

}
