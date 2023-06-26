package me.marquea.socket.udp;

import me.marquez.socket.udp.UDPEchoServer;
import me.marquez.socket.udp.entity.UDPEchoSend;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class Test {

    private static long a = 0;

    private static UDPEchoServer server_1;
    public static void main(String[] args) throws IOException, InterruptedException {
        server_1 = new UDPEchoServer(8080, LoggerFactory.getLogger("server-1"));
        server_1.registerHandler((client, send, response) -> {
            System.out.println("server-1: " + send.nextString());
        });
        server_1.start();

        UDPEchoServer server_2 = new UDPEchoServer(8081, LoggerFactory.getLogger("server-2"));
        server_2.registerHandler((client, send, response) -> {
            System.out.println("server-2: " + send.nextString());
        });
        server_2.start();

        long target = System.currentTimeMillis()+1000;
        Executors.newCachedThreadPool().submit(() -> {
            while(System.currentTimeMillis() < target) {
                run();
                break;
            }
        });
        while(System.currentTimeMillis() < target) {
            run();
            break;
        }
    }

    private static SocketAddress address = new InetSocketAddress("localhost", 8081);
    private static UDPEchoSend send = new UDPEchoSend("TEST");
    private static void run() {
        server_1.sendData(address, send);
    }
}
