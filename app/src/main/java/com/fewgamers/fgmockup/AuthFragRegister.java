package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/30/2017.
 */

public class AuthFragRegister extends android.support.v4.app.Fragment {
    EditText registerUsernameDisplay, registerFirstNameDisplay, registerLastNameDisplay,
            registerEmailDisplay, registerPasswordDisplay;
    Button registerButton;

    String username, firstName, lastName, email, password;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.authfragregister, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerUsernameDisplay = getActivity().findViewById(R.id.registerUsernameEdit);
        registerFirstNameDisplay = getActivity().findViewById(R.id.registerFirstNameEdit);
        registerLastNameDisplay = getActivity().findViewById(R.id.registerLastNameEdit);
        registerEmailDisplay = getActivity().findViewById(R.id.registerEmailEdit);
        registerPasswordDisplay = getActivity().findViewById(R.id.registerPasswordEdit);

        registerButton = getActivity().findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(registerEmailDisplay.getText().toString(), registerUsernameDisplay.getText().toString(),
                        registerPasswordDisplay.getText().toString(), registerFirstNameDisplay.getText().toString(),
                        registerLastNameDisplay.getText().toString());
            }
        });
    }

    private void registerUser(String email, String username, String password, String firstName, String lastName) {
        if (username.equals(null) || email.equals(null) || password.equals(null) || username.equals("") || email.equals("") || password.equals("")) {
            Toast.makeText(getActivity(), "Required field missing", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject registerObject = new JSONObject();
            registerObject.put("email", email);
            registerObject.put("username", username);
            registerObject.put("password", password);
            registerObject.put("firstname", firstName);
            registerObject.put("lastname", lastName);
            Log.d("register", registerObject.toString());
            new RegisterAsyncTask().execute("https:/fewgamers.com/register/", registerObject.toString(), "POST");
        } catch (JSONException exception) {
            exception.printStackTrace();
            Toast.makeText(getActivity(), "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private class RegisterAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            switch (response[0]) {
                case "201":
                    Toast.makeText(getActivity(), "Registration succesful", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
