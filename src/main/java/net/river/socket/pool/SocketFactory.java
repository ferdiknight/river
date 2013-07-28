package net.river.socket.pool;

import java.io.IOException;
import java.net.Socket;

/**
 * @author zig
 *
 */
public interface SocketFactory {
    public Socket createSocket() throws IOException;
}
