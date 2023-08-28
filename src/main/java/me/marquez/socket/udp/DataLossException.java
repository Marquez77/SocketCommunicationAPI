package me.marquez.socket.udp;


import lombok.Getter;

public class DataLossException extends Exception{
    @Getter
    private final double loss;

    public DataLossException(int sentLength, int receivedLength, double loss) {
        super("Client sent " + sentLength + " length data, but Server received " + receivedLength + " length data. So data loss: " + loss);
        this.loss = loss;
    }
}
