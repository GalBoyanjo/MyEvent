package com.gal.invitation.Interfaces;

import com.gal.invitation.Entities.User;

/**
 * Created by Gal on 01/07/2017.
 */

public interface LoginRequestCallbacks {

    void onSuccess(User user);
    void onError(String errorMessage);

}
