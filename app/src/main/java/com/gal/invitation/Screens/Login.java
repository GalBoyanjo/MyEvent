package com.gal.invitation.Screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.gal.invitation.Interfaces.LoginRequestCallbacks;
import com.gal.invitation.Utils.JSONParser;
import com.gal.invitation.R;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.SaveSharedPreference;
import com.gal.invitation.Utils.ScreenUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;


public class Login extends AppCompatActivity {

    public static String systemLanguage;
    private JSONParser jsonParser;
    private final static String url_create_user = "http://master1590.a2hosted.com/invitations/createUser.php";
    private final static String url_get_user = "http://master1590.a2hosted.com/invitations/getUser.php";
    private final static String TAG_SUCCESS = "success";

    private User user;

    private TextInputLayout txtEmail;
    private TextInputLayout txtPassword;
    private Button btnLogin;

    private ProgressDialog progressDialog;
    private ProgressDialog loginDialog;

    int retryGetUser = 0;


    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleSignInButton;
    private int RC_SIGN_IN = 30;

    private LoginButton facebookLoginButton;
    private CallbackManager facebookCallbackManager;

    boolean allNotEmpty;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setLocale(Login.this, getString(R.string.title_activity_login));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        loginDialog = new ProgressDialog(Login.this);
        loginDialog.setTitle(getString(R.string.log_in));
        loginDialog.setCancelable(false);
        loginDialog.setIndeterminate(true);
        loginDialog.setMessage(getString(R.string.please_wait));

        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton = findViewById(R.id.facebook_sign_in_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends"));

        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginDialog.show();

                getUserFacebookDetails(loginResult);

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInButton = findViewById(R.id.google_sign_in_button);

        setGooglePlusButtonText(googleSignInButton, getString(R.string.google_login_btn));


        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginDialog.show();

                googleSignIn();
            }
        });


        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);


        final TextView RegisterLink = findViewById(R.id.Register);
        RegisterLink.setText(getText(R.string.register));



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                new GetUser().execute(txtEmail.getText().toString(),
//                        txtPassword.getText().toString());
//            getUser(txtEmail.getText().toString(),txtPassword.getText().toString(),"","");
                checkEmptyField();
                if(checkEmptyField()) {
                    loginDialog.show();

                    String userType = getString(R.string.user_type_regular);
                    getUser(txtEmail.getEditText().getText().toString(), txtPassword.getEditText().getText().toString(), "", userType, "");
                }
            }
        });

        RegisterLink.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent RegisterIntent = new Intent(Login.this, Register.class);
                Login.this.startActivity(RegisterIntent);
            }
        });


    }

    @Override
    public void onBackPressed(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setIcon(R.mipmap.ic_my_logo);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setMessage(getResources().getString(R.string.are_you_sure_you_want_to_exit));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                System.exit(0);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();


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

    private void getUser(final String email, final String password, final String accountID,
                         final String type, final String userName) {

        NetworkUtil.getUser(Login.this, url_get_user, email, password, type,
                new LoginRequestCallbacks() {
                    @Override
                    public void onSuccess(User myUser) {
                        user = myUser;
                        SaveSharedPreference.setUser(Login.this, email, password, type);
                        Intent loginIntent = new Intent(Login.this, Profile.class);
                        loginIntent.putExtra("user", user);
                        loginIntent.putExtra("userType", type);
                        loginDialog.dismiss();
                        Login.this.startActivity(loginIntent);
                        //hide the keyboard
                        View view = Login.this.getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {

                        if (type.equals(getString(R.string.user_type_regular))) {
                            loginDialog.dismiss();

                            Toast.makeText(Login.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG).show();


                        } else if (type.equals(getString(R.string.user_type_google)) || type.equals(getString(R.string.user_type_facebook))) {
                            NetworkUtil.createUser(Login.this, url_create_user, email, password,
                                    userName, type, accountID,
                                    new LoginRequestCallbacks() {
                                        @Override
                                        public void onSuccess(User myUser) {
                                            user = myUser;
                                            loginDialog.dismiss();
                                            Toast.makeText(Login.this,
                                                    (getText(R.string.welcome)),
                                                    Toast.LENGTH_LONG).show();
                                            SaveSharedPreference.setUser(Login.this, email, password, type);
                                            Intent registerIntent = new Intent(Login.this, Profile.class);
                                            registerIntent.putExtra("user", user);
                                            registerIntent.putExtra("userType", type);
                                            Login.this.startActivity(registerIntent);
                                            //hide the keyboard
                                            View view = Login.this.getCurrentFocus();
                                            if (view != null) {
                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                            }
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            loginDialog.dismiss();

                                            Toast.makeText(Login.this,
                                                    (getText(R.string.error_bad_email_password)),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });


                        }
                    }
                });

    }

    class GetUser extends AsyncTask<String, String, Boolean> {

        String email;
        String password;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Login.this);
            progressDialog.setTitle(getString(R.string.login));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.show();
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


                // check for success tag

                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    user = new User(json.getInt("ID"), json.getString("Username"), json.getString("Password"),
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
            if (result && user != null) {
//                lblWelcome.setText(user.getEmail()+ " " + user.getUserName());
//                lblWelcome.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
                Intent loginIntent = new Intent(Login.this, Profile.class);
                loginIntent.putExtra("user", user);
                Login.this.startActivity(loginIntent);
                //hide the keyboard
                View view = Login.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            } else {
                if (retryGetUser < 2) {
                    retryGetUser++;
                    new GetUser().execute(email, password);
                    return;
                }
                progressDialog.dismiss();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            String userType = getString(R.string.user_type_google);
            getUser(account.getEmail(), account.getId(), account.getId(), userType, account.getDisplayName());
        } catch (ApiException e) {

            loginDialog.dismiss();

            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    protected void getUserFacebookDetails(LoginResult loginResult) {
        GraphRequest data_request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject json_object, GraphResponse response) {
                        try {
                            getUser(json_object.getString("email"),
                                    json_object.getString("id"),
                                    json_object.getString("id"),
                                    (getString(R.string.user_type_facebook)),
                                    json_object.getString("name"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Intent intent = new Intent(MainActivity.this, UserProfile.class);
//                        intent.putExtra("userProfile", json_object.toString());
//                        startActivity(intent);
                    }

                });
        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email");
        data_request.setParameters(permission_param);
        data_request.executeAsync();
    }


    public boolean checkEmptyField() {
        allNotEmpty = true;

        if(txtEmail.getEditText().getText().toString().isEmpty()) {
            txtEmail.setError(getString(R.string.empty_field));
            allNotEmpty = false;

        }else {
            txtEmail.setError(null);
        }

        if (txtPassword.getEditText().getText().toString().isEmpty()) {
            txtPassword.setError(getString(R.string.empty_field));
            allNotEmpty = false;

        }else {
            txtPassword.setError(null);
        }

        return allNotEmpty;

    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //If signin
//        if (requestCode == SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            //Calling a new function to handle signin
//            handleGoogleSignInResult(result);
//        }
//    }
//
//    private void handleGoogleSignInResult(GoogleSignInResult result) {
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
