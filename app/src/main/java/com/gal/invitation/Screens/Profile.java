package com.gal.invitation.Screens;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import com.gal.invitation.Interfaces.UpdateAllContactsCallbacks;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.Constants;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.ContactsAdapter;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.ProfileContactsAdapter;
import com.gal.invitation.Utils.ScreenUtil;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class Profile extends AppCompatActivity {

    public static String systemLanguage;
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

    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabNewContact;
    private FloatingActionButton fabFromContacts;

    private TextView txtName;
    private ProfileContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(Profile.this, getString(R.string.title_activity_profile));
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_contacts));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        getUserContacts();

        txtName = (TextView) findViewById(R.id.userName);
        txtName.setText(getString(R.string.hello) +" " + String.valueOf(user.getUsername()));

        final LinearLayout sendInvitationsSms = (LinearLayout) findViewById(R.id.profile_send_invitation_btn);
        sendInvitationsSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendInvitationLink = new Intent(Profile.this, SendInvitations.class);
                sendInvitationLink.putExtra("user", user);
                sendInvitationLink.putExtra("list", new ArrayList<>(userContacts));
                startActivity(sendInvitationLink);
            }
        });

        final RelativeLayout createInvitation = (RelativeLayout) findViewById(R.id.profile_create_invitation_btn);
        createInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createInvitationLink = new Intent(Profile.this, CreateInvitation.class);
                createInvitationLink.putExtra("user", user);
                startActivity(createInvitationLink);
            }
        });

        fabFromContacts = (FloatingActionButton) findViewById(R.id.fab_from_contacts);
        fabFromContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, ContactList.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        fabNewContact = (FloatingActionButton) findViewById(R.id.fab_new_contact);
        fabNewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(Profile.this);

                View contactEditView = inflater.inflate(R.layout.contact_edit, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);

                builder.setView(contactEditView);

                final EditText contactName = (EditText) contactEditView.findViewById(R.id.contact_name);
                final EditText contactPhone = (EditText) contactEditView.findViewById(R.id.contact_phone);

                builder
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String setContactName = contactName.getText().toString();
                                String setContactPhone = contactPhone.getText().toString();
                                final Contact contact = new Contact();
                                contact.setName(setContactName);
                                contact.setPhone(setContactPhone);
                                NetworkUtil.updateDB(Profile.this, user, contact, new UpdateAllContactsCallbacks() {
                                    @Override
                                    public void onSuccess() {
                                        adapter.add(contact);
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(Profile.this,
                                                errorMessage,
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.create();

                builder.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings: {
                Intent intent = new Intent(Profile.this, SettingsActivity.class);
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
                intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                startActivityForResult(intent, Constants.REQUEST_SETTINGS);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.RESULT_RESTART) {
            Intent mStartActivity = new Intent(Profile.this, Login.class);
            int mPendingIntentId = 15901590;
            PendingIntent mPendingIntent = PendingIntent.getActivity(Profile.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        /**
         * This overridden method will catch the screen rotation event and will prevent the onCreate
         * function call. Defined in Manifest xml - activity node
         */
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setNavigationBarColor(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
        }
        systemLanguage = newConfig.locale.getLanguage();
        ScreenUtil.setLocale(Profile.this, getString(R.string.title_activity_profile));

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
                                tempContact.setCode(jo.getString("Code"));
                                tempContact.setStatus(jo.getInt("Status"));
                                tempContact.setImage(ContactUtil.retrieveContactPhoto(
                                        tempContact.getPhone(), Profile.this));

                                userContacts.add(tempContact);
                            }
                            showContact();
                        }
                        else{
                            progressDialog.dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(Profile.this,
                                getString(R.string.error_occurred),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(Profile.this,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_LONG).show();
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(Profile.this,
                    getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void showStatus(int statusYes, int statusNo, int statusMaybe) {

        TextView yesRow = (TextView) findViewById(R.id.row_yes);
        yesRow.setText(String.valueOf(statusYes));
        TextView noRow = (TextView) findViewById(R.id.row_no);
        noRow.setText(String.valueOf(statusNo));
        TextView maybeRow = (TextView) findViewById(R.id.row_maybe);
        maybeRow.setText(String.valueOf(statusMaybe));

    }

    private void showContact() {
        ArrayList<Contact> contactsArrayList = new ArrayList<>(userContacts);
        final ListView listView = (ListView) findViewById(R.id.profile_contact_list);
        adapter = new ProfileContactsAdapter(Profile.this,
                R.layout.profile_contact_row, contactsArrayList);
        listView.setAdapter(adapter);

        int statusMaybe = 0, statusNo = 0, statusYes = 0;
        for (Contact contact : contactsArrayList) {
            if (contact.getStatus() < 0)
                statusMaybe++;
            else if (contact.getStatus() == 0)
                statusNo++;
            else
                statusYes += contact.getStatus();
        }


        showStatus(statusYes, statusNo, statusMaybe);


        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final Contact contact = ((Contact) listView.getAdapter().getItem(position));
                LinearLayout rowContainer = (LinearLayout) view.findViewById(R.id.row_container);


                ImageButton deleteContactBtn = (ImageButton) rowContainer.findViewById(R.id.row_remove);
                deleteContactBtn.setVisibility(View.VISIBLE);
                final ImageButton editContactBtn = (ImageButton) rowContainer.findViewById(R.id.row_edit);
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

                        Toast.makeText(Profile.this, contact.getName() + "  EDIT",
                                Toast.LENGTH_LONG).show();


                        LayoutInflater inflater = LayoutInflater.from(Profile.this);

                        View contactEditView = inflater.inflate(R.layout.contact_edit, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);

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

    private void deleteContact(final Contact contact) {
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
                            Toast.makeText(Profile.this, contact.getName() + "  DELETED",
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

    private void editContact(final Contact contact, String contactName, String contactPhone) {
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
                            Toast.makeText(Profile.this, contact.getName() + "  Edited",
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