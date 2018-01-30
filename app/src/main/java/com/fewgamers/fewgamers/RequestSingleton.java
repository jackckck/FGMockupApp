package com.fewgamers.fewgamers;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Administrator on 12/11/2017.
 */

// throughout the applications runtime, only one RequestQueue for Volley requests is required. by
// calling the getInstance().getRequestQueue() static method from this singleton class, only one
// RequestQueue instance will ever exist
public class RequestSingleton {
    private static RequestSingleton myInstance;
    private RequestQueue myRequestQueue;
    private static Context myContext;

    private RequestSingleton(Context context) {
        myContext = context;
        myRequestQueue = getRequestQueue();
    }

    public static synchronized RequestSingleton getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new RequestSingleton(context);
        }
        return myInstance;
    }

    public RequestQueue getRequestQueue() {
        if (myRequestQueue == null) {
            myRequestQueue = Volley.newRequestQueue(myContext.getApplicationContext());
        }
        return myRequestQueue;
    }
}
