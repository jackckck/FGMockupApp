package com.fewgamers.fgmockup;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 12/28/2017.
 */

// class that is used to send any HTTP request other than GET. to send a request, one must initialize
// an FGASyncTask object, and call its .execute(url, payload, method). these parameters are called
// upon later by strings[0], strings[1], and string[2]
public class FGAsyncTask extends AsyncTask<String, Void, String[]> {
    @Override
    protected String[] doInBackground(String... strings) {
        // FGAsyncTask responds with a String[], of which response[0] is the response code, and
        // response[1] is the response body
        String[] response = new String[2];
        Integer responseCode;
        boolean errorOccured;
        try {
            URL url = new URL(strings[0]);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(strings[2]);

            // handles the output of a string. this must be skipped for GET requests
            if (!strings[2].equals("GET")) {
                connection.setDoOutput(true);
                DataOutputStream streamOut = new DataOutputStream(connection.getOutputStream());

                streamOut.writeBytes(strings[1]);
                streamOut.flush();
                streamOut.close();
            }
            responseCode = connection.getResponseCode();
            Log.d("Response Code", responseCode + "");

            errorOccured = (responseCode >= 400);
            BufferedReader reader;

            // when an error occurs, a different reader is required
            if (errorOccured) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            String line = "";
            StringBuilder responseOutput = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                responseOutput.append(line);
            }

            response[0] = responseCode.toString();
            response[1] = responseOutput.toString();
            // for debugging
            Log.d("Response Output", response[1]);

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}