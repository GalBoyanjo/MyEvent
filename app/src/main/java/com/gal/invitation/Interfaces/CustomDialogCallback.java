package com.gal.invitation.Interfaces;

import com.gal.invitation.Entities.Contact;

import java.util.List;

/**
 * Created by Gal on 10/10/2017.
 */

public interface CustomDialogCallback {
    void onSelected(List<Contact> contacts);
}
