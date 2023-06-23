package me.marquez.socket.udp;

import me.marquez.socket.udp.entity.UDPEchoResponse;
import me.marquez.socket.udp.entity.UDPEchoSend;

import java.net.InetSocketAddress;

public interface UDPMessageHandler {

    /**
     * UDP 통신 데이터 수신 시 실행
     *
     * @param client 데이터를 송신한 클라이언트
     * @param message 수신받은 데이터
     * @param response 응답으로 송신할 데이터ㄴ
     */
    void onReceive(InetSocketAddress client, UDPEchoSend send, UDPEchoResponse response);

}
