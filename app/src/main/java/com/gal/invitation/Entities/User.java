package com.gal.invitation.Entities;


import java.io.Serializable;

public class User implements Serializable {

    private int ID;
    private String userName;
    private String password;
    private String email;


    public User(int ID, String userName, String password, String email) {
        setID(ID);
        setUserName(userName);
        setPassword(password);
        setEmail(email);

    }

    public int getID() {
        return ID;
    }


    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
