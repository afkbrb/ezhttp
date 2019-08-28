package com.github.afkbrb.ezhttp;

import com.github.afkbrb.ezhttp.request.Request;
import com.github.afkbrb.ezhttp.request.RequestParser;
import com.github.afkbrb.ezhttp.response.*;
import com.github.afkbrb.ezhttp.thread.ThreadPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server {

    private String host = "localhost";
    private int port = 80;

    private Selector selector;

    public Server() {
    }

    public Server(int port) {
        this.port = port;
    }

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

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

    // 由子类覆盖实现自定义逻辑
    protected Response handle(Request request) {
        // return new HtmlResponse("<h1>😄Welcome to ezhttp!😄</h1>");
        // return new FileResponse("D:\\图片\\其他\\wick rick.jpg");
        // Map map = new HashMap();
        // map.put("a", new String[] {"1", "2", "3"});
        // map.put("b", "哈哈哈哈");
        // Map map1 = new HashMap();
        // map1.put("inner", "innerItem");
        // map.put("map", map1);
        // return new JsonResponse(map);
        // return new NotFoundResponse();
        return new ServerInternalErrorResponse();
    }

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
                // 需要及时将key移除，以免重复处理
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

    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = serverSocketChannel.accept();
            System.out.println("accept connection from: " + client.getRemoteAddress());
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                    e.printStackTrace();
                }
                Response response = handle(request);
                try {
                    client.register(selector, SelectionKey.OP_WRITE, response);
                    selector.wakeup();
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        });
        // todo: keep-alive
        // 不注销OP_READ的话，由于新线程中的读取存在延迟，将会多次判断READ就绪，重复调用read方法
        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
    }

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
                    // todo: keep-alive
                    key.cancel();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // 注销写操作
        // 不注销OP_WRITE的话，由于新线程中的写存在延迟，将会多次判断WRITE就绪，重复调用write方法
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

}
