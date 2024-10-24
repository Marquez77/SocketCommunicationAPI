package udp;

import me.marquez.socket.packet.entity.PacketReceiveImpl;

public class AnalyzeTest {
    public static void main(String[] args) {
        var receive = PacketReceiveImpl.of("VariableLink\u0017player-variables\u0017\u0002AA4AD28BA95A476988C6E2FE294051CD\u0017696E76656E746F72793A3A61726D6F723A3A68656C6D6574\u0017AA4AD28BA95A476988C6E2FE294051CD\u0017696E76656E746F72793A3A61726D6F723A3A68656C6D6574\u0017226E7");
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());

    }
}
