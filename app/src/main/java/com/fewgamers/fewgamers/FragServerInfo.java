package com.fewgamers.fewgamers;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/19/2017.
 */

// fragment that displays all available information on a server
public class FragServerInfo extends Fragment {

    MainActivity mainActivity;

    String thisGame, thisGameUUID, thisServerName, thisServerIP, thisCreator, thisCreatorUUID, additionalInfo;
    TextView thisGameDisplay, thisServerNameDisplay, thisServerIPDisplay, thisServerCreatorDisplay, thisServerAdditionalInfoDisplay;

    public void setServer(ServerObject thisServer) {
        this.thisGameUUID = thisServer.getGameUUID();
        this.thisServerName = thisServer.getServerName();
        this.thisServerIP = thisServer.getIp();
        this.thisCreatorUUID = thisServer.getCreator();
        this.additionalInfo = thisServer.getAdditionalInfo();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragserverinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        this.thisGame = mainActivity.getGameNameFromUUID(this.thisGameUUID);

        // retrieving widgets
        thisGameDisplay = (TextView) getActivity().findViewById(R.id.serverInfoGameDisplay);
        thisServerNameDisplay = (TextView) getActivity().findViewById(R.id.serverInfoServerNameDisplay);
        thisServerIPDisplay = (TextView) getActivity().findViewById(R.id.serverInfoIPAddressDisplay);
        thisServerCreatorDisplay = (TextView) getActivity().findViewById(R.id.serverInfoCreatorUserDisplay);
        thisServerAdditionalInfoDisplay = (TextView) getActivity().findViewById(R.id.serverInfoAdditionalDataDisplay);

        getCreatorAndSetDisplayTexts();

        // clicking the creator's username will open that user's profile
        thisServerCreatorDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragContacts.openProfile(thisCreatorUUID, mainActivity);
            }
        });
    }

    // the creator's username needs to be retrieved first from their uuid. the method to fill out all
    // informative TextViews is then called
    private void getCreatorAndSetDisplayTexts() {
        RequestQueue requestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://fewgamers.com/api/user/?uuid=" + thisCreatorUUID + "&key=" + mainActivity.getMyKey(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject userJSONObject = new JSONObject(response.substring(1, response.length()));
                    thisCreator = userJSONObject.getString("nickname");
                    setDisplayTexts();
                } catch (JSONException exception) {
                    exception.printStackTrace();
                    thisCreator = "Unknown user";
                    setDisplayTexts();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
    }

    private void setDisplayTexts() {
        thisGameDisplay.setText(thisGame);
        thisServerNameDisplay.setText(thisServerName);
        thisServerIPDisplay.setText(thisServerIP);
        thisServerCreatorDisplay.setText(thisCreator);
        thisServerAdditionalInfoDisplay.setText(additionalInfo);
    }
}
