package com.arttu.chatserver;

import java.nio.charset.Charset;
import java.sql.SQLException;


import com.sun.net.httpserver.BasicAuthenticator;

public class ChatAuthenticator extends BasicAuthenticator {

    


    public ChatAuthenticator() {
        super("chat");
        
    } 

    
    @Override
    public boolean checkCredentials(String username, String password) {
        try {
            return ChatDatabase.getInstance().authenticateUser(username, password);
        } catch (SQLException e) {
            
            e.printStackTrace();
        }
        return false;
        
    }
    public boolean addUser(User user) throws SQLException {
        
        if (Charset.forName("US-ASCII").newEncoder().canEncode(user.getName())) {
            return ChatDatabase.getInstance().addUser(user);
        }
        
        
        return false;
    }
    
}
