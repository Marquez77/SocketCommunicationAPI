import java.math.BigInteger;

public class Test {
    public static byte[] decodeUsingBigInteger(String hexString) {
        byte[] byteArray = new BigInteger(hexString, 16).toByteArray();
        if (byteArray[0] == 0) {
            byte[] output = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, output,0, output.length);
            return output;
        }
        return byteArray;
    }
    public static void main(String[] args) {
        byte[] b = decodeUsingBigInteger("aa7d77ed8ca1ae701d2ccfd258bd02fd42530bda");
        for(int i = 0; i < b.length; i++) {
            System.out.print(b[i] + " " );
        }
    }
}
