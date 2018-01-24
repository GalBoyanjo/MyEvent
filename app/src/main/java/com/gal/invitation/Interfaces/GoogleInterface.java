package com.gal.invitation.Interfaces;

import android.content.Context;

import com.gal.invitation.Entities.User;

/**
 * Created by Gal on 14/01/2018.
 */

public interface GoogleInterface {
    void googleLogin(Context context, User user);
    void googleLogOut(Context context);
}
