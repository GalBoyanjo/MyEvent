package com.gal.invitation.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.gal.invitation.Entities.User;
import com.gal.invitation.Interfaces.GoogleInterface;
import com.gal.invitation.Interfaces.LoginRequestCallbacks;
import com.gal.invitation.R;
import com.gal.invitation.Screens.AppCompatPreferenceActivity;
import com.gal.invitation.Screens.Login;
import com.gal.invitation.Screens.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



public class GoogleHelper extends AppCompatActivity implements GoogleInterface{

    private final static String url_create_user = "http://master1590.a2hosted.com/invitations/createUser.php";
    private final static String url_get_user = "http://master1590.a2hosted.com/invitations/getUser.php";
    private final static String TAG_SUCCESS = "success";

    private User user;

    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 30;
    private Context context;

    @Override
    public void googleLogin(Context context, User user) {


        this.user = user;
        this.context = context;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);


        googleSignIn();
    }

    @Override
    public void googleLogOut(Context context) {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });

    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void getUser(final String email, final String password, final String accountID,
                         final String type, final String userName) {

        NetworkUtil.getUser(context, url_get_user, email, password, type,
                new LoginRequestCallbacks() {
                    @Override
                    public void onSuccess(User myUser) {
                        user = myUser;
                        SaveSharedPreference.setUser(context, email, password, type);
                        Intent loginIntent = new Intent(context, Profile.class);
                        loginIntent.putExtra("user", user);
                        loginIntent.putExtra("userType", type);
                        context.startActivity(loginIntent);
                        //hide the keyboard
//                        View view = context.getCurrentFocus();
//                        if (view != null) {
//                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                        }
                    }

                    @Override
                    public void onError(String errorMessage) {

                        if (type.equals(getString(R.string.user_type_regular))) {
                            Toast.makeText(context,
                                    errorMessage,
                                    Toast.LENGTH_LONG).show();

                        } else if (type.equals(getString(R.string.user_type_google)) || type.equals(getString(R.string.user_type_facebook))) {
                            NetworkUtil.createUser(context, url_create_user, email, password,
                                    userName, type, accountID,
                                    new LoginRequestCallbacks() {
                                        @Override
                                        public void onSuccess(User myUser) {
                                            user = myUser;
                                            Toast.makeText(context,
                                                    (getText(R.string.welcome)),
                                                    Toast.LENGTH_LONG).show();
                                            SaveSharedPreference.setUser(context, email, password, type);
                                            Intent registerIntent = new Intent(context, Profile.class);
                                            registerIntent.putExtra("user", user);
                                            registerIntent.putExtra("userType", type);
                                            context.startActivity(registerIntent);
                                            //hide the keyboard
//                                            View view = context.getCurrentFocus();
//                                            if (view != null) {
//                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                                            }
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Toast.makeText(context,
                                                    (getText(R.string.error_bad_email_password)),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });


                        }
                    }
                });

    }

}
