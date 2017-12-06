package com.gal.invitation.Screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gal.invitation.Interfaces.LoginRequestCallbacks;
import com.gal.invitation.Utils.Constants;
import com.gal.invitation.Utils.JSONParser;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.ScreenUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;


public class Register extends AppCompatActivity {

    public static String systemLanguage;
    private JSONParser jsonParser;
    private final static String url_create_user = "http://master1590.a2hosted.com/invitations/createUser.php";
    private final static String TAG_SUCCESS = "success";

    private User user;

    private EditText ReEmail;
    private EditText ReName;
    private EditText RePassword;
    private Button btnRegister;
    private TextView lblWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(Register.this, getString(R.string.title_activity_register));
        setContentView(R.layout.content_register);
        ReEmail = (EditText) findViewById(R.id.ReEmail);
        ReName = (EditText) findViewById(R.id.ReName);
        RePassword = (EditText) findViewById(R.id.RePassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setText(getText(R.string.create));
        //lblWelcome = (TextView) findViewById(R.id.lblWelcome);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                new CreateUser().execute(ReEmail.getText().toString(),
//                        ReName.getText().toString(),
//                        RePassword.getText().toString());
                NetworkUtil.createUser(Register.this, url_create_user, ReEmail.getText().toString(), RePassword.getText().toString(),
                        ReName.getText().toString(), "Regular", "",
                        new LoginRequestCallbacks(){

                            @Override
                            public void onSuccess(User myUser) {
                                user = myUser;
                                Toast.makeText(Register.this,
                                        (getText(R.string.welcome)),
                                        Toast.LENGTH_LONG).show();
                                Intent registerIntent= new Intent(Register.this, Profile.class);
                                registerIntent.putExtra("user", user);
                                Register.this.startActivity(registerIntent);
                                //hide the keyboard
                                View view = Register.this.getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }

                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(Register.this,
                                        (getText(R.string.error_bad_email_password)),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }

        });


    }

    public void onConfigurationChanged(Configuration newConfig) {
        /**
         * This overridden method will catch the screen rotation event and will prevent the onCreate
         * function call. Defined in Manifest xml - activity node
         */
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setNavigationBarColor(ContextCompat.getColor(Register.this, R.color.colorPrimary));
        }
        systemLanguage = newConfig.locale.getLanguage();
        ScreenUtil.setLocale(Register.this, getString(R.string.title_activity_register));

    }




    class CreateUser extends AsyncTask<String, String, Boolean> {

        String email;
        String userName;
        String password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Boolean doInBackground(String... args) {
            try {
                email = args[0];
                userName = args[1];
                password = args[2];

                HashMap<String, String> params = new HashMap<>();
                params.put("Password", password);
                params.put("Username", userName);
                params.put("Email", email);

                jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(url_create_user,
                        "POST", params);


                Log.d("CreateUser Response", json.toString());

                // check for success tag

                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    user = new User(json.getInt("ID"),json.getString("Username"), json.getString("Password"),
                            json.getString("Email"));

                    return true;
                } else {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }


        }

        protected void onPostExecute(Boolean result) {
            if (result && user!=null) {
                Toast.makeText(Register.this,
                        (getText(R.string.welcome)),
                        Toast.LENGTH_LONG).show();


                        Intent NewRegisterIntent = new Intent(Register.this,Login.class);
                        Register.this.startActivity(NewRegisterIntent);




            }

            else {
                Toast.makeText(Register.this,
                        (getText(R.string.error_bad_email_password)),
                        Toast.LENGTH_LONG).show();
            }
        }


}


}
