package com.arttu.chatserver;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;

public class ChatAuthenticator extends BasicAuthenticator {

    private Map<String, String> users = null;


    public ChatAuthenticator() {
        super("chat");
        users = new Hashtable<String,String>();
        users.put("dummy", "password");
    }

    
    @Override
    public boolean checkCredentials(String username, String password) {
        if (users.containsKey(username)) {
            if (users.get(username).equals(password)) {
                return true;
            }
        }
        return false;
    }
    public boolean addUser(String username, String password) {
        if (!users.containsKey(username)) {
            users.put(username, password);
            return true;
        }
        return false;
    }
    
}
