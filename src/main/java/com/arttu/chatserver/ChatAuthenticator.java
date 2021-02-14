package com.arttu.chatserver;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;

public class ChatAuthenticator extends BasicAuthenticator {

    private Map<String, User> users = null;


    public ChatAuthenticator() {
        super("chat");
        users = new Hashtable<String, User>();
        //users.put("dummy", "users");
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
    public boolean addUser(User user) {
        if (!users.containsKey(user)) {
            users.put(user.getName(), user);
            //users.put(user.getPassword(), user);
            //users.put(user.getEmail(), user);

            return true;
        }
        return false;
    }
    
}
