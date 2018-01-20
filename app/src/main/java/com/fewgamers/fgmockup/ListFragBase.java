package com.fewgamers.fgmockup;

import android.app.ListFragment;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by Administrator on 12/24/2017.
 */

public class ListFragBase extends ListFragment {
    MainActivity mainActivity;

    public String formatStringToJSONArray(String string) {
        String res = string;
        for (int i = res.length() - 1; i > 1; i--) {
            if (res.charAt(i) == '{') {
                res = res.substring(0, i) + "," + res.substring(i);
            }
        }
        res = "[" + res + "]";
        return res;
    }

    public String removeHyphens(String uuid) {
        String res = uuid;
        res.replace("-", "");
        return res;
    }

    private void volleyRequestData(RequestQueue requestQueue, String urlString) {
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("/api/user/ error", error.toString());
            }
        });
        requestQueue.add(stringRequest);
    }
}
