package com.arttu.chatserver;

/**
 * Hello world!
 *
 */
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
public class ChatServer 
{
    public static void main(String[] args) throws Exception {
    
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/Chat", new ChatHandler());
        server.setExecutor(null);
        server.start();
    }
}
