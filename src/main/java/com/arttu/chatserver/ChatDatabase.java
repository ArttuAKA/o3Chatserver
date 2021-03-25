package com.arttu.chatserver;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;

import java.security.*;

public class ChatDatabase {
    private SecureRandom secureRandom;
    private Connection dbconnection;
    private static ChatDatabase singleton = null;
    public static synchronized ChatDatabase getInstance() {
        if (null == singleton) {
           singleton = new ChatDatabase();
        }
        return singleton;
    }

    private ChatDatabase() {
        secureRandom = new SecureRandom();
    }

    public boolean open(String dbName) throws SQLException{
        if(null==dbconnection){
        File file = new File (dbName);
        boolean exists = file.exists();
        


        String database = "jdbc:sqlite:" + dbName;
        dbconnection = DriverManager.getConnection(database);
        if (!file.exists() && !file.isDirectory()){
            initializeDatabase(); 
        }
        }
        
        
         return false;
         
        //user

    }

    public boolean initializeDatabase() throws SQLException{
       if (null!=dbconnection) {

           String createUsersString = "CREATE TABLE User " +
           "(username VARCHAR(255) not NULL, " +
           " password VARCHAR(255) not NULL, " + 
           " email VARCHAR(255) not NULL, " +
           " salt VARCHAR(255) not NULL, " +  
           " PRIMARY KEY ( username ))"; 
           Statement createStatement = dbconnection.createStatement();
           createStatement.executeUpdate(createUsersString);
           createStatement.close(); //MESSAGE

           String createMessageString = "CREATE TABLE Message " +
           " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
           " nick VARCHAR(255), " +
           " message VARCHAR(255) not NULL, " + 
           " sent VARCHAR(255) not NULL) ";  
             
           Statement createMsgStatement = dbconnection.createStatement();
           createMsgStatement.executeUpdate(createMessageString);
           createMsgStatement.close();

           return true;
       }
       return false;
    }



    public boolean addUser(User user) throws SQLException {
        boolean result = false;
        if (null != dbconnection && !isUserNameRegistered(user.getName())){
            byte bytes[] = new byte[13];
            long timeStamp = System.currentTimeMillis();
            secureRandom.nextBytes(bytes);
            String saltBytes = new String(Base64.getEncoder().encode(bytes));
            String salt = "$6$" + saltBytes;
            String hashedPassword = Crypt.crypt(user.getPassword(),salt);
            
            String insertUserString = "INSERT INTO User(username, password, email, salt) VALUES ('" + user.getName() + "','" + hashedPassword + "','" + user.getEmail() + "','" + salt + "')";
            Statement createStatement;
            createStatement = dbconnection.createStatement();
            createStatement.executeUpdate(insertUserString);
            createStatement.close();
            result = true;
        }
        else {
            ChatServer.log("User already registered" + user.getName());
        }
        return result;
    }



    public boolean isUserNameRegistered(String username) {
        boolean result = false;
        if (null != dbconnection) {
            try {
                String queryUser = "SELECT username FROM User WHERE username='" + username +"'";
                Statement queryStatement = dbconnection.createStatement();
                ResultSet rs = queryStatement.executeQuery(queryUser);
                while (rs.next()) {
                    String user = rs.getString("username");
                    if (user.equals(username)){
                        result = true;
                        break;
                    }
                }
                queryStatement.close();
            } catch (SQLException e) {
                ChatServer.log("Could not check isUserNameRegistered: " + username);
                ChatServer.log("Reason: " + e.getErrorCode() + " " + e.getMessage());
            }
        }
        return result;
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        boolean result = false;
        if (null != dbconnection) {
            try{
                String queryAuthenticate = "SELECT username, password FROM User WHERE username='" + username + "'";
                Statement queryStatement = dbconnection.createStatement();
                ResultSet rs = queryStatement.executeQuery(queryAuthenticate);
                while (rs.next()) {
                    String user = rs.getString("username");
                    String pw = rs.getString("password");
                    if (user.equals(username) && pw.equals(Crypt.crypt(password, pw))) {
                        result = true;
                        break;
                    }
                }  queryStatement.close();
            } catch (SQLException e) {
                ChatServer.log("wrong username or password  " + username);
                ChatServer.log("Reason: " + e.getErrorCode() + " " + e.getMessage());
            }

        } return result;
    }

    public void insertMessage( ChatMessage message) throws SQLException {
        long timeStamp = message.dateAsInt();
        String insertMsStatement = "INSERT INTO Message(nick, message, sent) VALUES ('" + message.nick + "','" + message.message + "','" + timeStamp + "')";
        Statement createStatement;
        createStatement = dbconnection.createStatement();
        createStatement.executeUpdate(insertMsStatement);
        createStatement.close();
    }

    ArrayList<ChatMessage> getMessages(long messagesSince) throws SQLException {    
        ArrayList<ChatMessage> messages = null;
        Statement queryStatement;
        String queryMessages;
        PreparedStatement pr;
        queryStatement = dbconnection.createStatement();
        
        if (messagesSince == -1) {
            queryMessages = "SELECT message, nick, sent"
            + " FROM Message WHERE sent > ? ORDER BY sent DESC LIMIT 100";
          
        pr = dbconnection.prepareStatement(queryMessages);
        pr.setLong(1, messagesSince);
        }
        else {
            queryMessages = "SELECT nick, sent, message "
            + "FROM Message ORDER BY sent DESC";
            pr = dbconnection.prepareStatement(queryMessages);
        }
        ChatServer.log(queryMessages);
        
        ResultSet rs = pr.executeQuery();
        int recordCount = 0;
        while (rs.next() ) {                       //&& recordCount < MAX_NUMBER_OF_RECORDS_TO_FETCH
            if (null == messages) {
                messages = new ArrayList<ChatMessage>();
            }
            String user = rs.getString("nick");
            String message = rs.getString("message");
            long sent = rs.getLong("sent");
            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(sent), ZoneOffset.UTC);
            ChatMessage msg = new ChatMessage(user, message, time);
            msg.nick = user;
            msg.message = message;
            msg.setSent(sent);
            messages.add(msg);

        }
        queryStatement.close();
        return messages;

    }


}
