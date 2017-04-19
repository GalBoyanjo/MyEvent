package com.gal.invitation.Screens;

import android.app.ProgressDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    private TextView txtName;

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

        txtName=(TextView)findViewById(R.id.userName);
        txtName.setText("HELLO "+String.valueOf(user.getUsername()));


    }

    private void getUserContacts(){
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
                            for(int i=0; i<ja.length(); i++){
                                JSONObject jo = ja.getJSONObject(i);
                                Contact tempContact = new Contact();
                                tempContact.setName(jo.getString("Name"));
                                tempContact.setPhone(jo.getString("Phone"));
                                tempContact.setImage(ContactUtil.retrieveContactPhoto(
                                        tempContact.getPhone(),Profile.this));

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Contact contact = ((Contact) listView.getAdapter().getItem(position));
                LinearLayout rowContainer = (LinearLayout) view.findViewById(R.id.row_container);
                Toast.makeText(Profile.this,contact.getName(),Toast.LENGTH_LONG).show();
            }
        });

        progressDialog.dismiss();
    }

}
