package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// is nog under construction.
public class LoginActivity extends AppCompatActivity {

    EditText userName, password;
    Button login;
    ImageButton visibility;
    String name, pass, userKey, generalKey, iv;
    Boolean passwordIsVisible;
    CheckBox check;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

    LoginCrypt loginCrypt;

    RequestQueue loginRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.loginButton);
        visibility = (ImageButton) findViewById(R.id.passwordVisibilityButton);
        check = (CheckBox) findViewById(R.id.loginCheck);
        userName.setText("");
        password.setText("");

        passwordIsVisible = false;

        userKey = "Game er s few123";
        generalKey = "Fewg am er s1234";
        iv = "";

        loginSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();

        loginCrypt = new LoginCrypt(generalKey, iv);

        loginRequestQueue = RequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();

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

        if ((name.equals("jack") || name.equals("luuk")) && pass.equals("FewGamers")) {
            if (check.isChecked()) {
                loginEditor.putBoolean("stayLogged", true);
                loginEditor.commit();
            }
            allowAccess();
        } else {
            password.setBackgroundColor(getResources().getColor(R.color.errorColor));
        }

        String loginJSONString, encryptedLoginJSONString;

        try {
            loginJSONString = makeLoginJSONString(name, pass);
            encryptedLoginJSONString = loginCrypt.byteArrayToHexString(loginCrypt.encrypt(loginJSONString));

        } catch (Exception exception) {
            Log.e("Encryption method", "Something went wrong when calling encryption method");
            return;
        }

        Log.d("encrypto", encryptedLoginJSONString);
        new LoginAsyncTask().execute("http://fewgamers.com/api/login/", encryptedLoginJSONString);
    }

    private void makeLoginRequest(final String string) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://www.fewgamers.com/api/login/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("login error", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("User data", string);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        loginRequestQueue.add(stringRequest);
    }

    private String makeLoginJSONString(String name, String pass) {
        String res = "{\"username\":\"" + name + "\",\"password\":\"" + pass + "\"}";
        return res;
    }

    private void allowAccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
}