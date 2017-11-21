package com.gal.invitation.Screens;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Entities.Invitation;
import com.gal.invitation.Entities.InvitationPic;
import com.gal.invitation.Interfaces.CustomDialogCallback;
import com.gal.invitation.Interfaces.UpdateAllContactsCallbacks;
import com.gal.invitation.Interfaces.UpdateProfileContacts;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.Constants;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.CustomDialog;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.ProfileContactsAdapter;
import com.gal.invitation.Utils.ScreenUtil;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Profile extends AppCompatActivity{

    private ListView listView;

    public static String systemLanguage;
    private RequestQueue netRequestQueue;
    private static boolean hasContactsPermission = false;
    private static final int CONTACTS_PERMISSIONS_REQUEST = 123;
    private final static String url_edit_contact = "http://master1590.a2hosted.com/invitations/editContact.php";
    private final static String url_delete_contact = "http://master1590.a2hosted.com/invitations/deleteContact.php";
    private final static String url_get_contacts = "http://master1590.a2hosted.com/invitations/getUserContacts.php";
    private final static String url_get_invitation = "http://master1590.a2hosted.com/invitations/getUserInvitation.php";
    private final static String url_get_invitation_pic = "http://master1590.a2hosted.com/invitations/getUserInvitationPic.php";

    private InvitationPic userInvitationPic;
    private Invitation userInvitation;
    private final static String TAG_SUCCESS = "success";
    private User user = null;
    private TreeSet<Contact> userContacts = new TreeSet<>(new Comparator<Contact>() {
        @Override
        public int compare(Contact contact, Contact contact2) {
            return contact.getName().compareTo(contact2.getName());
        }
    });
    private ArrayList<Contact> selectedContacts = new ArrayList<>();
    private ProgressDialog progressDialog;
    private AlertDialog editor;

    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabNewContact;
    private FloatingActionButton fabFromContacts;

    private TextView txtName;
    private ProfileContactsAdapter adapter;

    Button yesFilter;
    Button noFilter;
    Button maybeFilter;
    Button allFilter;

    CountDownTimer countDownTimer;
    long mInitialTime;
    TextView countDownView;


    long startTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(Profile.this, getString(R.string.title_activity_profile));
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");

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


        txtName = (TextView) findViewById(R.id.userName);
        txtName.setText(getString(R.string.hello) + " " + String.valueOf(user.getUsername()));



        final LinearLayout sendInvitationsSms = (LinearLayout) findViewById(R.id.profile_send_invitation_btn);

        sendInvitationsSms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(userInvitation.getType()!=null && !userInvitation.getType().isEmpty()
                        && userInvitationPic.getPicture()!=null && !userInvitationPic.getPicture().isEmpty()) {

                    CustomDialog dialog = new CustomDialog(Profile.this, new ArrayList<Contact>(userContacts), new CustomDialogCallback() {
                        @Override
                        public void onSelected(List<Contact> contacts) {

                            Intent sendInvitationLink = new Intent(Profile.this, SendInvitations.class);
                            sendInvitationLink.putExtra("user", user);
                            sendInvitationLink.putExtra("list", new ArrayList<>(contacts));
                            startActivity(sendInvitationLink);

                        }
                    });
                    dialog.setCancelable(true);
                    dialog.show();

//                    Intent sendInvitationLink = new Intent(Profile.this, SendInvitations.class);
//                    sendInvitationLink.putExtra("user", user);
//
//                    sendInvitationLink.putExtra("list", new ArrayList<>(userContacts));
//                    startActivity(sendInvitationLink);

                }
            }
        });

        final RelativeLayout createInvitationPopUP = (RelativeLayout) findViewById(R.id.profile_create_invitation_btn_popup);
        final RelativeLayout createInvitation = (RelativeLayout) findViewById(R.id.profile_create_invitation_btn);
        final RelativeLayout createInvitationDesign = (RelativeLayout) findViewById(R.id.profile_create_invitation_btn_design);
        final RelativeLayout createInvitationPic = (RelativeLayout) findViewById(R.id.profile_create_invitation_btn_pic);
        final RelativeLayout createInvitationPreview = (RelativeLayout) findViewById(R.id.profile_create_invitation_btn_preview);
        final LinearLayout dimLayout = (LinearLayout) findViewById(R.id.dim_layout);
