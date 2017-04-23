package com.gal.invitation.Screens;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.*;

import com.gal.invitation.Entities.Contact;
import com.gal.invitation.R;

import java.util.ArrayList;


public class SendInvitations extends Activity {
        private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
        Button sendBtn;
        String phoneNo;
        String message;
        private ArrayList<Contact> contactArrayList = new ArrayList<>();
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_send_invatations);

            try {
                contactArrayList = (ArrayList<Contact>) getIntent().getSerializableExtra("list");
            }catch (Exception e){}

            sendBtn = (Button) findViewById(R.id.btnSendSMS);

            sendBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (ContextCompat.checkSelfPermission(SendInvitations.this,
                            Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(SendInvitations.this,
                                Manifest.permission.SEND_SMS)) {
                            sendSMSMessage();
                        } else {
                            ActivityCompat.requestPermissions(SendInvitations.this,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    MY_PERMISSIONS_REQUEST_SEND_SMS);
                        }
                    }

                }
            });
        }

        protected void sendSMSMessage() {
            for(Contact contact : contactArrayList) {

                phoneNo = contact.getPhone();
                message = contact.getName();

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message, null, null);

            }
            Toast.makeText(getApplicationContext(), (getString(R.string.SMS_sent)),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                      sendSMSMessage();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                (getString(R.string.SMS_faild_please_try_again)), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

        }
    }