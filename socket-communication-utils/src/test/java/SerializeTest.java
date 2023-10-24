import me.marquez.socket.utils.SerializeUtil;

public class SerializeTest {
    public static void main(String[] args) {
        Object value = 1;
        byte[] bytes = SerializeUtil.objectToByteArray(value);
        System.out.println("byte length: " + bytes.length);
        String hex = SerializeUtil.encode(bytes);
        System.out.println("hex length: " + hex.length());
    }
}
