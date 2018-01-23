package com.fewgamers.fgmockup;

import android.util.Log;

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

import static android.util.Base64.DEFAULT;
import static android.util.Base64.encodeToString;

/**
 * Created by Administrator on 1/10/2018.
 */

public class FGEncrypt {
    SecretKeySpec keySpec;
    IvParameterSpec ivSpec;
    Cipher cipher;

    public FGEncrypt() {
        String key = "Game er s few123";
        String iv = "Fewg am er s1234";

        try {
            keySpec = new SecretKeySpec(key.getBytes(), "AES");
            ivSpec = new IvParameterSpec(iv.getBytes());
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String string) {
        String hex = "";
        StringBuilder builder = new StringBuilder();
        builder.append(string);

        try {
            char paddingChar = Character.UNASSIGNED;
            int size = 16;
            int x = string.length() % size;
            int padLength = size - x;
            for (int i = 0; i < padLength; i++) {
                string += paddingChar;
                builder.append(paddingChar);
            }

            byte[] res;
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            res = cipher.doFinal(string.getBytes());
            for (int i = 0; i < res.length; i++) {
                System.out.print(res[i] + ", ");
            }

            hex = bytesToHex(res);
            System.out.println(hex);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return hex;
    }

    private String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int val = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[val >>> 4];
            hexChars[i * 2 + 1] = hexArray[val & 0x0F];
        }
        return new String(hexChars);
    }

    public String getFinalQuery(String encryptedUserdata, String master) {
        String res = null;
        try {
            JSONObject finalQuery = new JSONObject();
            finalQuery.put("key", master);
            finalQuery.put("userdata", encryptedUserdata);
            res = finalQuery.toString();
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return res;
    }
}
