package me.marquea.socket.udp;

import me.marquez.socket.udp.UDPEchoServer;
import me.marquez.socket.udp.entity.UDPEchoSend;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BigDataTest {
    public static void main(String[] args) throws IOException {
        UDPEchoServer server = new UDPEchoServer(8280, LoggerFactory.getLogger("server1"));
//        server.setDebug(true);
        server.start();

        UDPEchoServer server2 = new UDPEchoServer(8281, LoggerFactory.getLogger("server2"));
//        server2.setDebug(true);
        server2.registerHandler((client, send, response) -> {
            System.out.println(send.toString().length());
        });
        server2.start();

        UDPEchoSend send = new UDPEchoSend();
        for(int i = 0; i < 10000; i++) {
            send.append("aaaaaaaaaaaaaaaaaaaa");
        }
//        System.out.println(send);
        System.out.println(send.toString().length());
        server.sendDataAndReceive(new InetSocketAddress("localhost", 8281), send)
                .whenComplete((udpEchoResponse, throwable) -> {
                    if(throwable != null) {
                        throwable.printStackTrace();
                        return;
                    }
                    System.out.println(udpEchoResponse);
                    System.exit(0);
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
                });
    }
}
