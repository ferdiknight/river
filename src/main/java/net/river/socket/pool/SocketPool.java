package net.river.socket.pool;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Socket ���ӳ�.
 * 
 * @author zig
 */
public class SocketPool {
//    private Log log = LogFactory.getLog(SocketPool.class);

    private SocketFactory socketFactory = null;

    private long maxIdle = 0;

    private int maxSocketCount = 0;

    private int socketCount = 0;

    private boolean block = true;

    private boolean runDaemon = true;

    private int socketCreateCount = 0;

    private int socketClosedCount = 0;

    private LinkedList<PooledSocket> freeSockets;

    private ArrayList<PooledSocket> usedSockets;

    public SocketPool() {
        freeSockets = new LinkedList<PooledSocket>();
        usedSockets = new ArrayList<PooledSocket>(256);
    }

    public SocketPool(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        freeSockets = new LinkedList<PooledSocket>();
        usedSockets = new ArrayList<PooledSocket>(256);
    }

    public void setRunDaemon(boolean value) {
        this.runDaemon = value;
    }

    public boolean getRunDaemon() {
        return this.runDaemon;
    }

    /**
     * �����ӳ���ȡ��һ���µ�����. �����ӳ�������block״̬��ʱ�����������û�п��ÿ������ӵ�ʱ�� ����ȴ�״̬��ֱ�����ӿ���;
     * ���������non-block��״̬����û�� �������ӵ�ʱ�򣬻��׳�PoolFullException.
     * 
     * @return ���õ�����
     * @throws IOException
     */
    public synchronized Socket getSocket() throws PoolFullException,
            IOException {
        PooledSocket sock = null;
        while (true) {
            // ���ȼ���Ƿ��г�ʱ�䲻ʹ�õ����ӣ�ɾ��֮
            if (maxIdle > 0) {
                long current = System.currentTimeMillis();
                for (Iterator it = freeSockets.iterator(); it.hasNext();) {
                    PooledSocket psock = (PooledSocket) it.next();
                    if (current - psock.getLastReleaseTime() > maxIdle) {
//                        log.info("Remove idle socket [local_port:"
//                                + psock.getLocalPort() + "]");
                        it.remove();
                        try {
                            remove(psock);
                        } catch (IOException e) {
//                            log.info(e.getMessage());
                        }
                    }
                }
            }

            while (!freeSockets.isEmpty()) {
                sock = (PooledSocket) freeSockets.removeFirst();
                if (sock.getSocket().isClosed()) {
                    // �����Ѿ����������ر�
//                    log.info("PooledSocket [local_port:" + sock.getLocalPort()
//                            + "] closed by server.");
                    sock = null;
                } else {
                    break;
                }
            }

            if (sock == null) {
                if (maxSocketCount > 0 && socketCount >= maxSocketCount) {
                    // socket���ӳ�����
                    if (!block) {
                        throw new PoolFullException(maxSocketCount
                                + " limit reached");
                    }
                    try {
                        wait();
                    } catch (InterruptedException e) {}
                } else {
                    // �����µ�����
                    if (socketFactory == null)
                        throw new NullPointerException("socketFactory is null");
                    Socket rawSock = socketFactory.createSocket();
                    sock = new PooledSocket(this, rawSock);
                    socketCount++;
                    socketCreateCount++;
//                    log.info("New connection created [" + socketCount + ", "
//                            + socketCreateCount + "]:" + rawSock);
                    break;
                }
            } else {
                break;
            }
        }
        sock.setOwner(Thread.currentThread().getName());
        sock.setClosed(false);
        usedSockets.add(sock);
        return sock;
    }

    /**
     * ���ĳ��socket �ϵ�InputStreamReader����������ӳ��е����ӣ���Ӧ�� ReaderҲ�Ǳ�cache��.
     * <b>�����������õ�Reader��Ҫ����close()�������ͷŶ�Ӧ��������Ҫ����socket.close()����</b>
     * 
     * @param socket
     * @param charset
     * @return
     * @throws IOException
     */
    public static InputStreamReader getReader(Socket socket, String charset)
            throws IOException {
        if (socket instanceof PooledSocket) {
            return ((PooledSocket) socket).getInputStreamReader(charset);
        } else {
            return new InputStreamReader(socket.getInputStream(), charset);
        }
    }

