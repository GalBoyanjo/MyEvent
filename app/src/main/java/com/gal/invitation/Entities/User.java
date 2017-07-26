package com.gal.invitation.Entities;


import java.io.Serializable;

public class User implements Serializable {

    private int ID;
    private String username;
    private String password;
    private String email;


    public User(int ID, String username, String password, String email) {
        setID(ID);
        setUsername(username);
        setPassword(password);
        setEmail(email);

    }

    public int getID() {
        return ID;
    }


    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
