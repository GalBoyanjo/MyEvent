package com.gal.invitation.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Interfaces.CustomDialogCallback;
import com.gal.invitation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gal on 09/10/2017.
 */
public class CustomDialog extends AlertDialog.Builder {

    private Activity activity;

    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private LinearLayout checkBoxesContainer;

    private List<Contact> contacts;
    private List<Contact> selectedContacts = new ArrayList<>();

    private CustomDialogCallback callback;

    private AlertDialog alertDialog;

    public CustomDialog(Activity activity, List<Contact> contacts, CustomDialogCallback callback) {
        super(activity);
        this.activity = activity;
        this.contacts = new ArrayList<>(contacts);
        this.callback = callback;
    }

    @SuppressLint("InflateParams")
    @Override
    public AlertDialog show() {
        View view = LayoutInflater.from(activity).inflate(R.layout.send_invitation_dialog, null);


        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                callback.onSelected(selectedContacts);
                dialog.dismiss();
            }
        });

        setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        radioGroup = (RadioGroup) view.findViewById(R.id.radio_grp);
        radioButton1 = (RadioButton) view.findViewById(R.id.radio_select_all);
        radioButton2 = (RadioButton) view.findViewById(R.id.radio_select_maybe);
        radioButton3 = (RadioButton) view.findViewById(R.id.radio_select_manually);
        checkBoxesContainer = (LinearLayout) view.findViewById(R.id.checkboxes_container);

        radioButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    selectedContacts.clear();
                    selectedContacts.addAll(contacts);
                    checkBoxesContainer.setVisibility(View.GONE);
                }
            }
        });
        radioButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    selectedContacts.clear();
//                selectedContacts.addAll();
                    checkBoxesContainer.setVisibility(View.GONE);
                }
            }
        });
        radioButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    selectedContacts.clear();
                    checkBoxesContainer.setVisibility(View.VISIBLE);
                }
            }
        });


        for (final Contact contact : contacts) {
            CheckBox checkBox = new CheckBox(activity);
            checkBox.setText(contact.getName());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if (checked)
                        selectedContacts.add(contact);
                    else
                        selectedContacts.remove(contact);
                }
            });
            checkBoxesContainer.addView(checkBox);
        }
        setView(view);
        alertDialog = super.show();
        return alertDialog;

    }
}