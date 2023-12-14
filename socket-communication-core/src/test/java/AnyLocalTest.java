import me.marquez.socket.SocketAPI;
import me.marquez.socket.SocketManager;
import me.marquez.socket.data.ServerProtocol;

import java.net.SocketException;
import java.net.UnknownHostException;

public class AnyLocalTest {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        SocketManager.initialize();
        SocketAPI.getFactory(ServerProtocol.UDP).createOrGet("0.0.0.0", 8182);
    }
}
