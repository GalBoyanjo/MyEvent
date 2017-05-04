package com.gal.invitation.Screens;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.gal.invitation.R;

public class CreateInvitation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invitation);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstannceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateInvitation.this);
        builder.setView(contactEditView);

        final EditText contactName = (EditText) contactEditView.findViewById(R.id.contact_name);
        final EditText contactPhone = (EditText) contactEditView.findViewById(R.id.contact_phone);

        contactName.setHint(String.valueOf(contact.getName()));
        contactPhone.setHint(String.valueOf(contact.getPhone()));

        builder
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String setContactName = contactName.getText().toString();
                        if (TextUtils.isEmpty(setContactName))
                            setContactName = (String.valueOf(contact.getName()));
                        String setContactPhone = contactPhone.getText().toString();
                        if (TextUtils.isEmpty(setContactPhone))
                            setContactPhone = (String.valueOf(contact.getPhone()));
                        editContact(contact, setContactName, setContactPhone);
                        adapter.remove(contact);
                        contact.setName(setContactName);
                        contact.setPhone(setContactPhone);
                        adapter.add(contact);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();

                    }
                });
        builder.create();

        builder.show();


    }
}
