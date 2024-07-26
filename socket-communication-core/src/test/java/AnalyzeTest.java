import me.marquez.socket.packet.entity.PacketReceiveImpl;

public class AnalyzeTest {
    public static void main(String[] args) {
        var receive = PacketReceiveImpl.of("Skript\u0017message-condition\u0017\u00026F7074696F6E733A3A6A6F696E5F64657461696C\u00176F6E\u0017C2A7612B20C2A7654F6C69766531");
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
        System.out.println(receive.nextString());
    }
}
