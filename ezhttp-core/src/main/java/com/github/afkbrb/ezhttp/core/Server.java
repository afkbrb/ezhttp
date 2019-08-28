package com.github.afkbrb.ezhttp.core;

import com.github.afkbrb.ezhttp.core.request.Request;
import com.github.afkbrb.ezhttp.core.request.RequestParser;
import com.github.afkbrb.ezhttp.core.response.*;
import com.github.afkbrb.ezhttp.core.thread.ThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private String host;
    private int port;

    private Selector selector;

    /**
     * Construct a server with default configuration.
     * By default, host is localhost and port is 2333
     */
    public Server() {
        this("localhost", 2333);
    }

    /**
     * Construct a server on the given port.
     *
     * @param port Port of the server.
     */
    public Server(int port) {
        this("localhost", port);
    }

    /**
     * Construct a server on the given host and port.
     *
     * @param host Host of the server, default localhost.
     * @param port Port of the server.
     */
    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Init the server, make the server ready to accept connections.
     */
    private void init() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * The most important method of the Server.
     * SubClass of this class should override this method to customize the server.
     *
     * @param request The Request instance parsed from HTTP request line, request header and request body.
     * @return HTTP response.
     */
    protected Response handle(Request request) {
        return new FileResponse(ClassLoader.getSystemClassLoader().getResource("page/welcome.html").getPath());
    }

    /**
     * Start the server, make the server ready to select acceptable, readable or writable operations that are ready.
     */
    public void start() {
        init();

        while (true) {
            try {
                if (selector.select() == 0) continue;
            } catch (IOException e) {
                e.printStackTrace();
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    /**
     * Accept new connection.
     *
     * @param key
     */
    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = null;
        try {
            client = serverSocketChannel.accept();
            System.out.println("accept connection from: " + client.getRemoteAddress());
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Process read operation in a new thread.
     *
     * @param key
     */
    private void read(SelectionKey key) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                SocketChannel client = (SocketChannel) key.channel();
                try {
                    System.out.println("read from: " + client.getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Request request = null;
                try {
                    request = RequestParser.parseRequest(client);
                } catch (IOException e) {
                    System.err.println("err parsing request");
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return;
                }

                Response response = handle(request);
                try {
                    client.register(selector, SelectionKey.OP_WRITE, response);
                    selector.wakeup();
                } catch (ClosedChannelException e) {

                }
            }
        });
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
    }

    /**
     * Process write operation in a new thread.
     *
     * @param key
     */
    private void write(SelectionKey key) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                SocketChannel client = (SocketChannel) key.channel();
                try {
                    System.out.println("write to: " + client.getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Response response = (Response) key.attachment();
                ByteBuffer buffer = response.getResponseBuffer();

                // write to client
                try {
                    client.write(buffer);
                } catch (IOException e) {

                } finally {
                    key.cancel();
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

}
