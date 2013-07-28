package net.river.socket.pool;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author zig
 *
 */
public class SocketFactoryImpl implements SocketFactory {

    private String host = null;

    private int port = 0;

    private HashMap<String, PropEntry> attributes = new HashMap<String, PropEntry>();

    private int soTimeout = 0;
    private int soReadTimeout = 0;
    
    //private InetAddress address;

    private InetSocketAddress endpoint = null;
    
    public SocketFactoryImpl() {

    }

    public SocketFactoryImpl(String host, int port) {
        this.host = host;
        this.port = port;
        endpoint = new InetSocketAddress(host, port);
    }

    private void setProperty(String name, Object value, Class type) {
        PropEntry entry = (PropEntry) attributes.get(name);
        if (entry == null) {
            entry = new PropEntry();

            String methodName = "set" + Character.toUpperCase(name.charAt(0))
                    + name.substring(1);
            try {
                Method method = Socket.class.getMethod(methodName,
                        new Class[] { type });
                entry.method = method;
                attributes.put(name, entry);
            } catch (NoSuchMethodException e) {
            }
        }
        entry.value = value;
    }

    private void applySocketSettings(Socket socket) throws IOException {
        for (Iterator it = attributes.values().iterator(); it.hasNext();) {
            PropEntry entry = (PropEntry) it.next();
            try {
                entry.method.invoke(socket, new Object[] { entry.value });
            } catch (Exception e) {
            }
        }
    }

    public Socket createSocket() throws IOException {
        Socket socket = new Socket();
        applySocketSettings(socket);
        if (host != null && port > 0) {
            try {
                if (soTimeout > 0)
                    socket.connect(endpoint, soTimeout);
                else
                    socket.connect(endpoint);
                
                if (soReadTimeout > 0){
                	socket.setSoTimeout(this.soReadTimeout);
                }
                
            } catch (IOException e) {
                socket.close();
                throw e;
            }
        }
        return socket;
    }

    public void setHost(String host) {
        this.host = host;
        endpoint = new InetSocketAddress(host, port);
    }

    public void setPort(int port) {
        this.port = port;
        endpoint = new InetSocketAddress(host, port);
    }

    public void setKeepAlive(boolean on) {
        setProperty("keepAlive", new Boolean(on), Boolean.TYPE);
    }

    public void setOOBInline(boolean on) {
        setProperty("oOBInline", new Boolean(on), Boolean.TYPE);
    }

    public void setReceiveBufferSize(int size) {
        setProperty("receiveBufferSize", new Integer(size), Integer.TYPE);
    }

    public void setReuseAddress(boolean on) {
        setProperty("reuseAddress", new Boolean(on), Boolean.TYPE);
    }

    public void setSendBufferSize(int size) {
        setProperty("sendBufferSize", new Integer(size), Integer.TYPE);
    }

    public void setSoTimeout(int timeout) {
        soTimeout = timeout;
        setProperty("soTimeout", new Integer(timeout), Integer.TYPE);
    }

    public void setSoReadTimeout(int timeout) {
    	this.soReadTimeout = timeout;
    	setProperty("soReadTimeout", new Integer(timeout), Integer.TYPE);
    }
    
    public void setTcpNoDelay(boolean on) {
        setProperty("tcpNoDelay", new Boolean(on), Boolean.TYPE);
    }

    private class PropEntry {
        Method method;
        Object value;
    }
}
