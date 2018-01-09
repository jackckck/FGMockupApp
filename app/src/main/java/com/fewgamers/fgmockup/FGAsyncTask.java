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

public class FGAsyncTask extends AsyncTask<String, Void, String> {
    private String response = "";

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            DataOutputStream streamOut = new DataOutputStream(connection.getOutputStream());

            streamOut.writeBytes(strings[1]);
            streamOut.flush();
            streamOut.close();

            int responseCode = connection.getResponseCode();

            Log.d("Response Code", responseCode + "");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();

            while((line = reader.readLine()) != null) {
                responseOutput.append(line);
            }

            response = responseOutput.toString();
            Log.d("Response Output", response);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}