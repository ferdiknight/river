package net.river.socket.pool;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author zig
 *
 */
public class PooledSocketOutputStream extends OutputStream {
    private PooledSocket parent;
    private OutputStream os;
    
    public PooledSocketOutputStream(PooledSocket parent, OutputStream os) {
        this.parent = parent;
        this.os = os;
    }
    
    public void close() throws IOException {
        parent.close();
    }
    
    public void flush() throws IOException {
        try {
            os.flush();
        } catch(IOException e) {
        	//System.err.println("flush error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }
    
    public void write(int b) throws IOException {
        try {
            os.write(b);
        } catch(IOException e) {
        	//System.err.println("write error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }

    public void write(byte [] b) throws IOException {
        try {
            os.write(b);
        } catch(IOException e) {
        	//System.err.println("write error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            os.write(b, off, len);
        } catch(IOException e) {
        	//System.err.println("write error "+e);
            parent.setHealthy(false);
            throw e;
        }
    }


}
