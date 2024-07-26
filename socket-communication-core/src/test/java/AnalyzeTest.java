import me.marquez.socket.packet.entity.PacketReceiveImpl;

public class AnalyzeTest {
    public static void main(String[] args) {
        var receive = PacketReceiveImpl.of("VariableLink\u0017player-variables\u0017\u00021D00D2153066468FBE669E9C018074A8\u0017\u00176174747269627574653A3A6174747269627574657322737472696E672C4834734941414141414141412F355752545737444942434637384C615174677838633846656F6");
        System.out.println(receive.nextUUID());
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
    }
}
