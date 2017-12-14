package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/6/2017.
 */

// hier komt de server browser. momenteel maakt hij al contact met onze server, maar hij levert nog niets zinnigs op
public class FragServerBrowser extends ListFragment {

    ArrayList<String> serverList = new ArrayList<String>();

    ArrayAdapter<String> serverAdapter;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragserverbrowser, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // hier wordt een nieuwe requestview geïnstantieerd als die nog niet bestaat. als hij wel bestaat wordt de bestaande
        // requestview opgehaald.
        final RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        makeStringRequest(requestQueue, "http://www.fewgamers.com/api.php");

        serverAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, serverList);
        setListAdapter(serverAdapter);

        super.onViewCreated(view, savedInstanceState);
    }

    private void makeStringRequest(RequestQueue queue, String url) {

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                TextView tV = (TextView) getActivity().findViewById(R.id.serverBrowserTextView);
                tV.setText("Response is: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView tV = (TextView) getActivity().findViewById(R.id.serverBrowserTextView);
                tV.setText("There was an error.");
            }
        });

        queue.add(stringRequest);
    }
}