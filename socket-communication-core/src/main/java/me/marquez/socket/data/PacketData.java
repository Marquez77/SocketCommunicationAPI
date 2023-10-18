package me.marquez.socket.data;

public record PacketData(
        UDPEchoServer server,
        UDPEchoSend send,
        UDPEchoResponse response
) {}

