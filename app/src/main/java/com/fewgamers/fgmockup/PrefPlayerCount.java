package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 1/7/2018.
 */

public class PrefPlayerCount extends DialogPreference {
    private String minPlayerString, maxPlayerString;
    private EditText minPlayer, maxPlayer;

    public PrefPlayerCount(Context context, AttributeSet attrs) {
        super(context, attrs);

        minPlayerString = "null";
        maxPlayerString = "null";

        setDialogLayoutResource(R.layout.pref_playercount);
        setPositiveButtonText("Apply");
        setNegativeButtonText("Cancel");

        setDialogIcon(null);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String[] array;
        if (restorePersistedValue) {
            array = getPersistedString("null-null").split("-");
        } else {
            array = defaultValue.toString().split("-");
            Log.d("defaulto", defaultValue.toString());
        }
        minPlayerString = array[0];
        maxPlayerString = array[1];
    }

    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onBindDialogView(View view) {
        minPlayer = view.findViewById(R.id.pref_playercount_min);
        maxPlayer = view.findViewById(R.id.pref_playercount_max);

        if (!minPlayerString.equals("null")) {
            Log.d("min", "is niet null");
            minPlayer.setText(minPlayerString);
        }
        if (!maxPlayerString.equals("null")) {
            Log.d("max", "is niet null");
            maxPlayer.setText(maxPlayerString);
        }

        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Log.d("positive", "value is positive!");

            minPlayerString = minPlayer.getText().toString();
            maxPlayerString = maxPlayer.getText().toString();
            if (minPlayerString == null || minPlayerString.equals("")) {
                minPlayerString = "null";
            }
            if (maxPlayerString == null || maxPlayerString.equals("")) {
                maxPlayerString = "null";
            }
            persistString(minPlayerString + "-" + maxPlayerString);
            Log.d("persistString", minPlayerString + "-" + maxPlayerString);
        }

        super.onDialogClosed(positiveResult);
    }
}