package net.river.socket.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author zig
 *
 */
public class Test implements Runnable
{

    private static SocketPool pool;
    private static String command;

    public static void main(String[] args) throws Exception
    {

        if (args.length < 3)
        {
            return;
        }

        command = args[2];

        SocketFactoryImpl factory = new SocketFactoryImpl(args[0], Integer.parseInt(args[1]));
        factory.setSoTimeout(10000);

        pool = new SocketPool();
        pool.setSocketFactory(factory);
        pool.setMaxSocketCount(50);
        pool.setPoolSize(10);
        pool.setMaxIdle(30000);

        for(int i=0;i<10;i++)
        {
            new Thread(new Test()).start();
        }
        
//        while (true)
//        {
//            Socket socket = pool.getSocket();
//            System.out.println(socket.getLocalPort());
//            try
//            {
//                InputStream is = socket.getInputStream();
//                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
//
//                writer.println(command);
//                byte[] b = new byte[1024];
//                is.read(b);
//                System.out.println(new Date() + " : " + new String(b));
//
//            }
//            finally
//            {
//                socket.close();
//            }
//        }

    }

    public void run()
    {
        while (true)
        {
            try
            {
                Socket socket = pool.getSocket();
                System.out.println(socket.getLocalPort());
                try
                {
                    InputStream is = socket.getInputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                    writer.println(command);
                    byte[] b = new byte[1024];
                    is.read(b);
                    System.out.println(new Date() + " : " + new String(b));

                }
                finally
                {
                    socket.close();
                }
            }
            catch (PoolFullException ex)
            {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);

            }
        }
    }
}
