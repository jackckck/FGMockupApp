package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Administrator on 12/30/2017.
 */

public class LoginFragRegister extends android.support.v4.app.Fragment {
    EditText registerUsernameDisplay, registerFirstNameDisplay, registerLastNameDisplay,
            registerEmailDisplay, registerPasswordDisplay;
    Button registerButton;
    TextView registerFail;

    String username, firstName, lastName, email, password;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loginfragregister, container, false);
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
                registerUser();
            }
        });

        registerFail = getActivity().findViewById(R.id.registerFailText);
    }

    private void registerUser() {
        username = registerUsernameDisplay.getText().toString();
        firstName = registerFirstNameDisplay.getText().toString();
        lastName = registerLastNameDisplay.getText().toString();
        email = registerEmailDisplay.getText().toString();
        password = registerPasswordDisplay.getText().toString();

        if (username.equals(null) || email.equals(null) || password.equals(null) || username.equals("") || email.equals("") || password.equals("")) {
            registerFail.setText("Incomplete user data");
            return;
        }

        new RegisterAsyncTask().execute("http://www.fewgamers.com/api/register/", "");
    }

    private class RegisterAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String response) {
            registerAlert();
        }
    }

    private void registerAlert() {
        registerFail.setText("");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Confirmation email has been sent");
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(850, 220);
    }
}
