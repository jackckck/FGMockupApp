package com.fewgamers.fgmockup;

import android.app.Notification;
import android.app.NotificationManager;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

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

public class AuthFragLogin extends android.support.v4.app.Fragment {
    EditText emailEdit, passEdit;
    Button login;
    ImageButton visibility;
    String email, pass, encryptKey, iv;
    Boolean passwordIsVisible;
    CheckBox check;

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

        if (isLoggedIn()) {
            allowAccess();
        }

        emailEdit = (EditText) getActivity().findViewById(R.id.loginEmail);
        passEdit = (EditText) getActivity().findViewById(R.id.loginPass);
        login = (Button) getActivity().findViewById(R.id.loginButton);
        visibility = (ImageButton) getActivity().findViewById(R.id.passwordVisibilityButton);
        emailEdit.setText("");
        passEdit.setText("");

        passwordIsVisible = false;

        encryptKey = "Game er s few123";
        iv = "Fewg am er s1234";

        passEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    passEdit.setBackgroundColor(getResources().getColor(R.color.background));
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordIsVisible) {
                    passwordIsVisible = false;
                    visibility.setImageResource(R.drawable.ic_login_visibility_off);
                    passEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    passwordIsVisible = true;
                    visibility.setImageResource(R.drawable.ic_login_visibility);
                    passEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    private void checkLogin() {
        email = emailEdit.getText().toString();
        pass = passEdit.getText().toString();

        String loginDataString, encryptedLoginDataString;
        JSONObject loginData = new JSONObject();
        JSONObject finalLogin = new JSONObject();

        try {
            loginData.put("email", email);
            loginData.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        loginDataString = loginData.toString();

        //encryptedLoginDataString = loginEncrypt(loginDataString);
        FGEncrypt encryptor = new FGEncrypt();
        encryptedLoginDataString = encryptor.encrypt(loginDataString);

        try {
            finalLogin.put("logindata", encryptedLoginDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("encrypto", finalLogin.toString());

        new LoginAsyncTask().execute("https://fewgamers.com/api/login/", finalLogin.toString(), "POST");
    }

    private String makeLoginJSONString(String name, String pass) {
        String res = "{\"email\":\"" + name + "\",\"password\":\"" + pass + "\"}";
        return res;
    }

    private void allowAccess() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    public String loginEncrypt(String loginData) {
        SecretKeySpec keySpec;
        IvParameterSpec ivSpec;
        Cipher cipher;

        String key = "Game er s few123";
        String iv = "Fewg am er s1234";

        String base = "";

        try {
            keySpec = new SecretKeySpec(key.getBytes(), "AES");
            ivSpec = new IvParameterSpec(iv.getBytes());
            cipher = Cipher.getInstance("AES/CBC/NoPadding");

            String test = loginData;
            char paddingChar = '0';
            int size = 16;
            int x = test.length() % size;
            int padLength = size - x;
            for (int i = 0; i < padLength; i++) {
                test += paddingChar;
            }

            byte[] res;
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            res = cipher.doFinal(test.getBytes());
            for (int i = 0; i < res.length; i++) {
                System.out.print(res[i] + ", ");
            }

            base = encodeToString(res, DEFAULT);
            System.out.println(base);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return base;
    }

    private class LoginAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);

                loginEditor.putString("uuid", jsonObject.getString("uuid"));
                loginEditor.putString("email", jsonObject.getString("email"));
                loginEditor.putString("username", jsonObject.getString("nickname"));
                loginEditor.putString("firstName", jsonObject.getString("firstname"));
                loginEditor.putString("lastName", jsonObject.getString("lastname"));
                loginEditor.putString("key", jsonObject.getString("key"));
                loginEditor.putString("activationCode", jsonObject.getString("activationcode"));

                loginEditor.putBoolean("isLoggedIn", true);

                loginEditor.apply();

                allowAccess();

            } catch (JSONException exception) {
                Log.e("Incorrect user data", "Retrieved user login data was incomplete");
            }

        }
    }
}
