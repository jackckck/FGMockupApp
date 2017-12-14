package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import javax.crypto.Cipher;

// is nog under construction.
public class LoginActivity extends AppCompatActivity {

    EditText userName, password;
    Button login;
    String name, pass;
    CheckBox check;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

    Cipher loginCipher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.loginButton);
        check = (CheckBox) findViewById(R.id.loginCheck);
        userName.setText(""); password.setText("");

        loginSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();

        RequestQueue requestQueue = RequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();

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
        }

        else {
            password.setBackgroundColor(getResources().getColor(R.color.errorColor));
        }
    }

    private void allowAccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
