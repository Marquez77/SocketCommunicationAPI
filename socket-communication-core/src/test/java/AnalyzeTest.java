import me.marquez.socket.packet.entity.PacketReceiveImpl;

public class AnalyzeTest {
    public static void main(String[] args) {
        var receive = PacketReceiveImpl.of("VariableLink\u0017system-variables\u0017\u00026B61726D61\u00176576656E74735F776F726C64\u0017226E756C6C");
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
    }
}
