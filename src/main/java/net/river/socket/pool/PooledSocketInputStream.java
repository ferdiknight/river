package net.river.socket.pool;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zig
 *
 */
public class PooledSocketInputStream extends InputStream {
    private InputStream is;
    private PooledSocket parent;
    
    public PooledSocketInputStream(PooledSocket parent, InputStream is) {
        this.is = is;
        this.parent = parent;
    }
    
    public int available() throws IOException {
        try {
            return is.available();
        } catch(IOException e) {
        	//System.err.println("input available error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }
    
    public void close() throws IOException {
        parent.close();
    }

    public int read() throws IOException {
        try {
            return is.read();
        } catch(IOException e) {
        	//System.err.println("read error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }
    
    public int read(byte [] b) throws IOException {
        try {
            return is.read(b);
        } catch(IOException e) {
        	//System.err.println("read error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }
    
    public int read(byte [] b, int off, int len) throws IOException {
        try {
            return is.read(b, off, len);
        } catch(IOException e) {
        	//System.err.println("read error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }
    
    public long skip(long n) throws IOException {
        try {
            return is.skip(n);
        } catch(IOException e) {
            parent.setHealthy(false);
            throw e;
        }
    }

}
