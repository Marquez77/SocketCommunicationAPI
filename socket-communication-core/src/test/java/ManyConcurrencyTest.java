import me.marquez.socket.udp.UDPEchoServer;
import me.marquez.socket.udp.entity.UDPEchoSend;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ManyConcurrencyTest {
    public static void main(String[] args) throws IOException {
        UDPEchoServer server = new UDPEchoServer(8280, LoggerFactory.getLogger("server1"));
//        server.setDebug(true);
        server.start();

        UDPEchoServer server2 = new UDPEchoServer(8281, LoggerFactory.getLogger("server2"));
//        server2.setDebug(true);
        server2.registerHandler((client, send, response) -> {
            System.out.println("receive: " + send.toString());
        });
        server2.start();

        InetSocketAddress host = new InetSocketAddress("localhost", 8281);
        UDPEchoSend send = new UDPEchoSend("TEST");
        for(int i = 0; i < 10; i++) {
            server.sendData(host, send);
        }
    }
}
