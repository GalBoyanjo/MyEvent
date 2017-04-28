package com.gal.invitation.Screens;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.ContactsAdapter;
import com.gal.invitation.Utils.MyStringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Profile extends AppCompatActivity {

    private RequestQueue netRequestQueue;
    private final static String url_edit_contact = "http://master1590.a2hosted.com/invitations/editContact.php";
    private final static String url_delete_contact = "http://master1590.a2hosted.com/invitations/deleteContact.php";
    private final static String url_get_contacts = "http://master1590.a2hosted.com/invitations/getUserContacts.php";
    private final static String TAG_SUCCESS = "success";
    private User user = null;
    private TreeSet<Contact> userContacts = new TreeSet<>(new Comparator<Contact>() {
        @Override
        public int compare(Contact contact, Contact contact2) {
            return contact.getName().compareTo(contact2.getName());
        }
    });
    private ProgressDialog progressDialog;
    private AlertDialog editor;

    private TextView txtName;

    final Context context= this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_contacts));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        getUserContacts();

        txtName = (TextView) findViewById(R.id.userName);
        txtName.setText("HELLO " + String.valueOf(user.getUsername()));

        final Button sendInvitationsSms = (Button) findViewById(R.id.profile_send_invitation_btn);
        sendInvitationsSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendInvitationLink = new Intent(Profile.this,SendInvitations.class);
                sendInvitationLink.putExtra("list", new ArrayList<>(userContacts));
                startActivity(sendInvitationLink);
            }
        });
    }



    private void getUserContacts() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_get_contacts, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            JSONArray ja = jsonObject.getJSONArray("Contacts");
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject jo = ja.getJSONObject(i);
                                Contact tempContact = new Contact();
                                tempContact.setName(jo.getString("Name"));
                                tempContact.setPhone(jo.getString("Phone"));
                                tempContact.setImage(ContactUtil.retrieveContactPhoto(
                                        tempContact.getPhone(), Profile.this));

                                userContacts.add(tempContact);
                            }

                            showContact();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showContact() {
        ArrayList<Contact> contactsArrayList = new ArrayList<>(userContacts);
        final ListView listView = (ListView) findViewById(R.id.profile_contact_list);
        ContactsAdapter adapter = new ContactsAdapter(Profile.this,
                R.layout.contact_row, contactsArrayList);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final Contact contact = ((Contact) listView.getAdapter().getItem(position));
                LinearLayout rowContainer = (LinearLayout) view.findViewById(R.id.row_container);



                ImageButton deleteContactBtn=(ImageButton)rowContainer.findViewById(R.id.row_remove);
                deleteContactBtn.setVisibility(View.VISIBLE);
                final ImageButton editContactBtn=(ImageButton)rowContainer.findViewById(R.id.row_edit);
                editContactBtn.setVisibility(View.VISIBLE);


                deleteContactBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        deleteContact(contact);
                        finish();
                        startActivity(getIntent());

                    }
                });

                editContactBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {

                        Toast.makeText(Profile.this, contact.getName()+"  EDIT",
                                Toast.LENGTH_LONG).show();


                        LayoutInflater inflater = LayoutInflater.from(context);

                        View contactEditView = inflater.inflate(R.layout.contact_edit,null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setView(contactEditView);

                        final EditText contactName = (EditText)contactEditView.findViewById(R.id.contact_name);
                        final EditText contactPhone = (EditText)contactEditView.findViewById(R.id.contact_phone);

                        contactName.setHint(String.valueOf(contact.getName()));
                        contactPhone.setHint(String.valueOf(contact.getPhone()));

                        builder
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String setContactName=contactName.getText().toString();
                                        if(TextUtils.isEmpty(setContactName))
                                            setContactName=(String.valueOf(contact.getName()));
                                        String setContactPhone=contactPhone.getText().toString();
                                        if(TextUtils.isEmpty(setContactPhone))
                                            setContactPhone=(String.valueOf(contact.getPhone()));
                                        editContact(contact,setContactName,setContactPhone);
                                        finish();
                                        startActivity(getIntent());

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
                });

                //deleteContact(contact);
                //Toast.makeText(Profile.this, contact.getName(), Toast.LENGTH_LONG).show();
                return true;
            }

        });

        progressDialog.dismiss();
    }

    private void sendInvitationsMessage(final ArrayList<Contact> contactArrayList) {


    }
    private void deleteContact(final Contact contact){
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));
            params.put("Name", contact.getName());
            params.put("Phone", contact.getPhone());

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_delete_contact, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            Toast.makeText(Profile.this, contact.getName()+"  DELETED",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Profile.this,
                                "error_deleting_contact_in_db",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void editContact(final Contact contact,String contactName,String contactPhone){
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));
            params.put("Name", contact.getName());
            params.put("NewName", contactName);
            params.put("Phone", contact.getPhone());
            params.put("NewPhone", contactPhone);

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_edit_contact, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            Toast.makeText(Profile.this, contact.getName()+"  Edited",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Profile.this,
                                "error_editing_contact_in_db",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}