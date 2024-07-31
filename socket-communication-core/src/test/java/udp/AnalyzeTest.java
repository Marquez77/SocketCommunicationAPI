package udp;

import me.marquez.socket.packet.entity.PacketReceiveImpl;

public class AnalyzeTest {
    public static void main(String[] args) {
        var receive = PacketReceiveImpl.of("VariableLink\u0017player-variables-saving-result\u0017\u00026368616E6E656C31\u001700000000");
        System.out.println(receive.nextString());
        System.out.println(receive.nextInt());
    }
}
