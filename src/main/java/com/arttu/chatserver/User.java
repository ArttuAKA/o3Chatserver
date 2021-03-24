package com.arttu.chatserver;

class User {
    private String username;
    private String password;
    private String email;

    User(String name, String pw, String mail) {
        username = name;
        password = pw;
        email = mail;
    }

    public String getName() {
        return username;
    }
    //public void setName(String newName) {
      //  this.username = newName;
      //}

    public String getPassword() {
        return password;
    }
    //public void setPassword (String newPassword ) {
      //  this.password = newPassword;
    //}

    public String getEmail() {
        return email;
    }
    //public void setEmail (String newEmail){
      //  this.email = newEmail;
    //}
    
}
