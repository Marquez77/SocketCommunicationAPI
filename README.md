# SocketCommunicationAPI

## UDP Communication

### Example [UDPEchoServer](src/main/java/me/marquez/socket/udp/UDPEchoServer.java)
```java
public class ServerA {
    public static void main(String[] args) {
        //Default values
        int server_port = 1234;
        Logger logger = LoggerFactory.getLogger("UDP-Server");
        
        //Create the udp server instance.
        UDPEchoServer server = new UDPEchoServer(server_port, logger);
        
        //Handling the receive messages.
        server.registerHandler((client, send, response) -> {
            //Get the message from UDPEchoSend instance.
            String first = send.nextString();
            int second = send.nextInt();
            logger.info("Received message: {}, {}", first, second);
            
            //Response the message to sender.
            response.append("Pong!");
            response.append(100);
        });
        
        //Start udp server.
        server.start();
    }
}

public class ServerB {
    public static void main(String[] args) {
        //Default values
        int server_port = 1235;
        Logger logger = LoggerFactory.getLogger("UDP-Server");

        //Create the udp server instance.
        UDPEchoServer server = new UDPEchoServer(server_port, logger);

        //Start udp server.
        server.start();
        
        //Send message to server A.
        
        //Make instance of sending packet.
        SocketAddress address = new InetSocketAddress("localhost", 1234);
        UDPEchoSend send = new UDPEchoSend();
        send.append("Ping!");
        send.append(10);
        
        //Send the message to server A.
        server.sendDataAndReceive(address, send).whenComplete((response, throwable) -> {
            if(throwable != null) {
                throwable.printStackTrace();
                return;
            }
            //Get the message from UDPEchoResponse instance.
            String first = response.nextString();
            int second = response.nextInt();
            logger.info("Response message: {}, {}", first, second);
        });
        
        //If you want just sending message:
        server.sendData(address, send).whenComplete((unused, throwable) -> {
            if(throwable != null) {
                throwable.printStackTrace();
                return;
            }
            logger.info("Send message success!");
        });
    }
}
```