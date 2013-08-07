package net.river.socket.pool;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.types.ObjectId;

/**
 * @author zig
 *
 */
public class Test implements Runnable
{

    private static SocketPool pool;
    private static String command;
    
    private static DB db;
    
    private static long sleep;

    public static void main(String[] args) throws Exception
    {

        if (args.length < 6)
        {
            return;
        }

        command = args[2];

        db = new Mongo(args[3]).getDB(args[4]);
        
        sleep = Long.parseLong(args[5]);
        
        SocketFactoryImpl factory = new SocketFactoryImpl(args[0], Integer.parseInt(args[1]));
        factory.setSoTimeout(30000);

        pool = new SocketPool();
        pool.setSocketFactory(factory);
        pool.setMaxSocketCount(50);
        pool.setPoolSize(10);
        pool.setMaxIdle(120000);

        for (int i = 0; i < 10; i++)
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
//                String resp = new String(b);
//                System.out.println(new Date() + " : " + resp);
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
        DBObject object;
        while (true)
        {
            try
            {
                Socket socket = pool.getSocket();
                //System.out.println(socket.getLocalPort());
                try
                {
//                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
//                    pw.println(command);
//                    pw.flush();
                    OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    osw.write(command + "\n");
                    osw.flush();

                    InputStream is = socket.getInputStream();

                    byte[] b = new byte[512];

                    is.read(b);

                    String resp = new String(b).trim();

//                    InputStream is = socket.getInputStream();
//                    InputStreamReader isr = new InputStreamReader(is);
//                    StringBuilder response = new StringBuilder();
//                    int c = -1;
//                    while ((c = isr.read()) >= 0)
//                    {
//                        System.out.println(c);
//                        response.append((char) c);
//                    }

//                    String resp = response.toString();


                    //System.out.println(new Date() + " : " + resp);
                    try
                    {
                        Thread.sleep(sleep);
                    }
                    catch(Exception ex)
                    {
                        
                    }
                    
                    object = db.getCollection("crawl_product").findOne(new BasicDBObject("_id",new ObjectId(resp)));
                    if(object == null)
                    {
                       System.out.println(resp);
                    }


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
