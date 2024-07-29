import me.marquez.socket.packet.entity.PacketReceiveImpl;

public class AnalyzeTest {
    public static void main(String[] args) {
        var receive = PacketReceiveImpl.of("VariableLink\u0017system-variables\u0017\u0002636F75706F6E\u0017636F75706F6E3A3A372E32362C32ECB0A8ECA090EAB2803A3A726577617264733A3A3334\u0017226E756C6C");
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
    }
}
