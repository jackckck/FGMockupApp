package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static android.util.Base64.*;

/**
 * Created by Administrator on 12/30/2017.
 */


// fragment that allows a user to log in
public class AuthFragLogin extends android.support.v4.app.Fragment {
    EditText emailEdit, passEdit;
    Button loginButton;
    ImageButton visibilityButton;
    TextView loginErrorNotifier;

    Boolean passwordIsVisible;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

    // checks whether a user has previously logged in without logging out
    private boolean isLoggedIn() {
        return loginSharedPreferences.getBoolean("isLoggedIn", false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.authfraglogin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // create an editor to store the user's login data after a successful login
        loginSharedPreferences = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();
        loginEditor.apply();

        // allows a user to stay logged in
        if (isLoggedIn()) {
            allowAccessToMain();
        }

        // retrieve widgets
        emailEdit = (EditText) getActivity().findViewById(R.id.loginEmail);
        passEdit = (EditText) getActivity().findViewById(R.id.loginPass);
        loginButton = (Button) getActivity().findViewById(R.id.loginButton);
        visibilityButton = (ImageButton) getActivity().findViewById(R.id.passwordVisibilityButton);
        loginErrorNotifier = (TextView) getActivity().findViewById(R.id.loginErrorNotifier);

        // password will be invisible by default
        passwordIsVisible = false;

        // checks whether the filled out login data is valid
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin(emailEdit.getText().toString(), passEdit.getText().toString());
            }
        });

        // button that controls the password's visibility
        visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordIsVisible) {
                    passwordIsVisible = false;
                    visibilityButton.setImageResource(R.drawable.ic_login_visibility_off);
                    // makes password text illegible
                    passEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    passwordIsVisible = true;
                    visibilityButton.setImageResource(R.drawable.ic_login_visibility);
                    // makes password text legible
                    passEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    // sends the given email-password combination to the FewGamers server. when a response indicating
    // a successful login is received, the user is moved to an instance of MainActivity
    private void checkLogin(String email, String pass) {
        if (email.equals("") || pass.equals("")) {
            loginErrorNotifier.setText("Required field missing");
            return;
        }

        String loginDataString, encryptedLoginDataString;
        JSONObject loginData = new JSONObject();
        JSONObject finalLogin = new JSONObject();

        try {
            loginData.put("email", email);
            loginData.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // logindata will look like this: {"email":email,"password":pass}
        loginDataString = loginData.toString();

        FGEncrypt encryptor = new FGEncrypt();
        encryptedLoginDataString = encryptor.encrypt(loginDataString);

        try {
            finalLogin.put("logindata", encryptedLoginDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("final login", finalLogin.toString());

        // the user's final login will look like: {"logindata":encrypted logindata}
        new LoginAsyncTask().execute("https://fewgamers.com/api/login/", finalLogin.toString(), "POST");
    }

    // opens an instance of MainActivity
    private void allowAccessToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    // extension of FGAsyncTask, that stores the received login data in SharedPreferences
    private class LoginAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            try {
                // response met alle gebruikersdata. {"uuid":uuid,"email":email,"nickname":username,
                // "firstname":firstName,"lastname":lastName,"key":key,"activationcode":activationCode}
                JSONObject jsonObject = new JSONObject(response[1]);

                // slaat alle accountinformatie op in sharedpreferences
                String uuid = jsonObject.getString("uuid");
                loginEditor.putString("uuid", uuid);
                loginEditor.putString("email", jsonObject.getString("email"));
                loginEditor.putString("username", jsonObject.getString("nickname"));
                loginEditor.putString("firstName", jsonObject.getString("firstname"));
                loginEditor.putString("lastName", jsonObject.getString("lastname"));
                loginEditor.putString("key", jsonObject.getString("key"));
                loginEditor.putString("activationCode", jsonObject.getString("activationcode"));

                // zorgt dat de gebruiker ingelogd blijft
                // wordt weer false wanneer de gebruiker uitlogt
                loginEditor.putBoolean("isLoggedIn", true);

                loginEditor.apply();

                // updatet de firebase token van de ingelogde user in de database
                // de server kan van d.m.v. deze token dit apparaat rechtstreeks een notification sturen
                FGFirebaseInstanceIdService.sendRegistrationToServer(uuid, FirebaseInstanceId.getInstance().getToken(),
                        getResources().getString(R.string.master));

                // stuurt de gebruiker door naar de main activity
                allowAccessToMain();
            } catch (JSONException exception) {
                loginErrorNotifier.setText("Could not log in with what was entered");
                exception.printStackTrace();
            }

        }
    }
}
