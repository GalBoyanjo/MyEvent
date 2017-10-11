package com.gal.invitation.Screens;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Entities.Invitation;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Interfaces.UpdateAllContactsCallbacks;
import com.gal.invitation.R;
import com.gal.invitation.Utils.MyStringRequest;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.ScreenUtil;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;

public class CreateInvitation extends AppCompatActivity {

    public static String systemLanguage;
    private RequestQueue netRequestQueue;
    private final static String url_update_invitation = "http://master1590.a2hosted.com/invitations/updateInvitation.php";
    private final static String url_get_invitation = "http://master1590.a2hosted.com/invitations/getUserInvitation.php";
    private final static String TAG_SUCCESS = "success";
    private User user = null;
    Button date;
    Button time;
    EditText eventPlaceName;
    EditText groomName;
    EditText brideName;
    EditText parentsName;
    EditText freeText;
    EditText eventAddress;
    Spinner eventType;
    Spinner eventPlaceType;
    DatePickerDialog datePickerDialog;

    boolean allNotEmpty;
    private Invitation userInvitation;
    private ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(CreateInvitation.this, getString(R.string.title_activity_create_invitations));
        setContentView(R.layout.activity_create_invitation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        netRequestQueue = Volley.newRequestQueue(this);

        user = (User) getIntent().getSerializableExtra("user");


        eventPlaceName = (EditText)findViewById(R.id.EventPlace);
        groomName = (EditText)findViewById(R.id.eventGroom);
        brideName = (EditText)findViewById(R.id.eventBride);
        parentsName = (EditText)findViewById(R.id.eventParents);
        freeText  =(EditText)findViewById(R.id.EventText);
        eventAddress = (EditText)findViewById(R.id.EventPlaceAddress);
        date = (Button) findViewById(R.id.eventDate);
        eventType = (Spinner) findViewById(R.id.eventType);
        time = (Button) findViewById(R.id.eventTime);
        eventPlaceType = (Spinner) findViewById(R.id.eventPlaceType);



        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("LOADING INVITATION");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        getUserInvitation();




        createEventType();
        createEventDate();
        createEventTime();
        createEventPlaceType();




    }

