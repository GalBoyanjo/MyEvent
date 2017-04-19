package com.gal.invitation.Screens;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gal.invitation.Utils.JSONParser;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Login extends AppCompatActivity {

    private JSONParser jsonParser;
    private final static String url_get_user = "http://master1590.a2hosted.com/invitations/getUser.php";
    private final static String TAG_SUCCESS = "success";

    private User user;

    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnLogin;
    private TextView lblWelcome;

    int retryGetUser = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        lblWelcome = (TextView) findViewById(R.id.lblWelcome);

        final TextView RegisterLink = (TextView) findViewById(R.id.Register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetUser().execute(txtEmail.getText().toString(),
                        txtPassword.getText().toString());
            }
        });

        RegisterLink.setOnClickListener (new View.OnClickListener(){

            public void onClick(View v){
                Intent RegisterIntent = new Intent(Login.this,Register.class);
                Login.this.startActivity(RegisterIntent);
            }
        });

    }

    class GetUser extends AsyncTask<String, String, Boolean> {

        String email;
        String password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Boolean doInBackground(String... args) {
            try {
                email = args[0];
                password = args[1];

                HashMap<String, String> params = new HashMap<>();
                params.put("Password", password);
                params.put("Email", email);

                jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(url_get_user,
                        "POST", params);


                Log.d("GetUser Response", json.toString());

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
//                lblWelcome.setText(user.getEmail()+ " " + user.getUsername());
//                lblWelcome.setVisibility(View.VISIBLE);
                Intent loginIntent= new Intent(Login.this,Profile.class);
                loginIntent.putExtra("user", user);
                Login.this.startActivity(loginIntent);
                //hide the keyboard
                View view = Login.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            } else {
                if(retryGetUser<2){
                    retryGetUser++;
                    new GetUser().execute(email,password);
                    return;
                }
                Toast.makeText(Login.this,
                        "Error - bad email/password",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


}
