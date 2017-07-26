package com.gal.invitation.Screens;


import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.*;

import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Entities.User;
import com.gal.invitation.R;
import com.gal.invitation.Utils.Constants;
import com.gal.invitation.Utils.ScreenUtil;

import java.util.ArrayList;
import java.util.Locale;


public class SendInvitations extends Activity {

    public static String systemLanguage;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private Button sendBtn;
    private String phoneNo;
    private String message;
    private ArrayList<Contact> contactArrayList = new ArrayList<>();
    private boolean hasSMSPermission = false;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(SendInvitations.this, getString(R.string.title_activity_send_invitations));
        setContentView(R.layout.activity_send_invatations);



        hasSMSPermission = ContextCompat.checkSelfPermission(SendInvitations.this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED;

        try {
            contactArrayList = (ArrayList<Contact>) getIntent().getSerializableExtra("list");
            user = (User)getIntent().getSerializableExtra("user");
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendBtn = (Button) findViewById(R.id.btnSendSMS);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (hasSMSPermission) {
                    sendSMSMessage();
                } else {
                    requestSMSPermission();
                }
            }
        });
    }

    public void onConfigurationChanged(Configuration newConfig) {
        /**
         * This overridden method will catch the screen rotation event and will prevent the onCreate
         * function call. Defined in Manifest xml - activity node
         */
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setNavigationBarColor(ContextCompat.getColor(SendInvitations.this, R.color.colorPrimary));
        }
        systemLanguage = newConfig.locale.getLanguage();
        ScreenUtil.setLocale(SendInvitations.this, getString(R.string.title_activity_send_invitations));

    }

    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);

        } else {
            hasSMSPermission = true;
            sendSMSMessage();
        }

    }

    protected void sendSMSMessage() {
        for (Contact contact : contactArrayList) {

            phoneNo = contact.getPhone();
            message = "http://master1590.a2hosted.com/invitations/confirmation_page/index.php?Code=" +
                    contact.getCode() + "&By=" + user.getID();

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(SendInvitations.this,
                    "http://master1590.a2hosted.com/invitations/confirmation_page/index.php?Code=" + contact.getCode() + (getString(R.string.SMS_sent)),
                    Toast.LENGTH_LONG).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMSMessage();
                } else {
                    Toast.makeText(SendInvitations.this,
                            (getString(R.string.SMS_faild_please_try_again)),
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }
}