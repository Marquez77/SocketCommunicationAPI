package me.marquez.socket.udp.exception;


import lombok.Getter;

@Getter
public class DataLossException extends Exception{
    private final double loss;

    public DataLossException(int sentLength, int receivedLength, double loss) {
        super("Client sent " + sentLength + " length data, but Server received " + receivedLength + " length data. So data loss: " + loss);
        this.loss = loss;
    }
}
