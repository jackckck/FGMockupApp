package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// is nog under construction.
public class LoginActivity extends AppCompatActivity {

    EditText userName, password;
    Button login;
    String name, pass, userKey, generalKey;
    CheckBox check;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

    LoginCrypt loginCryptZero, loginCryptOne;

    RequestQueue loginRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.loginButton);
        check = (CheckBox) findViewById(R.id.loginCheck);
        userName.setText("");
        password.setText("");

        userKey = "72C22E124DB9499C9FAA71CB501D273E";
        generalKey = "0123456789abcdef";

        loginSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();

        loginCryptZero = new LoginCrypt(userKey);
        loginCryptOne = new LoginCrypt(generalKey);

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

        String encryptedPass, loginJSONString, encryptedLoginJSONString;

        try {
            encryptedPass = loginCryptZero.byteArrayToHexString(loginCryptZero.encrypt(pass));
            loginJSONString = makeLoginJSONString(name, encryptedPass, userKey);
            encryptedLoginJSONString = loginCryptOne.byteArrayToHexString(loginCryptOne.encrypt(loginJSONString));

        } catch (Exception exception) {
            Log.e("Encryption method", "Something went wrong when calling encryption method");
            return;
        }

        sendLoginRequest(encryptedLoginJSONString);
    }

    private void sendLoginRequest(final String string) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, " http://www.fewgamers.com/api/user/?uuid=ALL",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        //Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Property name", string);

                return params;
            }
        };
        loginRequestQueue.add(postRequest);
    }

    private String makeLoginJSONString(String name, String encryptedPass, String key) {
        String res = "{\"username\":\"" + name + "\",\"password\":\"" + encryptedPass
                + "\",\"key\":\"" + key + "\"}";
        return res;
    }

    private void allowAccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private class LoginCrypt {
        private SecretKeySpec keySpec;
        private Cipher cipher;

        public LoginCrypt(String key) {
            keySpec = new SecretKeySpec(key.getBytes(), "AES");

            try {
                cipher = Cipher.getInstance("AES/GCM/NoPadding");
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
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
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
                cipher.init(Cipher.DECRYPT_MODE, keySpec);

                res = cipher.doFinal(string.getBytes());
            } catch (Exception exception) {
                Log.e("Decryption error", "Something went wrong with the decryption");
            }

            return res;
        }

        public String byteArrayToHexString(byte[] array) {
            StringBuffer hexString = new StringBuffer();
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
