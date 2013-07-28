package net.river.socket.pool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * @author zig
 *
 */
public class Test {
    
    public static void main(String[] args) throws Exception {
        SocketFactoryImpl factory = new SocketFactoryImpl("127.0.0.1", 4404);
        factory.setSoTimeout(1000);
        
        SocketPool pool = new SocketPool();
        pool.setSocketFactory(factory);
        
        while (true) {
            Socket socket = pool.getSocket();
            System.out.println(socket.getLocalPort());
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                
                writer.println("get wuyinghui97@sohu.com");
                System.out.println(new Date() + " : " + reader.readLine());
                
            } finally {
                socket.close();
            }
        }
        
    }
    
}
