package com.fewgamers.fgmockup;

import android.app.Fragment;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Administrator on 12/30/2017.
 */

public class LoginFragLogin extends android.support.v4.app.Fragment {
    EditText userName, password;
    Button login;
    ImageButton visibility;
    String name, pass, userKey, activationCode, generalKey, iv;
    Boolean passwordIsVisible;
    CheckBox check;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

    LoginCrypt loginCrypt;

    RequestQueue loginRequestQueue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loginfraglogin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userName = (EditText) getActivity().findViewById(R.id.userName);
        password = (EditText) getActivity().findViewById(R.id.password);
        login = (Button) getActivity().findViewById(R.id.loginButton);
        visibility = (ImageButton) getActivity().findViewById(R.id.passwordVisibilityButton);
        check = (CheckBox) getActivity().findViewById(R.id.loginCheck);
        userName.setText("");
        password.setText("");

        passwordIsVisible = false;

        userKey = "Game er s few123";
        generalKey = "Fewg am er s1234";
        iv = "";

        loginSharedPreferences = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();

        loginCrypt = new LoginCrypt(generalKey, iv);

        loginRequestQueue = RequestSingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        if (loginSharedPreferences.getBoolean("stayLogged", false)) {
            allowAccess();
        }

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    password.setBackgroundColor(getResources().getColor(R.color.background));
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
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    passwordIsVisible = true;
                    visibility.setImageResource(R.drawable.ic_login_visibility);
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    private void checkLogin() {
        name = userName.getText().toString();
        pass = password.getText().toString();

        String loginJSONString, encryptedLoginJSONString;

        try {
            loginJSONString = makeLoginJSONString(name, pass);
            encryptedLoginJSONString = loginCrypt.byteArrayToHexString(loginCrypt.encrypt(loginJSONString));

        } catch (Exception exception) {
            Log.e("Encryption method", "Something went wrong when calling encryption method");
            return;
        }

        Log.d("encrypto", encryptedLoginJSONString);
        new LoginAsyncTask().execute("https://fewgamers.com/api/login/", "{\"logindata\":\"mKa8++Zb/CC9AHh/PBFMS2y6FHx9YqZVwJ1Sb3x52pC6fJQVKpO3q+W3oUk/jQZw\"}");
    }

    private String makeLoginJSONString(String name, String pass) {
        String res = "{\"username\":\"" + name + "\",\"password\":\"" + pass + "\"}";
        return res;
    }

    private void allowAccess() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private class LoginCrypt {
        private SecretKeySpec keySpec;
        private IvParameterSpec ivSpec;
        private Cipher cipher;

        public LoginCrypt(String key, String iv) {
            keySpec = new SecretKeySpec(key.getBytes(), "AES");
            ivSpec = new IvParameterSpec(iv.getBytes());

            try {
                cipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException exception) {
                Log.e("Algorithm not found", "No such algorithm could be found");
            } catch (NoSuchPaddingException exception) {
                Log.e("Padding not found", "No such padding could be found");
            }
        }

        public byte[] encrypt(String string) throws Exception {
            if (string == null || string.length() == 0) {
                throw new Exception("Empty string");
            }
            byte[] res = null;

            try {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
                res = cipher.doFinal(string.getBytes());
            } catch (Exception exception) {
                Log.e("Encryption error", "Something went wrong with the encryption");
            }

            return res;
        }

        public byte[] decrypt(String string) throws Exception {
            if (string == null || string.length() == 0) {
                throw new Exception("Empty string");
            }
            byte[] res = null;

            try {
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

                res = cipher.doFinal(string.getBytes());
            } catch (Exception exception) {
                Log.e("Decryption error", "Something went wrong with the decryption");
            }

            return res;
        }

        public String byteArrayToHexString(byte[] array) {
            StringBuilder hexString = new StringBuilder();
            for (byte b : array) {
                int intVal = b & 0xff;
                if (intVal < 0x10)
                    hexString.append("0");
                hexString.append(Integer.toHexString(intVal));
            }
            return hexString.toString();
        }
    }

    private class LoginAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);


                userKey = jsonObject.getString("key");
                activationCode = jsonObject.getString("activationcode");

                loginEditor.putString("email", jsonObject.getString("email"));
                loginEditor.putString("nickname", jsonObject.getString("nickname"));
                loginEditor.putString("firstName", jsonObject.getString("firstname"));
                loginEditor.putString("lastName", jsonObject.getString("lastname"));
                loginEditor.putString("userKey", userKey);
                loginEditor.putString("activationCode", activationCode);

                Log.d("key", userKey);
                Log.d("activationCode", activationCode);

                allowAccess();

            } catch (JSONException exception) {
                Log.e("Incorrect user data", "Retrieved user login data was incomplete");
            }

        }
    }
}
