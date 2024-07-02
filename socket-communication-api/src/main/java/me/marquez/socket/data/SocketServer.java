package me.marquez.socket.data;

import me.marquez.socket.packet.PacketListener;
import me.marquez.socket.packet.entity.PacketReceive;
import me.marquez.socket.packet.entity.PacketSend;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public interface SocketServer {

    /**
     * @param debug whether print debugging
     */
    void setDebug(boolean debug);

    /**
     * @param length length of debugging message when print
     */
    void setDebuggingLength(int length);

    /**
     * @return host of this socket server
     */
    SocketAddress getHost();

    /**
     * Open socket server
     * @throws IOException something wrong on opening server.
     */
    void open() throws IOException;

    /**
     * @return whether the socket server is open
     */
    boolean isOpen();

    /**
     * Close socket server
     * @throws IOException something wrong on closing server.
     */
    void close() throws IOException;

    /**
     * Register packet listener
     * The method with {@link me.marquez.socket.packet.PacketHandler} in registered listener will process when receive packet.
     * @param listener listener to register
     */
    void registerListener(PacketListener listener);

    /**
     * Send packet to other socket server.
     * @param address target host to send packet
     * @param send_packet packet body
     * @return the future will complete when receive response from target host.
     */
    CompletableFuture<Boolean> sendDataFuture(SocketAddress address, PacketSend send_packet);

    /**
     * Send packet to other socket server and wait until receive response from target host.
     * @param address target host to send packet
     * @param send_packet packet body
     */
    default void sendData(SocketAddress address, PacketSend send_packet) {
        try {
            sendDataFuture(address, send_packet).join();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send packet to other socket server and receive response body from target host.
     * @param address target host to send packet
     * @param send_packet packet body
     * @param resend whether resend packet on fail
     * @return the future with response
     */
    CompletableFuture<PacketReceive> sendDataAndReceive(SocketAddress address, PacketSend send_packet, boolean resend);

    /**
     * Send packet to other socket server and receive response body from target host.
     * @param address target host to send packet
     * @param send_packet packet body
     * @return the future with response
     */
    default CompletableFuture<PacketReceive> sendDataAndReceive(SocketAddress address, PacketSend send_packet) {
        return sendDataAndReceive(address, send_packet, false);
    }

}
