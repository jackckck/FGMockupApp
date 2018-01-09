package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

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

public class LoginFragLogin extends android.support.v4.app.Fragment {
    EditText emailEdit, passEdit;
    Button login;
    ImageButton visibility;
    String email, pass, activationCode, userKey, encryptKey, iv;
    Boolean passwordIsVisible;
    CheckBox check;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

    LoginCrypt loginCrypt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.loginfraglogin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailEdit = (EditText) getActivity().findViewById(R.id.loginEmail);
        passEdit = (EditText) getActivity().findViewById(R.id.loginPass);
        login = (Button) getActivity().findViewById(R.id.loginButton);
        visibility = (ImageButton) getActivity().findViewById(R.id.passwordVisibilityButton);
        check = (CheckBox) getActivity().findViewById(R.id.loginCheck);
        emailEdit.setText("");
        passEdit.setText("");

        passwordIsVisible = false;

        encryptKey = "Game er s few123";
        iv = "Fewg am er s1234";

        loginSharedPreferences = getActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();

        loginCrypt = new LoginCrypt(encryptKey, iv);

        if (loginSharedPreferences.getBoolean("stayLogged", false)) {
            allowAccess();
        }

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
        encryptedLoginDataString = login(loginDataString);

        try {
            finalLogin.put("logindata", encryptedLoginDataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("encrypto", finalLogin.toString());

        new LoginAsyncTask().execute("https://fewgamers.com/api/login/", finalLogin.toString());
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

    private class LoginCrypt {
        private SecretKeySpec keySpec;
        private IvParameterSpec ivSpec;
        private Cipher cipher;

        public LoginCrypt(String key, String iv) {
            try {
                keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES/CBC/PKCS5Padding");
                ivSpec = new IvParameterSpec(iv.getBytes());
            } catch (UnsupportedEncodingException exception) {
                Log.e("Invalid encoding method", "No such encoding method could be found");
            }

            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
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
                Log.e("Encryption error", exception.getMessage());
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

        public String bytesToHex(byte[] bytes) {
            final char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++) {
                int val = bytes[i] & 0xFF;
                hexChars[i * 2] = hexArray[val >>> 4];
                hexChars[i * 2 + 1] = hexArray[val & 0x0F];
            }
            return new String(hexChars);
        }
    }

    public String login(String loginData) {
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
