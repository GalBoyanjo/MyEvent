package com.gal.invitation.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gal.invitation.Entities.Contact;
import com.gal.invitation.Entities.User;
import com.gal.invitation.Interfaces.GeneralRequestCallbacks;
import com.gal.invitation.Interfaces.LoginRequestCallbacks;
import com.gal.invitation.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gal on 01/07/2017.
 */

public class NetworkUtil {

    public static void updateDB(final Context context, User user, Contact contact,
                                final GeneralRequestCallbacks generalRequestCallbacks) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        try {
            Map<String, String> params = new HashMap<>();
            params.put("UserID", String.valueOf(user.getID()));
            params.put("Name", contact.getName());
            params.put("Phone", contact.getPhone());

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    Constants.url_update_contact, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(Constants.TAG_SUCCESS) == 1) {
                            Toast.makeText(context,
                                    context.getString(R.string.contact_saves_in_db),
                                    Toast.LENGTH_LONG).show();
                        }
                        generalRequestCallbacks.onSuccess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        generalRequestCallbacks.onError(context.getString(R.string.error_saving_contact_in_db));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    generalRequestCallbacks.onError(context.getString(R.string.error_saving_contact_in_db));
                }
            });
            requestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            generalRequestCallbacks.onError(context.getString(R.string.error_saving_contact_in_db));
        }

    }

    public static void getUser(final Context context,String url, String email, String password, String userType,
                                final LoginRequestCallbacks loginRequestCallbacks) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        try {
            Map<String, String> params = new HashMap<>();
            params.put("Email", email);
            params.put("Password", password);
            params.put("UserType", userType);

            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(Constants.TAG_SUCCESS) == 1) {
                            Toast.makeText(context,
                                    context.getString(R.string.contact_saves_in_db),
                                    Toast.LENGTH_LONG).show();
                        }
                        User user = new User(jsonObject.getInt("ID"), jsonObject.getString("Username"),
                                jsonObject.getString("Password"), jsonObject.getString("Email"));

                        loginRequestCallbacks.onSuccess(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        loginRequestCallbacks.onError(context.getString(R.string.error_bad_email_password));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    loginRequestCallbacks.onError(context.getString(R.string.error_saving_contact_in_db));
                }
            });
            requestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            loginRequestCallbacks.onError(context.getString(R.string.error_saving_contact_in_db));
        }

    }

    public static void createUser(final Context context,String url, String email, String password,
                                  String userName, String userType, String accountID,
                               final LoginRequestCallbacks loginRequestCallbacks) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        try {
            Map<String, String> params = new HashMap<>();
            params.put("Email", email);
            params.put("Password", password);
            params.put("UserName", userName);
            params.put("UserType", userType);
            params.put("AccountID", accountID);


            MyStringRequest request = new MyStringRequest(Request.Method.POST,
                    url, params, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getInt(Constants.TAG_SUCCESS) == 1) {
                            Toast.makeText(context,
                                    context.getString(R.string.contact_saves_in_db),
                                    Toast.LENGTH_LONG).show();
                        }
                        User user = new User(jsonObject.getInt("ID"), jsonObject.getString("Username"),
                                jsonObject.getString("Password"), jsonObject.getString("Email"));

                        loginRequestCallbacks.onSuccess(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        loginRequestCallbacks.onError(context.getString(R.string.error_bad_email_password));
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error", error.toString());
                    loginRequestCallbacks.onError(context.getString(R.string.error_bad_email_password));
                }
            });
            requestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
            loginRequestCallbacks.onError(context.getString(R.string.error_bad_email_password));
        }

    }

}
