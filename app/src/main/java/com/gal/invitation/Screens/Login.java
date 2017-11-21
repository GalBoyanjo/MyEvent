package com.gal.invitation.Screens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gal.invitation.Utils.Constants;
import com.gal.invitation.Utils.JSONParser;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.ScreenUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;



import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;



public class Login extends AppCompatActivity{

    public static String systemLanguage;
    private JSONParser jsonParser;
    private final static String url_get_user = "http://master1590.a2hosted.com/invitations/getUser.php";
    private final static String TAG_SUCCESS = "success";

    private User user;

    private EditText txtEmail;
    private EditText txtPassword;
    private Button btnLogin;



    int retryGetUser = 0;


//    private SignInButton signInButton;
//    private GoogleSignInOptions gso;
//    private GoogleApiClient mGoogleApiClient;
//    private int SIGN_IN = 30;
//    private TextView tv;
//    private ImageView iv;
//    private AQuery aQuery;
//    private Button btn;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setLocale(Login.this, getString(R.string.title_activity_login));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        FragmentManager fm = getSupportFragmentManager();
//        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
//
//
//        if (fragment == null) {
//            fragment = new GoogleSignIn();
//            fm.beginTransaction()
//                    .add(R.id.fragment_container, fragment)
//                    .commit();
//        }
        //!!new!!
//        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .addApi(Plus.API)
//                .build();
//
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//                startActivityForResult(signInIntent, SIGN_IN);
//            }
//        });
//
//        tv = (TextView) findViewById(R.id.gplus_tv);
//
//        aQuery = new AQuery(this);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });


        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);


        final TextView RegisterLink = (TextView) findViewById(R.id.Register);
        RegisterLink.setText( getText(R.string.register));


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

    public void onConfigurationChanged(Configuration newConfig) {
        /**
         * This overridden method will catch the screen rotation event and will prevent the onCreate
         * function call. Defined in Manifest xml - activity node
         */
        super.onConfigurationChanged(newConfig);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setNavigationBarColor(ContextCompat.getColor(Login.this, R.color.colorPrimary));
        }
        systemLanguage = newConfig.locale.getLanguage();
        ScreenUtil.setLocale(Login.this, getString(R.string.title_activity_login));

    }

    class GetUser extends AsyncTask<String, String, Boolean> {

        String email;
        String password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Boolean doInBackground(String...  args) {
            try {
                email = args[0];
                password = args[1];

                HashMap<String, String> params = new HashMap<>();
                params.put("Password", password);
                params.put("Email", email);

                jsonParser = new JSONParser();
                JSONObject json = jsonParser.makeHttpRequest(url_get_user,
                        "POST", params);



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
                Intent loginIntent= new Intent(Login.this, Profile.class);
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
                        (getString(R.string.error_bad_email_password)),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //If signin
//        if (requestCode == SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            //Calling a new function to handle signin
//            handleSignInResult(result);
//        }
//    }
//
//    private void handleSignInResult(GoogleSignInResult result) {
//        //If the login succeed
//        if (result.isSuccess()) {
//            //Getting google account
//            final GoogleSignInAccount acct = result.getSignInAccount();
//
//            //Displaying name and email
//            String name = acct.getDisplayName();
//            final String mail = acct.getEmail();
//            // String photourl = acct.getPhotoUrl().toString();
//
//            final String givenname="",familyname="",displayname="",birthday="";
//
//            Plus.PeopleApi.load(mGoogleApiClient, acct.getId()).setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
//                @Override
//                public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
//                    Person person = loadPeopleResult.getPersonBuffer().get(0);
//
//                    Log.d("GivenName ", person.getName().getGivenName());
//                    Log.d("FamilyName ",person.getName().getFamilyName());
//                    Log.d("DisplayName ",person.getDisplayName());
//                    Log.d("ID ",person.getId());
//
//                    Log.d("gender ", String.valueOf(person.getGender())); //0 = male 1 = female
//                    String gender="";
//                    if(person.getGender() == 0){
//                        gender = "Male";
//                    }else {
//                        gender = "Female";
//                    }
//
//                    if(person.hasBirthday()){
//                        tv.setText(person.getName().getGivenName()+" \n"+person.getName().getFamilyName()+" \n"+gender+"\n"+person.getBirthday());
//                    }else {
//                        tv.setText(person.getName().getGivenName()+" \n"+person.getName().getFamilyName()+" \n"+gender);
//
//                    }
//                    //aQuery.id(iv).image(acct.getPhotoUrl().toString());
////                    Log.d("Uriddd",acct.getPhotoUrl().toString());
//                  /*   Log.d(TAG,"CurrentLocation "+person.getCurrentLocation());
//                    Log.d(TAG,"AboutMe "+person.getAboutMe());*/
//                    // Log.d("Birthday ",person.getBirthday());
//                    // Log.d(TAG,"Image "+person.getImage());
//                }
//            });
//        } else {
//            //If login fails
//            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//    }
}