//        createInvitation.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent createInvitationLink = new Intent(Profile.this, CreateInvitation.class);
//                createInvitationLink.putExtra("user", user);
//                startActivity(createInvitationLink);
//            }
//        });

        fabFromContacts = (FloatingActionButton) findViewById(R.id.fab_from_contacts);
        fabFromContacts.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, ContactList.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        fabNewContact = (FloatingActionButton) findViewById(R.id.fab_new_contact);
        fabNewContact.setOnClickListener(new OnClickListener() {
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
                                        finish();
                                        startActivity(getIntent());

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


        // Locate the EditText in listview_main.xml
        yesFilter = (Button) findViewById(R.id.profile_yes_btn);
        noFilter = (Button) findViewById(R.id.profile_no_btn);
        maybeFilter = (Button) findViewById(R.id.profile_maybe_btn);
        allFilter = (Button) findViewById(R.id.profile_all_btn);




        //list filters:

        yesFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("yes");
                yesFilter.setTextColor(getResources().getColor(R.color.colorGreenStatus));
                noFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                maybeFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                allFilter.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        noFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("no");
                noFilter.setTextColor(getResources().getColor(R.color.colorRedStatus));
                yesFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                maybeFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                allFilter.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        maybeFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("maybe");
                maybeFilter.setTextColor(getResources().getColor(R.color.colorYellowStatus));
                noFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                yesFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                allFilter.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });
        allFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.filter("all");
                yesFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                noFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                maybeFilter.setTextColor(getResources().getColor(R.color.colorBlack));
                allFilter.setTextColor(getResources().getColor(R.color.colorBlack));
            }
        });



        createInvitation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dimLayout.getVisibility() == View.INVISIBLE) {

                    createInvitationDesign.setVisibility(View.VISIBLE);
                    createInvitationPic.setVisibility(View.VISIBLE);
                    createInvitationPreview.setVisibility(View.VISIBLE);
                    dimLayout.setVisibility(View.VISIBLE);

                    Animation rotate = AnimationUtils.loadAnimation(Profile.this, R.anim.rotate);
                    TextView plus = (TextView) findViewById(R.id.icon_edit_invitation_plus);
                    plus.startAnimation(rotate);
                }

                else if (dimLayout.getVisibility() != View.INVISIBLE) {

                    createInvitationDesign.setVisibility(View.INVISIBLE);
                    createInvitationPic.setVisibility(View.INVISIBLE);
                    createInvitationPreview.setVisibility(View.INVISIBLE);
                    dimLayout.setVisibility(View.INVISIBLE);

                    Animation rotateBack = AnimationUtils.loadAnimation(Profile.this, R.anim.rotate_back);
                    TextView plus = (TextView) findViewById(R.id.icon_edit_invitation_plus);
                    plus.startAnimation(rotateBack);
                }


            }
        });

        createInvitationPic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPicIntent = new Intent(Profile.this, CreateInvitationPic.class);
                addPicIntent.putExtra("user", user);
                startActivity(addPicIntent);
            }
        });
        createInvitationDesign.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent designInvitationIntent = new Intent(Profile.this, CreateInvitation.class);
                designInvitationIntent.putExtra("user", user);
                startActivity(designInvitationIntent);
            }
        });

        createInvitationPreview.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(userInvitation.getType()!=null && !userInvitation.getType().isEmpty()
                            && userInvitationPic.getPicture()!=null && !userInvitationPic.getPicture().isEmpty()) {

                        Uri uriUrl = Uri.parse("http://master1590.a2hosted.com/invitations/confirmation_page/index.php?Code=" +
                                "&By=" + user.getID());
                        Intent launchBrowser = new Intent();
                        launchBrowser.setAction(Intent.ACTION_VIEW);
                        launchBrowser.addCategory(Intent.CATEGORY_BROWSABLE);
                        launchBrowser.setData(uriUrl);
                        startActivity(launchBrowser);
                    }
                }
            });








        //create popup window
//        createInvitation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                createInvitation.setVisibility(View.INVISIBLE);
//                LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
//                popupView = layoutInflater.inflate(R.layout.create_invitation_pop_up,null);
//
//                popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//                TextView btnClose = (TextView)popupView.findViewById(R.id.pop_up_close);
//
//                btnClose.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        popupWindow.dismiss();
//                        createInvitation.setVisibility(View.VISIBLE);
//                    }
//                });
//
//
//                int[] loc_int = new int[2];
//                createInvitation.getLocationOnScreen(loc_int);
//                Rect location = new Rect();
//                location.left=loc_int[0];
//                location.top=loc_int[1];
//                location.right=location.left+createInvitation.getWidth();
//                location.bottom=location.top+createInvitation.getHeight();
//
//                mCurrentX = 0;
//                mCurrentY = 95;
//
//                popupWindow.showAtLocation(createInvitationPopUP, Gravity.CENTER, mCurrentX,mCurrentY);
//

                //enable move the popup view on touch
