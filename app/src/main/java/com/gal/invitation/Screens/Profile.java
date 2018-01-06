package com.gal.invitation.Screens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
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
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.Constants;
import com.gal.invitation.Utils.ContactUtil;
import com.gal.invitation.Utils.CustomDialog;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.ScreenUtil;

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



    public static String systemLanguage;
    private RequestQueue netRequestQueue;
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
    private ProgressDialog progressDialog;


    private TextView txtName;



    CountDownTimer countDownTimer;
    long mInitialTime;
    TextView countDownDaysView;
    TextView countDownHoursView;
    TextView countDownMinutesView;
    TextView countDownSecondsView;


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

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_contacts));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        loadContacts();




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

        final LinearLayout createInvitation = (LinearLayout) findViewById(R.id.profile_design_invitation_btn);

        final LinearLayout invitationPreview = (LinearLayout) findViewById(R.id.profile_invitation_preview_btn);

        final LinearLayout manageGuestList = (LinearLayout) findViewById(R.id.profile_edit_guest_btn);

        manageGuestList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent manageGuestIntent = new Intent(Profile.this, ManageGuestList.class);
                manageGuestIntent.putExtra("user", user);
                startActivity(manageGuestIntent);
            }

        });

        createInvitation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent designInvitationIntent = new Intent(Profile.this, CreateInvitation.class);
                designInvitationIntent.putExtra("user", user);
                startActivity(designInvitationIntent);
            }

        });



        invitationPreview.setOnClickListener(new OnClickListener() {
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
            getUserInvitation();
            getUserInvitationPic();
            getUserContacts();

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
                            showGuestStatus();
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

    private void showGuestStatus() {
        ArrayList<Contact> contactsArrayList = new ArrayList<>(userContacts);


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

            countDownDaysView = (TextView) findViewById(R.id.profile_count_down_days);
            countDownHoursView = (TextView) findViewById(R.id.profile_count_down_hours);
            countDownMinutesView = (TextView) findViewById(R.id.profile_count_down_minutes);
            countDownSecondsView = (TextView) findViewById(R.id.profile_count_down_seconds);

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

                    String hoursLeft = String.format("%d", (serverUptimeSeconds % 86400) / 3600);

                    String minutesLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) / 60);

                    String secondsLeft = String.format("%d", ((serverUptimeSeconds % 86400) % 3600) % 60);


                    countDownDaysView.setText(daysLeft);
                    countDownHoursView.setText(hoursLeft);
                    countDownMinutesView.setText(minutesLeft);
                    countDownSecondsView.setText(secondsLeft);

                }

                @Override
                public void onFinish() {
                    countDownDaysView.setText(DateUtils.formatElapsedTime(0));
                    countDownHoursView.setText(DateUtils.formatElapsedTime(0));
                    countDownMinutesView.setText(DateUtils.formatElapsedTime(0));
                    countDownSecondsView.setText(DateUtils.formatElapsedTime(0));

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