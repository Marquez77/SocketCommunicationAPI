package me.marquez.socket.udp;

public class NoEchoDataException extends Exception{
    NoEchoDataException() {
        super("No echo was returned to the data you sent. So 100% loss data.");
    }
}
