package com.gal.invitation.Entities;

/**
 * Created by Gal on 31/07/2017.
 */

public class Invitation {

    private String type = "";
    private String date = "";
    private String time = "";
    private String placetype = "";
    private String place = "";
    private String address = "";
    private String freetext = "";
    private String bride = "";
    private String groom = "";

    public Invitation(){
    }

    public Invitation(String type , String date , String time , String placetype , String place , String address , String freetext , String bride , String groom){
        setType(type);
        setDate(date);
        setTime(time);
        setPlacetype(placetype);
        setPlace(place);
        setAddress(address);
        setFreeText(freetext);
        setBride(bride);
        setGroom(groom);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlacetype() {
        return placetype;
    }

    public void setPlacetype(String placetype) {
        this.placetype = placetype;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFreeText() {
        return freetext;
    }

    public void setFreeText(String freeText) {
        this.freetext = freeText;
    }

    public String getBride() {
        return bride;
    }

    public void setBride(String bride) {
        this.bride = bride;
    }

    public String getGroom() {
        return groom;
    }

    public void setGroom(String groom) {
        this.groom = groom;
    }


}
