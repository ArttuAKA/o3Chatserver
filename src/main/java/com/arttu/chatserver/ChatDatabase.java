package com.arttu.chatserver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ChatDatabase {
    private Connection dbconnection;
    private static ChatDatabase singleton = null;
    public static synchronized ChatDatabase getInstance() {
        if (null == singleton) {
           singleton = new ChatDatabase();
        }
        return singleton;
    }

    private ChatDatabase() {
    }

    public boolean open(String dbName) throws SQLException{
        if(null!=dbconnection){
        File file = new File (dbName);
        boolean exists = file.exists();
        


        String database = "jdbc:sqlite:" + dbName;
        dbconnection = DriverManager.getConnection(database);
        if (!file.exists() && !file.isDirectory()){
            initializeDatabase(); 
        }
        }
        
        
         return false;
         
        

    }

    public boolean initializeDatabase() throws SQLException{
       if (null!=dbconnection) {
           String createUsersString = "CREATE TABLE REGISTRATION " +
           "(user VARCHAR(255) not NULL, " +
           " password VARCHAR(255) not NULL, " + 
           " email VARCHAR(255) not NULL, " +  
           " PRIMARY KEY ( user ))";         

           Statement createStatement = dbconnection.createStatement();
           createStatement.executeUpdate(createUsersString);
           createStatement.close();

           String createMessageString = "CREATE TABLE MESSAGE " +
           "(nick VARCHAR(255) not NULL, " +
           " message VARCHAR(255) not NULL, " + 
           " sent VARCHAR(255) not NULL, " +  
           " PRIMARY KEY ( nick ))";         

           Statement createMsgStatement = dbconnection.createStatement();
           createMsgStatement.executeUpdate(createMessageString);
           createMsgStatement.close();
           return true;
       }
       return false;
    }
}
