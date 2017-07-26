package com.gal.invitation.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.gal.invitation.R;

import java.util.Locale;

/**
 * Created by Gal on 01/07/2017.
 */

public class ScreenUtil {


    public static void setLocale(Activity activity, String title) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit();
        Resources res = activity.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        String systemLanguage = Locale.getDefault().getLanguage();
        int languagePosition = Integer.valueOf(preferences.getString(Constants.TAG_LANGUAGE_POSITION, "-1"));
        if (languagePosition < 0) {
            switch (systemLanguage) {
                case "iw":
                    languagePosition = 0;
                    break;
                case "he":
                    languagePosition = 0;
                    break;
                case "en":
                    languagePosition = 1;
                    break;
                default:
                    languagePosition = 1;
                    break;
            }
            editor.putString(Constants.TAG_LANGUAGE_POSITION, String.valueOf(languagePosition));
            editor.commit();
        }
        conf.setLocale(new Locale(Constants.LANGUAGES[languagePosition].toLowerCase()));
        conf.setLayoutDirection(new Locale(Constants.LANGUAGES[languagePosition]));
        res.updateConfiguration(conf, dm);
        activity.setTitle(title);
    }

}
