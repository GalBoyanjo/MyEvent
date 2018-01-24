package com.gal.invitation.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Gal on 05/12/2017.
 */

public class SaveSharedPreference {
    private static final String PREF_EMAIL= "myEmail";
    private static final String PREF_PASSWORD= "myPassword";
    private static final String PREF_USER_TYPE= "myType";


    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUser(Context ctx, String email, String password, String type) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_EMAIL, email);
        editor.putString(PREF_PASSWORD, password);
        editor.putString(PREF_USER_TYPE, type);
        editor.apply();
    }

    public static void removeUser(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove(PREF_EMAIL);
        editor.remove(PREF_PASSWORD);
        editor.remove(PREF_USER_TYPE);
        editor.apply();
    }

    public static String getUserEmail(Context ctx) {return getSharedPreferences(ctx).getString(PREF_EMAIL, "");}
    public static String getUserPassword(Context ctx) {return getSharedPreferences(ctx).getString(PREF_PASSWORD, "");}
    public static String getUserType(Context ctx) {return getSharedPreferences(ctx).getString(PREF_USER_TYPE, "");}
}
