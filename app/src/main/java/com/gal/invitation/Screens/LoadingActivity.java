package com.gal.invitation.Screens;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gal.invitation.Entities.User;
import com.gal.invitation.Interfaces.LoginRequestCallbacks;
import com.gal.invitation.Interfaces.VersionRequestCallbacks;
import com.gal.invitation.R;
import com.gal.invitation.Utils.NetworkUtil;
import com.gal.invitation.Utils.SaveSharedPreference;
import com.github.ybq.android.spinkit.style.Wave;

public class LoadingActivity extends AppCompatActivity {

    private final static String url_get_user = "http://master1590.a2hosted.com/invitations/getUser.php";
    private final static String url_get_version = "http://master1590.a2hosted.com/invitations/getVersion.php";
    private User user;

    ImageView myLogo;
    int retry = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);



        myLogo = findViewById(R.id.my_logo_loading);


        Animation expand = AnimationUtils.loadAnimation(this, R.anim.expand_logo);
        myLogo.startAnimation(expand);

        ProgressBar progressBar = findViewById(R.id.wave);
        Wave wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);

        LinearLayout linearLayout = findViewById(R.id.activity_loading_background);
        AnimationDrawable animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

//                NetworkUtil.getVersion(LoadingActivity.this, url_get_version, getString(R.string.app_version),
//                        new VersionRequestCallbacks() {
//                            @Override
//                            public void onSuccess(String currentVersion) {
//                                try {
//                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
////                            if(pInfo.versionCode>2);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                if(!(currentVersion.equals(getString(R.string.app_version)))){
////                            Toast.makeText(LoadingActivity.this,
////                                    "need to update", Toast.LENGTH_LONG).show();
//                                }else{
//                                    getSharedPreference();
//                                }
//
//                            }
//                            @Override
//                            public void onError(String errorMessage) {
//
//                            }
//                        });
                getSharedPreference();


            }
        }, 3000);




    }

    private void getSharedPreference(){
        if (SaveSharedPreference.getUserEmail(LoadingActivity.this).length() == 0) {
            // call Login Activity
            Intent loginIntent = new Intent(LoadingActivity.this , Login.class);
            LoadingActivity.this.startActivity(loginIntent);

        } else {
            NetworkUtil.getUser(LoadingActivity.this, url_get_user,
                    SaveSharedPreference.getUserEmail(LoadingActivity.this),
                    SaveSharedPreference.getUserPassword(LoadingActivity.this),
                    SaveSharedPreference.getUserType(LoadingActivity.this),
                    new LoginRequestCallbacks() {
                        @Override
                        public void onSuccess(User myUser) {
                            user = myUser;
                            Intent profileIntent = new Intent(LoadingActivity.this, Profile.class);
                            profileIntent.putExtra("user", user);
                            profileIntent.putExtra("userType", SaveSharedPreference.getUserType(LoadingActivity.this));
//                            loginDialog.dismiss();
                            LoadingActivity.this.startActivity(profileIntent);
                            //hide the keyboard
                            View view = LoadingActivity.this.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                        }

                        @Override
                        public void onError(String errorMessage) {
                            retry++;
                            if(retry < 3) {
                                getSharedPreference();
                            } else{
                                Toast.makeText(LoadingActivity.this,
                                        errorMessage,
                                        Toast.LENGTH_SHORT).show();

                                Intent loginIntent = new Intent(LoadingActivity.this , Login.class);
                                String transitionName = getString(R.string.loadingTransitionName);

                                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(LoadingActivity.this, myLogo, transitionName);
                                startActivity(loginIntent, transitionActivityOptions.toBundle());



                            }

                        }
                    });

        }
    }


}
