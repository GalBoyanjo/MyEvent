package com.gal.invitation.Interfaces;

import com.gal.invitation.Entities.Contact;

/**
 * Created by Gal on 25/10/2017.
 */

public interface UpdateProfileContacts {
    void deleteContact(Contact contact);
    void editContactDialog(Contact contact);
}
