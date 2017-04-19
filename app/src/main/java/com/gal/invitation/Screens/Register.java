package com.gal.invitation.Screens;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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


public class Register extends AppCompatActivity {

    private JSONParser jsonParser;
    private final static String url_get_user = "http://master1590.a2hosted.com/invitations/createUser.php";
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
        setContentView(R.layout.content_register);
        ReEmail = (EditText) findViewById(R.id.ReEmail);
        ReName = (EditText) findViewById(R.id.ReName);
        RePassword = (EditText) findViewById(R.id.RePassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        //lblWelcome = (TextView) findViewById(R.id.lblWelcome);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CreateUser().execute(ReEmail.getText().toString(),
                        ReName.getText().toString(),
                        RePassword.getText().toString());
            }

        });


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
                JSONObject json = jsonParser.makeHttpRequest(url_get_user,
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
                        "WELCOME",
                        Toast.LENGTH_LONG).show();


                        Intent NewRegisterIntent = new Intent(Register.this,Login.class);
                        Register.this.startActivity(NewRegisterIntent);




            }

            else {
                Toast.makeText(Register.this,
                        "Error - bad email/password",
                        Toast.LENGTH_LONG).show();
            }
        }


}


}
