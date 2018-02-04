package com.gal.invitation.Screens;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Interfaces.GeneralRequestCallbacks;
import com.gal.invitation.Interfaces.UpdateGuestList;
import com.gal.invitation.R;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.GuestListAdapter;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.ScreenUtil;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

public class ManageGuestList extends AppCompatActivity {

    private RequestQueue netRequestQueue;
    public static String systemLanguage;
    private User user = null;
    private String userType = null;
    private static boolean hasContactsPermission = false;
    private ProgressDialog progressDialog;
    private GuestListAdapter adapter;
    private ListView listView;

    private static final int CONTACTS_PERMISSIONS_REQUEST = 123;
    private final static String TAG_SUCCESS = "success";
    private final static String url_get_contacts = "http://master1590.a2hosted.com/invitations/getUserContacts.php";
    private final static String url_edit_contact = "http://master1590.a2hosted.com/invitations/editContact.php";
    private final static String url_delete_contact = "http://master1590.a2hosted.com/invitations/deleteContact.php";

    private TreeSet<Contact> userContacts = new TreeSet<>(new Comparator<Contact>() {
        @Override
        public int compare(Contact contact, Contact contact2) {
            return contact.getName().compareTo(contact2.getName());
        }
    });



    Button yesFilter;
    Button noFilter;
    Button maybeFilter;
    Button allFilter;

    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabNewContact;
    private FloatingActionButton fabFromContacts;
    private FloatingActionButton fabExcel;

    public static final int FILE_SELECT_CODE = 1212;

    private ArrayList<Contact> excelContacts = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(ManageGuestList.this, getString(R.string.title_activity_manage_guest_list));
        setContentView(R.layout.activity_manage_guest_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.manage_guest_toolbar);
        setSupportActionBar(toolbar);

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

        // Locate the EditText in listview_main.xml
        yesFilter = (Button) findViewById(R.id.profile_yes_btn);
        noFilter = (Button) findViewById(R.id.profile_no_btn);
        maybeFilter = (Button) findViewById(R.id.profile_maybe_btn);
        allFilter = (Button) findViewById(R.id.profile_all_btn);




        //list filters:

        yesFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("yes");
                yesFilter.setTextColor(getResources().getColor(R.color.colorGreenStatus));
                noFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                maybeFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                allFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
            }
        });
        noFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("no");
                noFilter.setTextColor(getResources().getColor(R.color.colorRedStatus));
                yesFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                maybeFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                allFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
            }
        });
        maybeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("maybe");
                maybeFilter.setTextColor(getResources().getColor(R.color.colorYellowStatus));
                noFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                yesFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                allFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
            }
        });
        allFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("all");
                yesFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                noFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                maybeFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                allFilter.setTextColor(getResources().getColor(R.color.colorPrimaryText));
            }
        });


        listView = (ListView) findViewById(R.id.profile_contact_list);

        fabExcel = (FloatingActionButton) findViewById(R.id.fab_new_excel);
        fabExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(ManageGuestList.this, Excel.class);
//                intent.putExtra("user", user);
//                intent.putExtra("userType", userType);
//                startActivity(intent);
                Intent intent = new Intent();
                intent.setType("*/*");
                if (Build.VERSION.SDK_INT < 19) {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent = Intent.createChooser(intent, "Select file");
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    String[] mimetypes = {"xlsx"};
//                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                }
                startActivityForResult(Intent.createChooser(intent, "Select Excel File"), FILE_SELECT_CODE);
            }
        });

        fabFromContacts = (FloatingActionButton) findViewById(R.id.fab_from_contacts);
        fabFromContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageGuestList.this, ContactList.class);
                intent.putExtra("user", user);
                intent.putExtra("userType", userType);
                startActivity(intent);
            }
        });

        fabNewContact = (FloatingActionButton) findViewById(R.id.fab_new_contact);
        fabNewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasContactsPermission) {

                    LayoutInflater inflater = LayoutInflater.from(ManageGuestList.this);

                    View contactEditView = inflater.inflate(R.layout.contact_edit, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ManageGuestList.this);

                    builder.setView(contactEditView);

                    final EditText contactName = (EditText) contactEditView.findViewById(R.id.contact_name);
                    final EditText contactPhone = (EditText) contactEditView.findViewById(R.id.contact_phone);


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
                                        NetworkUtil.updateDB(ManageGuestList.this, user, contact, new GeneralRequestCallbacks() {
                                            @Override
                                            public void onSuccess() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_SELECT_CODE: {
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        if (inputStream==null) {
                            System.out.println("Can't get file");
                            return;
                        }

                        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
                        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
                        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

                        Workbook wb = null;
                        try {
                            wb = new XSSFWorkbook(inputStream);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Sheet datatypeSheet = wb.getSheetAt(0);
                        Iterator<Row> iterator = datatypeSheet.iterator();

                        excelContacts.clear();

                        while (iterator.hasNext()) {

                            Row currentRow = iterator.next();
                            Iterator<Cell> cellIterator = currentRow.iterator();

                            Contact contact = new Contact();
                            int curCell = 0;
                            while (cellIterator.hasNext()) {

                                curCell++;

                                Cell currentCell = cellIterator.next();
                                //getCellTypeEnum shown as deprecated for version 3.15
                                //getCellTypeEnum ill be renamed to getCellType starting from version 4.0
                                if (currentCell.getCellTypeEnum() == CellType.STRING) {
                                    System.out.print(currentCell.getStringCellValue() + "--");
                                } else if (currentCell.getCellTypeEnum() == CellType.NUMERIC) {
                                    System.out.print(currentCell.getNumericCellValue() + "--");
                                }
                                if (curCell == 1){
                                    if (!currentCell.getStringCellValue().isEmpty()){
                                        contact.setName(currentCell.getStringCellValue());
                                    }
                                }
                                if (curCell == 2){
                                    if (!currentCell.getStringCellValue().isEmpty()){
                                        contact.setPhone(currentCell.getStringCellValue());
                                    }
                                }



                            }
                            System.out.println();

                            if ((!contact.getName().isEmpty())&&(!contact.getPhone().isEmpty())) {
                                excelContacts.add(contact);
                            }

                        }
                        excelContacts.size();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            }
            default:
                break;
        }
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
        listView = (ListView) findViewById(R.id.profile_contact_list);
        adapter = new GuestListAdapter(ManageGuestList.this,
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
                                    Toast.makeText(ManageGuestList.this, contact.getName() + "  DELETED",
                                            Toast.LENGTH_LONG).show();
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

                finish();
                startActivity(getIntent());

            }

            @Override
            public void editContactDialog(final Contact contact) {
                LayoutInflater inflater = LayoutInflater.from(ManageGuestList.this);

                View contactEditView = inflater.inflate(R.layout.contact_edit, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(ManageGuestList.this);

                builder.setView(contactEditView);

                final EditText contactName = (EditText) contactEditView.findViewById(R.id.contact_name);
                final EditText contactPhone = (EditText) contactEditView.findViewById(R.id.contact_phone);

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

        progressDialog.dismiss();
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

