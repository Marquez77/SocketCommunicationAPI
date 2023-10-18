package me.marquez.socket.data;

import me.marquez.socket.udp.UDPEchoServer;
import me.marquez.socket.udp.UDPMessageHandler;
import me.marquez.socket.udp.entity.UDPEchoResponse;
import me.marquez.socket.udp.entity.UDPEchoSend;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

public class PacketExecutor implements UDPMessageHandler {

    private UDPEchoServer server;
    private final List<PacketListener> listeners = new LinkedList<>();

    public void registerHandler(PacketListener listener) {
        listeners.add(listener);
    }

    public void initialize(UDPEchoServer server) {
        this.server = server;
    }

    @Override
    public void onReceive(InetSocketAddress inetSocketAddress, UDPEchoSend udpEchoSend, UDPEchoResponse udpEchoResponse) {
        Object[] data = udpEchoSend.getData();
        PacketData info = new PacketData(server, udpEchoSend, udpEchoResponse);
        listeners.forEach(listener -> {
            for (Method method : listener.getClass().getDeclaredMethods()) {
                if(!method.isAnnotationPresent(PacketHandler.class))
                    continue;
                PacketHandler handler = method.getAnnotation(PacketHandler.class);
                String[] identifiers = handler.identifiers();
                if(checkIdentifiers(data, identifiers)) {
                    try {
                        System.out.println(method.getName());
                        info.send().setIndex(identifiers.length);
                        method.setAccessible(true);
                        method.invoke(listener, info);
                    } catch (IllegalAccessException ignored) {
                    } catch (InvocationTargetException e) {
                        e.getCause().printStackTrace();
                    }
                }
            }
        });
    }

    private boolean checkIdentifiers(Object[] data, String[] identifiers) {
        if(identifiers.length == 0) return true;
        if(data.length < identifiers.length) return false;
        for(int i = 0; i < identifiers.length; i++) {
            if(data[i] instanceof String str)
                if(!str.equalsIgnoreCase(identifiers[i])) return false;
        }
        return true;
    }
}
