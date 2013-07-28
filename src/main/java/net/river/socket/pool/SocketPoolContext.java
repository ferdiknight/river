package net.river.socket.pool;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * ���ڱ���ע��ͷ����̳߳ص�singleton����.
 * ��һЩ�ṩ��ȫ��Ψһ�������Ļ����Ŀ���У�����spring���Ϳ��Ա���ʹ��
 * �������.
 * 
 * @author zig
 *
 */
public class SocketPoolContext {

    private static SocketPoolContext instance = new SocketPoolContext();
    
    private HashMap<String, SocketPool> pools = new HashMap<String, SocketPool>();
    
    private SocketPoolContext() {
        
    }
    
    public static SocketPoolContext getInstance() {
        return instance;
    }
    
    public SocketPool getSocketPool(String name) {
        return (SocketPool)pools.get(name);
    }
    
    @SuppressWarnings("unchecked")
    public void registerSocketPool(String name, SocketPool pool) {
    	HashMap<String, SocketPool> new_pools = (HashMap<String, SocketPool>) pools.clone();
        new_pools.put(name, pool);
        pools = new_pools;
    }
    
    public static Socket getSocket(String name) throws IOException {
        SocketPool pool = instance.getSocketPool(name);
        if (pool == null) {
            throw new IOException("cannot find socket pool : "+name);
        }
        return pool.getSocket();
    }
    
}
