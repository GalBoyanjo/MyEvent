package com.gal.invitation.Utils;

/**
 * Created by Gal on 10/04/2017.
 */


import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class MyStringRequest extends StringRequest {

    private Map<String, String> params;

    public MyStringRequest(int method, String url, Map<String, String> params,
                           Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.params = new HashMap<>();
        this.params.putAll(params);
    }

    public MyStringRequest(String url, Map<String, String> params,
                           Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        this.params = new HashMap<>();
        this.params.putAll(params);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}