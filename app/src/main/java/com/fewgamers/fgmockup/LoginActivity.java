package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

// is nog under construction.
public class LoginActivity extends AppCompatActivity {

    EditText userName, password;
    Button login;
    String name, pass;
    CheckBox check;

    SharedPreferences loginSharedPreferences;
    SharedPreferences.Editor loginEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.loginButton);
        check = (CheckBox) findViewById(R.id.loginCheck);

        loginSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        loginEditor = loginSharedPreferences.edit();

        if (loginSharedPreferences.getBoolean("stayLogged", false)) {
            allowAccess();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });


        GCMParameterSpec spec = new GCMParameterSpec(128, new byte[] {
                (byte)0x46,(byte)0x94,(byte)0x38,(byte)0xdd,(byte)0x08,(byte)0xfc,(byte)0x8c,(byte)0x09,(byte)0x6f,(byte)0xf1,(byte)0x2f,(byte)0xfb,(byte)0xa1,(byte)0x4b,(byte)0x91,(byte)0xd3,
                (byte)0xfd,(byte)0x43,(byte)0x67,(byte)0xe5,(byte)0xc3,(byte)0x20,(byte)0x64,(byte)0xa2,(byte)0x5a,(byte)0xe9,(byte)0x73,(byte)0x71,(byte)0x96,(byte)0xc8,(byte)0x2c,(byte)0x08,
                (byte)0x3d,(byte)0x6d,(byte)0x4a,(byte)0x43,(byte)0x1c,(byte)0x6e,(byte)0xb5,(byte)0xf3,(byte)0xeb,(byte)0x0e,(byte)0x14,(byte)0x4a,(byte)0x93,(byte)0x55,(byte)0x82,(byte)0x8a,
                (byte)0xf4,(byte)0x00,(byte)0xfb,(byte)0xac,(byte)0xb0,(byte)0xa5,(byte)0x2f,(byte)0x0c,(byte)0xd5,(byte)0xc8,(byte)0xf2,(byte)0x43,(byte)0xa8,(byte)0xb5,(byte)0xe3,(byte)0xca,
                (byte)0x0a,(byte)0xd8,(byte)0x62,(byte)0xd3,(byte)0xef,(byte)0x03,(byte)0xfd,(byte)0xb5,(byte)0xde,(byte)0x02,(byte)0x0e,(byte)0x15,(byte)0x8c,(byte)0x7b,(byte)0x39,(byte)0x99,
                (byte)0x95,(byte)0xaa,(byte)0x6e,(byte)0xf0,(byte)0x89,(byte)0x20,(byte)0xac,(byte)0x4b,(byte)0xba,(byte)0x71,(byte)0x18,(byte)0x1e,(byte)0xe8,(byte)0x2c,(byte)0x64,(byte)0xa0,
                (byte)0xea,(byte)0x72,(byte)0x6e,(byte)0xa9,(byte)0x20,(byte)0x67,(byte)0x91,(byte)0x25,(byte)0xaa,(byte)0x70,(byte)0x7a,(byte)0x6c,(byte)0xbb,(byte)0x32,(byte)0x35,(byte)0x6b,
                (byte)0x6a,(byte)0x6a,(byte)0x56,(byte)0x45,(byte)0xdd,(byte)0x9a,(byte)0x5c,(byte)0x4a,(byte)0x88,(byte)0x2c,(byte)0x7d,(byte)0xf1,(byte)0x83,(byte)0x9e,(byte)0x08,(byte)0x3e});
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NOPADDING");
            cipher.init(Cipher.ENCRYPT_MODE, (Key) spec);
            ByteBuffer test;
            cipher.updateAAD(test);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

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
    }

    private void allowAccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
