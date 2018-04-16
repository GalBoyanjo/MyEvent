package com.gal.invitation.Interfaces;

import com.gal.invitation.Entities.Contact;

/**
 * Created by Gal on 01/07/2017.
 */

public interface GuestUpdateCallbacks {

    void onSuccess(Contact contact);
    void onError(String errorMessage);

}
