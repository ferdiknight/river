package net.river.socket.pool;

import java.io.IOException;

/**
 * @author zig
 *
 */
public class PoolFullException extends IOException {
    private static final long serialVersionUID = 0x873f3d51eeL;
    
    public PoolFullException() {
    }
    
    public PoolFullException(String msg) {
        super(msg);
    }

}
