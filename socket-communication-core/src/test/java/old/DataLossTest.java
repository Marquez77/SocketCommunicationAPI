//import me.marquez.socket.udp.UDPEchoServer;
//import me.marquez.socket.udp.entity.UDPEchoSend;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.net.InetSocketAddress;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//public class DataLossTest {
//    public static void main(String[] args) throws Exception{
//        File file = new File("body.txt");
//        String body = file.exists() ? Files.readString(file.toPath(), StandardCharsets.UTF_8) : null;
//        Logger logger = LoggerFactory.getLogger("server");
//
//        logger.info("Set body={}", body);
//
//        if(args.length == 0) {
//            System.out.println("Need to specify host and port or server port");
//            return;
//        }else if(args.length < 3) { //receive server
//            int port = Integer.parseInt(args[0]);
//            UDPEchoServer server = new UDPEchoServer(port, logger);
//            server.setDebug(!args[args.length-1].equals("-log"));
//            Map<InetSocketAddress, Set<Integer>> map = new HashMap<>();
//            server.registerHandler((client, send, response) -> {
//                int i = send.nextInt();
//                var set = map.getOrDefault(client, new HashSet<>());
//                set.add(i);
//                map.put(client, set);
//                response.append(i);
//                if(send.hasNext()) response.append(send.nextString());
//                if(i == -1) {
//                    logger.info("Received: {}", set.size()-1);
//                    set.clear();
//                }
//            });
//            server.start();
//        }else if(args.length < 6) { //sending server
//            int port = Integer.parseInt(args[0]);
//            String target_host = args[1];
//            int target_port = Integer.parseInt(args[2]);
//            int count = Integer.parseInt(args[3]);
//            UDPEchoServer server = new UDPEchoServer(port, logger);
//            server.setDebug(!args[args.length-1].equals("-log"));
//            server.start();
//            Set<Integer> set = new HashSet<>();
//            InetSocketAddress address = new InetSocketAddress(target_host, target_port);
//            long start = System.currentTimeMillis();
//            for(int i = 0; i < count; i++) {
//                UDPEchoSend send = new UDPEchoSend(i);
//                if(body != null) send.append(body);
//                server.sendDataAndReceive(address, send).whenComplete((udpEchoResponse, throwable) -> {
//                    if(throwable == null) {
//                        set.add(udpEchoResponse.nextInt());
//                    }
//                }).orTimeout(100000, TimeUnit.MILLISECONDS).join();
//            }
//            long stop = System.currentTimeMillis();
//            Thread.sleep(1000);
//            server.sendData(address, new UDPEchoSend(-1));
//            logger.info("Send: {}", count);
//            logger.info("Response: {}", set.size());
//            logger.info("Loss: {}%", (double)(count - set.size())/count*100);
//            logger.info("Time: {}ms", stop-start);
//            System.exit(0);
//        }else {
//            System.out.println("Invalid arguments");
//        }
//    }
//}