    public void createEventType(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(CreateInvitation.this,
                R.array.type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        eventType.setAdapter(adapter);


        eventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;

            }
        });


    }

    public void createEventDate(){
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int eventYear = calendar.get(Calendar.YEAR);
                int eventMonth = calendar.get(Calendar.MONTH);
                int eventDay = calendar.get(Calendar.DAY_OF_MONTH);


                datePickerDialog = new DatePickerDialog(CreateInvitation.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                date.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, eventYear, eventMonth, eventDay);
                datePickerDialog.show();
            }
        });
    }

    public void createEventTime(){

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar eventTime = Calendar.getInstance();
                int hour = eventTime.get(Calendar.HOUR_OF_DAY);
                int minute = eventTime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(CreateInvitation.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if(selectedMinute<10)
                            time.setText((selectedHour + ":" + "0" + selectedMinute));
                        else
                            time.setText((selectedHour + ":" + selectedMinute));
                    }
                }, hour, minute, true);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

    }

    public void createEventPlaceType(){

        ArrayAdapter<CharSequence> placeTypeAdapter = ArrayAdapter.createFromResource(CreateInvitation.this,
                R.array.placeType, android.R.layout.simple_spinner_item);

        placeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        eventPlaceType.setAdapter(placeTypeAdapter);


        eventPlaceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventPlaceTypeSelected(eventPlaceType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_invitation, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ok) {
            if(checkEmptyField()) {

                updateDB();
                Intent AddPicIntent = new Intent(CreateInvitation.this, CreateInvitationPic.class);
                AddPicIntent.putExtra("user", user);
                startActivity(AddPicIntent);
                finish();
            }
        }

//            if (selectedContacts.isEmpty())
//                checkFinished();
//            else
//                for (Contact contact : selectedContacts) {
//                    requestsStack++;
//                    updateDB(contact);
        return true;
//        }
//
//        return super.onOptionsItemSelected(item);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        /**
         * This overridden method will catch the screen rotation event and will prevent the onCreate
         * function call. Defined in Manifest xml - activity node
         */
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setNavigationBarColor(ContextCompat.getColor(CreateInvitation.this, R.color.colorPrimary));
        }
        systemLanguage = newConfig.locale.getLanguage();
        ScreenUtil.setLocale(CreateInvitation.this, getString(R.string.title_activity_create_invitations));

    }

    public void eventSelected(){
        String type = eventType.getSelectedItem().toString();


        if (type.equals(getString(R.string.wedding))){
            groomName.setVisibility(View.VISIBLE);
            brideName.setVisibility(View.VISIBLE);
            parentsName.setVisibility(View.GONE);
        }
        if (type.equals(getString(R.string.hina))){
            groomName.setVisibility(View.VISIBLE);
            brideName.setVisibility(View.VISIBLE);
            parentsName.setVisibility(View.GONE);
            }
        if (type.equals(getString(R.string.other))) {
            groomName.setVisibility(View.GONE);
            brideName.setVisibility(View.GONE);
            parentsName.setVisibility(View.VISIBLE);
        }
    }

    public void eventPlaceTypeSelected(Spinner eventPlaceType){

    }

    public boolean checkEmptyField(){
        allNotEmpty = true;
        
        if (date.getText().toString().isEmpty()) {
            date.setError(getString(R.string.empty_field));
            allNotEmpty = false;
        } else {
            date.setError(null);
        }
        if (time.getText().toString().isEmpty()) {
            time.setError(getString(R.string.empty_field));
            allNotEmpty = false;
        } else {
            time.setError(null);
        }

        checkField(eventPlaceName);
        if(eventType.getSelectedItem().toString().equals(getString(R.string.wedding))){
            checkField(groomName);
            checkField(brideName);
        }
        if (eventType.getSelectedItem().toString().equals(getString(R.string.other)))
            checkField(parentsName);



        return allNotEmpty;
    }

    public void checkField(EditText field){

        if (field.getText().toString().isEmpty()) {
            field.setError(getString(R.string.empty_field));
            allNotEmpty = false;
        } else {
            field.setError(null);
        }
    }

    private void updateDB() {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));
            params.put("Type", eventType.getSelectedItem().toString());
            params.put("Date", date.getText().toString());
            params.put("Time", time.getText().toString());
            params.put("PlaceType", eventPlaceType.getSelectedItem().toString());
            params.put("Place", eventPlaceName.getText().toString());
            params.put("Address", eventAddress.getText().toString());
            params.put("FreeText", freeText.getText().toString());
            params.put("Bride", brideName.getText().toString());
            params.put("Groom", groomName.getText().toString());

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url_update_invitation, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(TAG_SUCCESS) == 1) {
                            Toast.makeText(CreateInvitation.this,
                                    (getString(R.string.contact_saves_in_db)),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CreateInvitation.this,
                                (getString(R.string.error_saving_contact_in_db)),
                                Toast.LENGTH_LONG).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    Toast.makeText(CreateInvitation.this,
                            (getString(R.string.error_saving_contact_in_db)),
                            Toast.LENGTH_LONG).show();

                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(CreateInvitation.this,
                    (getString(R.string.error_saving_contact_in_db)),
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

                            showInvitation();
                        }
                        else{
                            progressDialog.dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(CreateInvitation.this,
                                getString(R.string.error_occurred),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    progressDialog.dismiss();
                    Toast.makeText(CreateInvitation.this,
                            getString(R.string.error_occurred),
                            Toast.LENGTH_LONG).show();
                }
            });
            netRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(CreateInvitation.this,
                    getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

    }

    private void showInvitation(){

        //params.put("Type", eventType.getSelectedItem().toString());
        //if(eventPlaceType.getSelectedItem().toString()==userInvitation.getPlace())

        date.setText(String.valueOf(userInvitation.getDate()));
        time.setText(String.valueOf(userInvitation.getTime()));
        eventPlaceName.setText(String.valueOf(userInvitation.getPlace()));
        eventAddress.setText(String.valueOf(userInvitation.getAddress()));
        freeText.setText(String.valueOf(userInvitation.getFreeText()));
        brideName.setText(String.valueOf(userInvitation.getBride()));
        groomName.setText(String.valueOf(userInvitation.getGroom()));


        progressDialog.dismiss();
    }

}
