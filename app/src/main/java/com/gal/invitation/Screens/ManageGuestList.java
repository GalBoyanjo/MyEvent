package com.gal.invitation.Screens;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Excel;
import com.gal.invitation.Interfaces.GeneralRequestCallbacks;
import com.gal.invitation.Interfaces.GuestUpdateCallbacks;
import com.gal.invitation.Interfaces.UpdateGuestList;
import com.gal.invitation.R;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.RecycleGuestListAdapter;
import com.gal.invitation.Utils.ScreenUtil;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class ManageGuestList extends AppCompatActivity {

    private RequestQueue netRequestQueue;
    public static String systemLanguage;
    private User user = null;
    private String userType = null;
    private static boolean hasContactsPermission = false;
    private ProgressDialog progressDialog;
    //    private GuestListAdapter adapter;
    private RecycleGuestListAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager myLayoutManager;

    private static final int CONTACTS_PERMISSIONS_REQUEST = 123;
    private final static String TAG_SUCCESS = "success";
    private final static String url_get_contacts = "";
    private final static String url_edit_contact = "";
    private final static String url_delete_contact = "";

    private TreeSet<Contact> userContacts = new TreeSet<>(new Comparator<Contact>() {
        @Override
        public int compare(Contact contact, Contact contact2) {
            return contact.getName().compareTo(contact2.getName());
        }
    });

    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabNewContact;
    private FloatingActionButton fabFromContacts;
    private FloatingActionButton fabExcel;


    private ArrayList<Contact> excelContacts = new ArrayList<>();

    boolean hasGuests = false;
    private static final String FILE_DIRECTORY = "/MyEvent";
    private int requestsStack = 0;

    View myView;

    DividerItemDecoration mDividerItemDecoration;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(ManageGuestList.this, getString(R.string.title_activity_manage_guest_list));
        setContentView(R.layout.activity_manage_guest_list);

        Toolbar toolbar = findViewById(R.id.manage_guest_toolbar);
        setSupportActionBar(toolbar);

        // Create an instance of the tab layout from the view.
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        // Set the text for each tab.
        tabLayout.addTab(tabLayout.newTab().setText(R.string.all));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.yes));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.maybe));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.no));

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");
        userType = getIntent().getStringExtra("userType");


        hasContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_contacts));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        loadContacts();

        myView = findViewById(R.id.manage_guest_list_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:{
                        adapter.filter("all");
                        return;
                    }
                    case 1:{
                        adapter.filter("yes");
                        return;
                    }
                    case 2:{
                        adapter.filter("maybe");
                        return;
                    }
                    case 3:{
                        adapter.filter("no");
                        return;
                    }
                    default:{
                        adapter.filter("all");
                        return;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        recyclerView = findViewById(R.id.guest_list);

        //Add divider for list
        mDividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL
        );
        recyclerView.addItemDecoration(mDividerItemDecoration);

        fabExcel = findViewById(R.id.fab_excel);
        fabExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(ManageGuestList.this, Excel.class);

                View sharedView = fabExcel;
                String transitionName = getString(R.string.excelTransitionName);

                intent.putExtra("user", user);
                intent.putExtra("userType", userType);
                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(ManageGuestList.this, sharedView, transitionName);
                startActivity(intent, transitionActivityOptions.toBundle());


//                Intent intent = new Intent();
//                intent.setType("*/*");
//                if (Build.VERSION.SDK_INT < 19) {
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent = Intent.createChooser(intent, "Select file");
//                } else {
//                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                }
//                startActivityForResult(Intent.createChooser(intent, "Select Excel File"), FILE_SELECT_CODE);
            }
        });


        fabFromContacts = findViewById(R.id.fab_from_contacts);
        fabFromContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageGuestList.this, ContactList.class);
                intent.putExtra("user", user);
                intent.putExtra("userType", userType);
                startActivity(intent);
            }
        });

        fabNewContact = findViewById(R.id.fab_new_contact);
        fabNewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasContactsPermission) {

                    LayoutInflater inflater = LayoutInflater.from(ManageGuestList.this);

                    View contactEditView = inflater.inflate(R.layout.contact_edit, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ManageGuestList.this);

                    builder.setView(contactEditView);

                    final EditText contactName = contactEditView.findViewById(R.id.contact_name);
                    final EditText contactPhone = contactEditView.findViewById(R.id.contact_phone);


                    builder
                            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String setContactName = contactName.getText().toString();
                                    String setContactPhone = contactPhone.getText().toString();

                                    if(contactName.getText().toString().isEmpty()||
                                            contactPhone.getText().toString().isEmpty()){

                                        Toast.makeText(ManageGuestList.this,
                                                getString(R.string.empty_field),
                                                Toast.LENGTH_LONG).show();

                                    }else {
                                        final Contact contact = new Contact();
                                        contact.setName(setContactName);
                                        contact.setPhone(setContactPhone);
                                        NetworkUtil.updateGuest(ManageGuestList.this, user, contact, new GuestUpdateCallbacks() {
                                            @Override
                                            public void onSuccess(Contact contact) {
                                                if (hasGuests) {
                                                    adapter.add(contact);
                                                    adapter.notifyDataSetChanged();
                                                }
                                                finish();
                                                startActivity(getIntent());

                                            }

                                            @Override
                                            public void onError(String errorMessage) {
                                                Toast.makeText(ManageGuestList.this,
                                                        errorMessage,
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

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

                } else {
                    requestContactsPermission();
                }
            }
        });


    }

    @Override
    public void onBackPressed(){
        Intent profileIntent = new Intent(ManageGuestList.this, Profile.class);
        profileIntent.putExtra("user", user);
        profileIntent.putExtra("userType", userType);
        startActivity(profileIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_guest_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_ok_guest_list: {
                Intent profileIntent = new Intent(ManageGuestList.this, Profile.class);
                profileIntent.putExtra("user", user);
                profileIntent.putExtra("userType", userType);
                startActivity(profileIntent);
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
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
            window.setNavigationBarColor(ContextCompat.getColor(ManageGuestList.this, R.color.colorPrimary));
        }
        systemLanguage = newConfig.locale.getLanguage();
        ScreenUtil.setLocale(ManageGuestList.this, getString(R.string.title_activity_manage_guest_list));

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
                                        tempContact.getPhone(), ManageGuestList.this));

                                userContacts.add(tempContact);
                            }
                            hasGuests = true;
                            showContact();
                        }
                        else{
                            progressDialog.dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(ManageGuestList.this,
                                getString(R.string.error_occurred),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(ManageGuestList.this,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_LONG).show();
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(ManageGuestList.this,
                    getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACTS_PERMISSIONS_REQUEST);

        } else {
            hasContactsPermission = true;
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CONTACTS_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    hasContactsPermission = true;
                    loadContacts();
                } else {

                    hasContactsPermission = false;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ManageGuestList.this);
                    builder.setTitle(getResources().getString(R.string.required_permission_title));
                    builder.setMessage(getResources().getString(R.string.required_contacts_permission_message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(getResources().getString(R.string.required_permission_ask_again), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            requestContactsPermission();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            progressDialog.dismiss();
                        }
                    });
                    builder.show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showContact() {
        ArrayList<Contact> contactsArrayList = new ArrayList<>(userContacts);
        recyclerView = findViewById(R.id.guest_list);
        myLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(myLayoutManager);
        adapter = new RecycleGuestListAdapter(ManageGuestList.this,
                R.layout.guest_list_row, contactsArrayList, new UpdateGuestList() {
            @Override
            public void deleteContact(final Contact contact) {
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
                                    Snackbar.make(myView, contact.getName() + " " + getString(R.string.deleted),Snackbar.LENGTH_LONG).
                                            setAction(getString(R.string.undo), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    NetworkUtil.updateGuest(ManageGuestList.this, user, contact, new GuestUpdateCallbacks() {
                                                        @Override
                                                        public void onSuccess(Contact contact) {
                                                            adapter.add(contact);
                                                            adapter.notifyDataSetChanged();
                                                            finish();
                                                            startActivity(getIntent());

                                                        }

                                                        @Override
                                                        public void onError(String errorMessage) {
                                                            Toast.makeText(ManageGuestList.this,
                                                                    errorMessage,
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                                }
                                            }).show();
                                    finish();
                                    startActivity(getIntent());

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(ManageGuestList.this,
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

//                finish();
//                startActivity(getIntent());

            }

            @Override
            public void editContactDialog(final Contact contact) {
                LayoutInflater inflater = LayoutInflater.from(ManageGuestList.this);

                View contactEditView = inflater.inflate(R.layout.contact_edit, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ManageGuestList.this);

                builder.setView(contactEditView);

                final EditText contactName = contactEditView.findViewById(R.id.contact_name);
                final EditText contactPhone = contactEditView.findViewById(R.id.contact_phone);

                contactName.setHint(String.valueOf(contact.getName()));
                contactPhone.setHint(String.valueOf(contact.getPhone()));

                builder
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
        recyclerView.setAdapter(adapter);

        int statusMaybe = 0, statusNo = 0, statusYes = 0;
        for (Contact contact : contactsArrayList) {
            if (contact.getStatus() < 0)
                statusMaybe++;
            else if (contact.getStatus() == 0)
                statusNo++;
            else
                statusYes += contact.getStatus();
        }

        progressDialog.dismiss();

        int resId = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
        recyclerView.setLayoutAnimation(animation);
    }

    public void loadContacts() {
        if (hasContactsPermission) {
            getUserContacts();
        } else {
            requestContactsPermission();
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
                            Toast.makeText(ManageGuestList.this, contact.getName() + "  Edited",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ManageGuestList.this,
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

