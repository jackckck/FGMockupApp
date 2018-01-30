package com.fewgamers.fewgamers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 1/10/2018.
 */

// extension of DialogPreference, used in the settings' account subscreen to allow a user to change
// their userdata in the FewGamers database
public class PrefChangeUserInfo extends DialogPreference {
    // whichValue is the name of the value type that the user wants to change, used as a key in the
    // json object sent to the server. whichValueRead is also the name of the value type, but as it
    // appears to the user.
    private String whichValue, whichValueRead, oldValue, newValue;
    private EditText newValueEdit;
    private MainActivity mainActivity;

    // constructor that determines which value is to be changed, based on the title that the
    // DialogPreference has in the settings screen that it is used in
    public PrefChangeUserInfo(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainActivity = (MainActivity) context;
        setDialogLayoutResource(R.layout.pref_change_user_info);
        switch (getTitleRes()) {
            case R.string.pref_user_change_email_title:
                whichValue = "email";
                whichValueRead = "email";
                oldValue = mainActivity.getMyEmail();
                break;
            case R.string.pref_user_change_username_title:
                whichValue = "nickname";
                whichValueRead = "username";
                oldValue = mainActivity.getMyUsername();
                break;
            case R.string.pref_user_change_firstname_title:
                whichValue = "firstname";
                whichValueRead = "first name";
                oldValue = mainActivity.getMyFirstName();
                break;
            case R.string.pref_user_change_lastname_title:
                whichValue = "lastname";
                whichValueRead = "last name";
                oldValue = mainActivity.getMyLastName();
                break;
        }
        setDialogTitle("Change " + whichValueRead + " " + oldValue + "?");
    }

    // method called when the setting is selected
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // retrieve custom Dialog layout widget
        newValueEdit = (EditText) view.findViewById(R.id.pref_change_user_info_new_value_edit);

        // helps the user with what they have to type
        newValueEdit.setHint("New " + whichValueRead);
    }

    // when the confirm button is clicked, an AlertDialog is shown asking the user to confirm their
    // new user data value
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            newValue = newValueEdit.getText().toString();

            if (newValue.length() == 0) {
                return;
            }

            AlertDialog.Builder confirmation = new AlertDialog.Builder(getContext());
            confirmation.setMessage("Do you want " + newValue + " as your new " + whichValueRead + "?");
            confirmation.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    changeUserDataValue(mainActivity.getMyUuid(), whichValue, newValue);
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

    // changes the value of a particular field of userdata
    private void changeUserDataValue(String uuid, String whichValue, String newValue) {
        JSONObject userData = new JSONObject();
        String encryptedUserDataString;
        FGEncrypt fgEncrypt = new FGEncrypt();

        try {
            userData.put("uuid", uuid);
            userData.put(whichValue, newValue);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        encryptedUserDataString = fgEncrypt.encrypt(userData.toString());
        String finalQuery = fgEncrypt.getFinalQuery(encryptedUserDataString, mainActivity.getMaster());

        if (newValue != null && !newValue.equals("")) {
            new ChangeUserInfoAsyncTask().execute("https://fewgamers.com/api/user/", finalQuery, "PATCH");
        }
    }

    // extension of FGAsyncTask that informs the user of a succesful user data change, and updates
    // the relevant user data field that the app has stored in its instance of MainActivity
    private class ChangeUserInfoAsyncTask extends FGAsyncTask {
        @Override
        protected void onPostExecute(String[] response) {
            if (response[0].equals("200")) {
                switch (whichValue) {
                    case "email":
                        mainActivity.setMyEmail(newValue);
                        break;
                    case "username":
                        mainActivity.setMyUsername(newValue);
                        break;
                    case "firstname":
                        mainActivity.setMyFirstName(newValue);
                        break;
                    case "lastname":
                        mainActivity.setMyLastName(newValue);
                        break;
                }
            } else {
                Toast.makeText(mainActivity, "Failed to change user data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
