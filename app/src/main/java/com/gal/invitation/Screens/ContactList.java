package com.gal.invitation.Screens;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Interfaces.GeneralRequestCallbacks;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.ContactsAdapter;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.ScreenUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.TreeSet;

public class ContactList extends AppCompatActivity {

    public static String systemLanguage;
    private RequestQueue netRequestQueue;

    private SearchView searchView;
    private ListView listView;

    private User user = null;
    private String userType = null;
    private static final String TAG = ContactList.class.getSimpleName();
    private static final int CONTACTS_PERMISSIONS_REQUEST = 123;
    private static final int CONTACT_REQUEST = 1234;
    private static boolean hasContactsPermission = false;
    private ArrayList<Contact> selectedContacts = new ArrayList<>();
    private ProgressDialog progressDialog;
    private int requestsStack = 0;
    ContactsAdapter adapter;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(ContactList.this, getString(R.string.title_activity_contacts_list));
        setContentView(R.layout.activity_contacts_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_contacts));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage((getString(R.string.please_wait)));
        progressDialog.show();

        hasContactsPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");
        userType = getIntent().getStringExtra("userType");


        loadContact();

        // Locate the EditText in listview_main.xml
        //editsearch = (EditText) findViewById(R.id.myActionEditText);


        // Capture Text in EditText
//        editsearch.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void afterTextChanged(Editable arg0) {
//                // TODO Auto-generated method stub
//                String text = editsearch.getText().toString().toLowerCase(Locale.getDefault());
//                adapter.filter(text);
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence arg0, int arg1,
//                                          int arg2, int arg3) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
//                                      int arg3) {
//                // TODO Auto-generated method stub
//            }
//        });
    }

    @Override
    public void onBackPressed(){
        if (selectedContacts.isEmpty())
            checkFinished();
        else {
            //ADD DIALOG
            checkFinished();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contacts_list, menu);

        MenuItem menuSearch = menu.findItem(R.id.action_search);

        //Search related stuff
        searchView = (SearchView) MenuItemCompat.getActionView(menuSearch);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {}
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                //on every change in the search box - filter the list
                adapter.filter(searchQuery.trim());
                listView.invalidate();
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menuSearch, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_ok: {
                if (selectedContacts.isEmpty())
                    checkFinished();
                else {
                    for (Contact contact : selectedContacts) {
                        requestsStack++;
                        NetworkUtil.updateDB(this, user, contact, new GeneralRequestCallbacks() {
                            @Override
                            public void onSuccess() {
                                requestsStack--;
                                checkFinished();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                requestsStack--;
                                checkFinished();
                                Toast.makeText(ContactList.this,
                                        errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                return true;
            }
            case R.id.action_search: {
                return true;
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
            window.setNavigationBarColor(ContextCompat.getColor(ContactList.this, R.color.colorPrimary));
        }
        systemLanguage = newConfig.locale.getLanguage();
        ScreenUtil.setLocale(ContactList.this, getString(R.string.title_activity_contacts_list));

    }

    public void loadContact() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hasContactsPermission) {
                    //    startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT, ContactsContract.Contacts.CONTENT_GROUP_URI), CONTACT_REQUEST);
                    TreeSet<Contact> contacts = new TreeSet<>(new Comparator<Contact>() {
                        @Override
                        public int compare(Contact contact, Contact contact2) {
                            return contact.getName().compareTo(contact2.getName());
                        }
                    });
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    while (phones.moveToNext()) {
                        String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Bitmap photo = ContactUtil.retrieveContactPhoto(phoneNumber, ContactList.this);

                        contacts.add(new Contact(phoneNumber, name, photo));
                    }
                    phones.close();
                    buildListView(contacts);
                } else {
                    requestContactsPermission();
                }
            }
        }, 500);
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
            loadContact();
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
                    loadContact();
                } else {

                    hasContactsPermission = false;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ContactList.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_REQUEST && resultCode == RESULT_OK) {
            String result = data.getExtras().getString("result");
            ArrayList<String> contacts = data.getExtras().getStringArrayList("result");
            Log.d(TAG, "Response: " + data.toString());
           /* uriContact = data.getData();
            Contact contact = new Contact();
            contact.setPhone(retrieveContactNumber());
            contact.setName(retrieveContactName());
            contacts.add(contact);*/

        }
    }

    private void buildListView(final TreeSet<Contact> contacts) {
        ArrayList<Contact> contactsArrayList = new ArrayList<>(contacts);
        listView = (ListView) findViewById(R.id.contacts_list);
        adapter = new ContactsAdapter(ContactList.this,
                R.layout.contact_row, contactsArrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Contact contact = new Contact();
                int i = 0;
                for (Contact c : adapter.searchData) {
                    if (i == position) {
                        contact = c;
                        break;
                    }
                    i++;
                }

                LinearLayout rowContainer = (LinearLayout) view.findViewById(R.id.row_container);
                if (selectedContacts.contains(contact)) {
                    rowContainer.setBackgroundColor(ContextCompat.getColor(ContactList.this, android.R.color.transparent));
                    selectedContacts.remove(contact);
                    contact.setSelected(false);
                } else {
                    rowContainer.setBackgroundColor(ContextCompat.getColor(ContactList.this, R.color.colorAccent));
                    selectedContacts.add(contact);
                    contact.setSelected(true);
                }
            }
        });

        progressDialog.dismiss();
    }

    private void checkFinished() {
        if (requestsStack == 0) {
            Intent guestListIntent = new Intent(ContactList.this, ManageGuestList.class);
            guestListIntent.putExtra("user", user);
            guestListIntent.putExtra("userType", userType);
            startActivity(guestListIntent);
            finish();
        }
    }


//    private String generateCode() {
//        String code = "";
//        char letters[] = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
//        Random rand = new Random();
//        for (int i = 0; i < 16; i++) {
//            if (i != 0 && i != 15)
//                code += letters[rand.nextInt(letters.length - 1)];
//            else
//                code += letters[0];
//        }
//        return code;
//    }

}