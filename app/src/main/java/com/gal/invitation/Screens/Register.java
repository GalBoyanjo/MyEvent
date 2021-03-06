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
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputLayout;
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
    private final static String url_create_user = "";
    private final static String TAG_SUCCESS = "success";

    private User user;

    private TextInputLayout ReEmail;
    private TextInputLayout ReName;
    private TextInputLayout RePassword;
    private Button btnRegister;
    private TextView lblWelcome;

    boolean allNotEmpty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtil.setLocale(Register.this, getString(R.string.title_activity_register));
        setContentView(R.layout.content_register);
        ReEmail = findViewById(R.id.ReEmail);
        ReName = findViewById(R.id.ReName);
        RePassword = findViewById(R.id.RePassword);
        btnRegister = findViewById(R.id.btnRegister);
        //lblWelcome = (TextView) findViewById(R.id.lblWelcome);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkEmptyField();

                if (checkEmptyField()){
                    if (isValidEmail(ReEmail.getEditText().getText().toString())) {

                        NetworkUtil.createUser(Register.this, url_create_user, ReEmail.getEditText().getText().toString(),
                                RePassword.getEditText().getText().toString(),
                                ReName.getEditText().getText().toString(), "Regular", "",
                                new LoginRequestCallbacks() {

                                    @Override
                                    public void onSuccess(User myUser) {
                                        user = myUser;
                                        Toast.makeText(Register.this,
                                                (getText(R.string.welcome)),
                                                Toast.LENGTH_LONG).show();
                                        Intent registerIntent = new Intent(Register.this, Profile.class);
                                        registerIntent.putExtra("user", user);
                                        Register.this.startActivity(registerIntent);
                                        //hide the keyboard
                                        View view = Register.this.getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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


                    } else {
                        ReEmail.setError(getString(R.string.error_email_not_valid));
                    }
                }

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

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    public boolean checkEmptyField() {
        allNotEmpty = true;

        if(ReEmail.getEditText().getText().toString().isEmpty()) {
            ReEmail.setError(getString(R.string.empty_field));
            allNotEmpty = false;

        }else {
            ReEmail.setError(null);
        }
        if (ReName.getEditText().getText().toString().isEmpty()) {
            ReName.setError(getString(R.string.empty_field));
            allNotEmpty = false;

        }else {
            ReName.setError(null);
        }
        if (RePassword.getEditText().getText().toString().isEmpty()) {
            RePassword.setError(getString(R.string.empty_field));
            allNotEmpty = false;

        }else {
            RePassword.setError(null);
        }

        return allNotEmpty;

    }
}