//                popupView.setOnTouchListener(new View.OnTouchListener() {
//                    private float mDx;
//                    private float mDy;
//
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        int action = event.getAction();
//                        if (action == MotionEvent.ACTION_DOWN) {
//                            mDx = mCurrentX - event.getRawX();
//                            mDy = mCurrentY - event.getRawY();
//                        } else
//                        if (action == MotionEvent.ACTION_MOVE) {
//                            mCurrentX = (int) (event.getRawX() + mDx);
//                            mCurrentY = (int) (event.getRawY() + mDy);
//                            popupWindow.update(mCurrentX, mCurrentY, -1, -1);
//                        }
//                        return true;
//                    }
//                });
//
//            }
//        });

    }

    public void loadContacts(){
        if (hasContactsPermission) {
            getUserInvitation();
            getUserInvitationPic();
            getUserContacts();

        } else {
            requestContactsPermission();
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
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
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
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
        listView = (ListView) findViewById(R.id.profile_contact_list);
        adapter = new ProfileContactsAdapter(Profile.this,
                R.layout.profile_contact_row, contactsArrayList, new UpdateProfileContacts() {
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

                finish();
                startActivity(getIntent());

            }

            @Override
            public void editContactDialog(final Contact contact) {
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


        progressDialog.dismiss();
    }

    private void sendInvitationsMessage(final ArrayList<Contact> contactArrayList) {


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

    private void getUserInvitationPic() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_get_invitation_pic, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            userInvitationPic =new InvitationPic(jsonObject.getString("Picture"));


                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(Profile.this,
                                getString(R.string.error_occurred),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());

                    Toast.makeText(Profile.this,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_LONG).show();
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(Profile.this,
                    getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void getUserInvitation() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_get_invitation, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            userInvitation=new Invitation(jsonObject.getString("Type")
                                    ,jsonObject.getString("Date") , jsonObject.getString("Time")
                                    , jsonObject.getString("PlaceType") , jsonObject.getString("Place")
                                    , jsonObject.getString("Address") , jsonObject.getString("FreeText")
                                    , jsonObject.getString("Bride") , jsonObject.getString("Groom"));

                            getUserCountDown();

                        }
                        else{

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(Profile.this,
                                getString(R.string.error_occurred),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());

                    Toast.makeText(Profile.this,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_LONG).show();
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(Profile.this,
                    getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void getUserCountDown(){
        if(userInvitation.getType()!=null && !userInvitation.getType().isEmpty()) {

            long milliseconds = 0;
            long diff;

            countDownView = (TextView) findViewById(R.id.profile_count_down);

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
            formatter.setLenient(false);

            String endTime = String.valueOf(userInvitation.getDate()) + ", " + String.valueOf(userInvitation.getTime());

            Date endDate;
            try {
                endDate = formatter.parse(endTime);
                milliseconds = endDate.getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            startTime = System.currentTimeMillis();

            diff = milliseconds - startTime;
//                    DateUtils.DAY_IN_MILLIS * 2 +
//                    DateUtils.HOUR_IN_MILLIS * 9 +
//                    DateUtils.MINUTE_IN_MILLIS * 3 +
//                    DateUtils.SECOND_IN_MILLIS * 42;

            countDownTimer = new CountDownTimer(milliseconds, 1000) {


                @Override
                public void onTick(long l) {
                    startTime=startTime-1;
                    Long serverUptimeSeconds =
                            (l - startTime) / 1000;

                    String daysLeft = String.format("%d", serverUptimeSeconds / 86400);
                    //txtViewDays.setText(daysLeft);


                    String hoursLeft = String.format("%d", (serverUptimeSeconds % 86400) / 3600);
                    //txtViewHours.setText(hoursLeft);

                    String minutesLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) / 60);
                    //txtViewMinutes.setText(minutesLeft);

                    String secondsLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) % 60);
                    //txtViewSecond.setText(secondsLeft);

                    countDownView.setText(daysLeft+" " +"DAY"+"\n"+" "+hoursLeft+":"+minutesLeft+":"+secondsLeft );

                }

                @Override
                public void onFinish() {
                    countDownView.setText(DateUtils.formatElapsedTime(0));
                }
            }.start();

        }

    }

//    public void onRadioButtonClick(View view){
//
//        boolean checked = ((RadioButton)view).isChecked();
//
//        switch (view.getId()) {
//            case R.id.radio_select_all:
//                if(checked) {
//                    Toast.makeText(Profile.this, "ALL!!!", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.radio_select_maybe:
//                if(checked){
//                    Toast.makeText(Profile.this, "MAYBE", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.radio_select_manually:
//                if (checked){
//
//                    final LayoutInflater inflater = LayoutInflater.from(Profile.this);
//
//                    final View sendInvitationListView = inflater.inflate(R.layout.send_invitation_list_dialog, null);
//
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
//
//                    builder.setView(sendInvitationListView);
//
//
//
//                    builder.create();
//
//                    builder.show();
//
//
//
//                }
//                break;
//        }
//
//    }




}