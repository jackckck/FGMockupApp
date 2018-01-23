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


// Fragment voor login
public class AuthFragLogin extends android.support.v4.app.Fragment {
    EditText emailEdit, passEdit;
    Button loginButton;
    ImageButton visibilityButton;
    Boolean passwordIsVisible;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

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

        loginSharedPreferences = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();
        loginEditor.apply();

        // stay logged in
        if (isLoggedIn()) {
            allowAccessToMain();
        }

        // Instantiëren van widgets
        emailEdit = (EditText) getActivity().findViewById(R.id.loginEmail);
        passEdit = (EditText) getActivity().findViewById(R.id.loginPass);
        loginButton = (Button) getActivity().findViewById(R.id.loginButton);
        visibilityButton = (ImageButton) getActivity().findViewById(R.id.passwordVisibilityButton);
        emailEdit.setText("");
        passEdit.setText("");

        // per default is password onzichtbaar
        passwordIsVisible = false;

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin(emailEdit.getText().toString(), passEdit.getText().toString());
            }
        });

        visibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordIsVisible) {
                    passwordIsVisible = false;
                    visibilityButton.setImageResource(R.drawable.ic_login_visibility_off);
                    passEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    passwordIsVisible = true;
                    visibilityButton.setImageResource(R.drawable.ic_login_visibility);
                    passEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    private void checkLogin(String email, String pass) {
        String loginDataString, encryptedLoginDataString;
        JSONObject loginData = new JSONObject();
        JSONObject finalLogin = new JSONObject();

        try {
            loginData.put("email", email);
            loginData.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // json string in de vorm {"email":email,"password":pass"}
        loginDataString = loginData.toString();


        FGEncrypt encryptor = new FGEncrypt();
        encryptedLoginDataString = encryptor.encrypt(loginDataString);

        try {
            finalLogin.put("logindata", encryptedLoginDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("final login", finalLogin.toString());

        // json string in de vorm {"logindata":encrypted logindata}
        new LoginAsyncTask().execute("https://fewgamers.com/api/login/", finalLogin.toString(), "POST");
    }

    // brengt de gebruiker naar de mainactivity
    private void allowAccessToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    // uitbreiding van FGAsyncTask, zodat de verkregen accountinformatie kan worden opgeslagen in sharedpreferences
    private class LoginAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            try {
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

                // wordt weer false wanneer de gebruiker uitlogt
                loginEditor.putBoolean("isLoggedIn", true);

                loginEditor.apply();

                FGFirebaseInstanceIdService.sendRegistrationToServer(uuid, FirebaseInstanceId.getInstance().getToken(),
                        getResources().getString(R.string.master));
                allowAccessToMain();
            } catch (JSONException exception) {
                Log.e("Incorrect user data", "Retrieved user login data was incomplete");
            }

        }
    }
}
