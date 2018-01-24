package com.gal.invitation.Entities;

import android.graphics.Bitmap;
import android.opengl.Visibility;

import java.io.Serializable;

/**
 * Created on 11/04/2017.
 */

public class Contact implements Comparable, Serializable {

    private String phone = "";
    private String name = "";
    private String code = "";
    private int status = -1;
    private transient Bitmap image = null;
    private boolean selected = false;

    public Contact() {
    }

    public Contact(String phone, String name, Bitmap image) {
        this.phone = phone;
        this.name = name;
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Contact))
            return false;
        if (this.phone.equals(((Contact) o).getPhone())) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Object another) {
        if (another != null && another instanceof Contact) {
            if (this.hashCode() == another.hashCode())
                return 0;
            else if (this.hashCode() > another.hashCode())
                return 1;
            else
                return -1;
        }
        return -2;
    }
}
