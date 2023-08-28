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
//            System.out.println("receive length: " + send.toString().length());
        });
        server2.start();

        UDPEchoSend send = new UDPEchoSend();
        for(int i = 0; i < 500000; i++) {
            send.append("aaaaaaaaaaaaaaaaaaaa");
        }
        for(int i = 0; i < 100; i++) {
    //        System.out.println(send);
            System.out.println("length: " + send.toString().length());
            long start = System.currentTimeMillis();
            server.sendDataAndReceive(new InetSocketAddress("localhost", 8281), send, true)
                    .whenComplete((udpEchoResponse, throwable) -> {
                        if(throwable != null) {
                            throwable.printStackTrace();
                            return;
                        }
                        System.out.println("response: " + udpEchoResponse);
                        System.out.println("time: " + (System.currentTimeMillis()-start));
            }).exceptionally(throwable -> {
                System.out.println(throwable.getMessage());
                return null;
                    }).join();
        }
        System.exit(0);
    }
}
