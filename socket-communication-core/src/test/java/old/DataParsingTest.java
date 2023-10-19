//import me.marquez.socket.udp.UDPEchoServer;
//import me.marquez.socket.udp.entity.UDPEchoSend;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//
//public class DataParsingTest {
//    public static void main(String[] args) throws IOException {
//        UDPEchoServer server = new UDPEchoServer(8080, LoggerFactory.getLogger("server1"));
//        server.registerHandler((client, send, response) -> {
//            System.out.println("server1: first " + send.nextString());
//            System.out.println("server1: second " + send.nextString());
//        });
//        server.start();
//
//        UDPEchoServer server2 = new UDPEchoServer(8081, LoggerFactory.getLogger("server2"));
//        server2.registerHandler((client, send, response) -> {
//            System.out.println("server2: first " + send.nextString());
//            System.out.println("server2: second " + send.nextString());
//        });
//        server2.start();
//
//        server.sendDataAndReceive(new InetSocketAddress("localhost", 8081), new UDPEchoSend("TEST, 1234", "1234, 3456"));
//    }
//}
