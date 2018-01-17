package com.fewgamers.fgmockup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 1/10/2018.
 */

public class PrefChangeUserInfo extends DialogPreference {
    private String whichValue, whichValueRead, oldValue, newValue;
    private EditText newValueEdit;
    private MainActivity mainActivity;

    public PrefChangeUserInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainActivity = (MainActivity) context;
        setDialogLayoutResource(R.layout.pref_change_user_info);
        switch (getTitleRes()) {
            case R.string.pref_user_change_email_title:
                whichValue = "email";
                whichValueRead = "email";
                oldValue = mainActivity.email;
                break;
            case R.string.pref_user_change_username_title:
                whichValue = "nickname";
                whichValueRead = "username";
                oldValue = mainActivity.username;
                break;
            case R.string.pref_user_change_firstname_title:
                whichValue = "firstname";
                whichValueRead = "first name";
                oldValue = mainActivity.firstName;
                break;
            case R.string.pref_user_change_lastname_title:
                whichValue = "lastname";
                whichValueRead = "last name";
                oldValue = mainActivity.lastName;
                break;
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TextView oldValueText = (TextView) view.findViewById(R.id.pref_change_user_info_old_value_text);
        newValueEdit = (EditText) view.findViewById(R.id.pref_change_user_info_new_value_edit);

        oldValueText.setText("Old " + whichValueRead + ": " + oldValue);
        newValueEdit.setHint("New " + whichValueRead);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            newValue = newValueEdit.getText().toString();
            Log.d("new value", newValue);

            if (newValue.length() == 0) {
                return;
            }

            AlertDialog.Builder confirmation = new AlertDialog.Builder(getContext());
            confirmation.setMessage("Do you want " + newValue + " as your new " + whichValueRead + "?");
            confirmation.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    changeUserValue(newValue);
                }
            });
            confirmation.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            confirmation.show();
        }
    }

    private void changeUserValue(String newValue) {
        JSONObject userData = new JSONObject();
        JSONObject finalChangeQuery = new JSONObject();
        String encryptedUserDataString;
        FGEncrypt fgEncrypt = new FGEncrypt();

        try {
            userData.put("uuid", mainActivity.uuid);
            userData.put(whichValue, newValue);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        Log.d("userdata", userData.toString());

        encryptedUserDataString = fgEncrypt.encrypt(userData.toString());

        try {
            finalChangeQuery.put("key", mainActivity.master);
            finalChangeQuery.put("userdata", encryptedUserDataString);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        Log.d("encrypted userdata", encryptedUserDataString);
        Log.d("let op!", finalChangeQuery.toString());
        if (newValue != null && !newValue.equals("")) {
            new ChangeUserInfoAsyncTask().execute("https://fewgamers.com/api/user/", finalChangeQuery.toString(), "PATCH");
        }
    }

    private class ChangeUserInfoAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String response) {
            switch (whichValue) {
                case "email":
                    mainActivity.email = newValue;
                    break;
                case "username":
                    mainActivity.username = newValue;
                    break;
                case "firstname":
                    mainActivity.firstName = newValue;
                    break;
                case "lastname":
                    mainActivity.lastName = newValue;
                    break;
            }
        }
    }
}
