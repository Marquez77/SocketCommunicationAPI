package me.marquez.socket.udp.exception;

public class NoEchoDataException extends Exception{
    public NoEchoDataException(Throwable cause) {
        super("No echo was returned to the data you sent. So 100% loss data.", cause);
    }
}
