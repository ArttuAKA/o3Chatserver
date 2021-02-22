package com.arttu.chatserver;

/**
 * Hello world!
 *
 */
import java.time.LocalDateTime;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


public class ChatServer {
    public static void main(String[] args) throws Exception {
        try {
            log("Launching ChatServer...");
            ChatDatabase database = ChatDatabase.getInstance();
            
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = chatServerSSLContext();
            server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
                public void configure (HttpsParameters params) {
                InetSocketAddress remote = params.getClientAddress();
                SSLContext c = getSSLContext();
                SSLParameters sslparams = c.getDefaultSSLParameters();
                params.setSSLParameters(sslparams);
                }
            });
            ChatAuthenticator auth = new ChatAuthenticator();
            HttpContext chatContext = server.createContext("/Chat", new ChatHandler());
            chatContext.setAuthenticator(auth);
            server.createContext("/registration", new RegistrationHandler(auth));
            server.setExecutor(null);
            log("Starting Chatserver!");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static final String ANSI_RESET = "\u001b[om";
    public static final String ANSI_GREEN = "\u001b[32m";
    public static final String ANSI_BLACK = "\u001b[3om";
    public static final String ANSI_RED = "\u001b[31m";
    public static final String ANSI_YELLOW = "\u001b[33m";
    public static final String ANSI_BLUE = "\u001b[34m";
    public static final String ANSI_PURPLE = "\u001b[35m";
    public static final String ANSI_CYAN = "\u001b[36m";
    public static final String ANSI_WHITE = "\u001b[37m";

    public static void log(String message) {
        System.out.println(ANSI_GREEN + LocalDateTime.now() + ANSI_RESET + " " + message);
    }

    private static SSLContext chatServerSSLContext()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
            IOException, UnrecoverableKeyException, KeyManagementException {
        char[] passphrase = "apinaorkesteri".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("C:\\Uusi kansio\\chatserver\\keystore.jks"), passphrase);
     
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
     
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
     
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;  
    }
        
}
        