    /**
     * ���ĳ��socket�ϵ�OutputStreamWriter����������ӳ��е����ӣ���Ӧ�� Writer Ҳ�Ǳ�cache��.
     * <b>�����������õ�Writer��Ҫ����close()�������ͷŶ�Ӧ��������Ҫ����socket.close()����</b>
     * 
     * @param socket
     * @param charset
     * @return
     * @throws IOException
     */
    public static OutputStreamWriter getWriter(Socket socket, String charset)
            throws IOException {
        if (socket instanceof PooledSocket) {
            return ((PooledSocket) socket).getOutputStreamWriter(charset);
        } else {
            return new OutputStreamWriter(socket.getOutputStream(), charset);
        }
    }

    /**
     * �����ӳ���ɾ��һ�����ӣ����ǲ����ر��������.
     * 
     * @param socket
     * @throws IOException
     */
    public static void evict(Socket socket) throws IOException {
        if (socket instanceof PooledSocket) {
            ((PooledSocket) socket).setHealthy(false);
        }
    }

    /**
     * ��PooledSocket״̬���õ�ʱ����close()��ʱ���ص�����������Ӷ� �����ӽ��������ӳ�.
     * 
     * @param sock
     * @throws IOException
     */
    synchronized void release(PooledSocket sock) throws IOException {
        if (sock.getPool() != this)
            throw new IOException("pool mistake");

        // log.info("Socket [local_port:" + sock.getLocalPort() + "] released");
        usedSockets.remove(sock);
        // freeSockets.addFirst(sock);
        // ȡ��ʱ����getFirst();
        freeSockets.addLast(sock);
        sock.setLastReleaseTime(System.currentTimeMillis());
        if (block)
            notify();
    }

    /**
     * ������ӳ��е��������ӣ����ҹر����� ��;�� ��PooledSocket������������IO�����ʱ�����������PooledSocket�ص���
     * �Ӷ����Ӵ����ӳ��������ʵ�ʹر�����.
     * 
     * @param sock
     * @throws IOException
     */
    synchronized void remove(PooledSocket sock) throws IOException {
        try {
            if (sock.getPool() != this)
                throw new IOException("pool mistake");

            if (usedSockets.contains(sock))
                usedSockets.remove(sock);
            else
                freeSockets.remove(sock);

            socketCount--;
            socketClosedCount++;
//            log.info("Socket [local_port:" + sock.getLocalPort() + ", "
//                    + socketCount + ", " + socketClosedCount
//                    + "] connection closed : " + sock.getSocket());
            sock.getSocket().close();
        } finally {
            if (block)
                notify();
        }
    }

    /**
     * �������ӳ������ӵ�������ʱ��.
     * 
     * @param idleTime
     */
    public void setMaxIdle(long idleTime) {
        maxIdle = idleTime;
    }

    public long getMaxIdle() {
        return maxIdle;
    }

    /**
     * �������ӳ��е����������.
     * 
     * @param size
     */
    public void setPoolSize(int size) {
        maxSocketCount = size;
    }

    public void setMaxSocketCount(int size) {
        maxSocketCount = size;
    }

    public int getMaxSocketCount() {
        return maxSocketCount;
    }

    public int getPoolSize() {
        return maxSocketCount;
    }

    /**
     * ������ӳ��е�������.
     * 
     * @return
     */
    public int getSocketCount() {
        return socketCount;
    }

    /**
     * �������ӳ���block����non-block��.
     * 
     * @param on
     */
    public void setBlock(boolean on) {
        block = on;
    }

    /**
     * �������ӵĹ�������. ֻ�������ӳ�Ϊ����û�����ڱ�ʹ�õ����ӵ�ʱ�� ���ܵ����������. ���������ӳس�ʼ����ʱ���������������Է�����.
     * 
     * @param socketFactory
     */
    public synchronized void setSocketFactory(SocketFactory socketFactory) {
        if (freeSockets.isEmpty() && usedSockets.isEmpty())
            this.socketFactory = socketFactory;
        else
            throw new IllegalStateException(
                    "socketFactory must be set at initializing state");
    }

}
