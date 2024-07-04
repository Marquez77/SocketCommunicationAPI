package me.marquez.socket.data;

import lombok.NonNull;

import java.net.*;

public interface SocketFactory {

    /**
     * Create new socket server.
     * @param host The server hostname
     * @param port The server port
     * @param debug whether print debugging
     * @param threadPoolSize The size of thread pool
     * @return created socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException if the server host is already bind or else case.
     */
    @NonNull SocketServer create(String host, int port, boolean debug, int threadPoolSize) throws UnknownHostException, SocketException;

    /**
     * Create new socket server.
     * @param host The server hostname
     * @param port The server port
     * @param debug whether print debugging
     * @return created socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException if the server host is already bind or else case.
     */
    default @NonNull SocketServer create(String host, int port, boolean debug) throws UnknownHostException, SocketException {
        return create(host, port, debug, 10);
    }

    /**
     * Create new socket server without debugging.
     * @param host The server host
     * @param port The server port
     * @return created socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException if the server host is already bind or else case.
     */
    default @NonNull SocketServer create(String host, int port) throws UnknownHostException, SocketException {
        return create(host, port, false);
    }

    /**
     * Create new socket server on localhost.
     * @param port The server port
     * @param debug whether print debugging
     * @return created socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException if the server host is already bind or else case.
     */
    default @NonNull SocketServer create(int port, boolean debug) throws UnknownHostException, SocketException {
        return create(null, port, debug);
    }

    /**
     * Create new socket server on localhost without debugging.
     * @param port The server port
     * @return created socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException if the server host is already bind or else case.
     */
    default @NonNull SocketServer create(int port) throws UnknownHostException, SocketException {
        return create(port, false);
    }

    /**
     * Create new socket server, or Get opened socket server from the host if the host is already bound.
     * @param host The server hostname
     * @param port The server port
     * @param debug whether print debugging
     * @param threadPoolSize The size of thread pool
     * @return created socket server or opened socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException something wrong when create new socket.
     */
    @NonNull SocketServer createOrGet(String host, int port, boolean debug, int threadPoolSize) throws UnknownHostException, SocketException;

    /**
     * Create new socket server, or Get opened socket server from the host if the host is already bound.
     * @param host The server hostname
     * @param port The server port
     * @param debug whether print debugging
     * @return created socket server or opened socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException something wrong when create new socket.
     */
    default @NonNull SocketServer createOrGet(String host, int port, boolean debug) throws UnknownHostException, SocketException {
        return createOrGet(host, port, debug, 10);
    }

    /**
     * Create new socket server without debugging, or Get opened socket server from the host if the host is already bound.
     * @param host The server hostname
     * @param port The server port
     * @return created socket server or opened socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException something wrong when create new socket.
     */
    default @NonNull SocketServer createOrGet(String host, int port) throws UnknownHostException, SocketException {
        return createOrGet(host, port, false);
    }

    /**
     * Create new socket server on localhost, or Get opened socket server from the host if the host is already bound.
     * @param port The server port
     * @param debug whether print debugging
     * @return created socket server or opened socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException something wrong when create new socket.
     */
    default @NonNull SocketServer createOrGet(int port, boolean debug) throws UnknownHostException, SocketException {
        return createOrGet(null, port, debug);
    }

    /**
     * Create new socket server on localhost without debugging, or Get opened socket server from the host if the host is already bound.
     * @param port The server port
     * @return created socket server or opened socket server
     * @throws UnknownHostException The server host is invalid.
     * @throws SocketException something wrong when create new socket.
     */
    default @NonNull SocketServer createOrGet(int port) throws UnknownHostException, SocketException {
        return createOrGet(port, false);
    }

}
