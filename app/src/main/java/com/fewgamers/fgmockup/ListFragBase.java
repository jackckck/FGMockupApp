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

// extension of ListFragment, from which all other list fragments in this app are extended
public class ListFragBase extends ListFragment {
    MainActivity mainActivity;
}
