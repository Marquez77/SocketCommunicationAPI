import me.marquez.socket.packet.entity.PacketReceiveImpl;

public class AnalyzeTest {
    public static void main(String[] args) {
        var receive = PacketReceiveImpl.of("\u0002ACED00057E72002F6D652E6D61727175657A2E6368616E6E656C6D616E616765722E6170692E736F636B65742E53796E63526573756C7400000000000000001200007872000E6A6176612E6C616E672E456E756D0000000000000000120000787074000");
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
    }
}
