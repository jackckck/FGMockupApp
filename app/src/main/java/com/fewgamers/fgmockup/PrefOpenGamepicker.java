package com.fewgamers.fgmockup;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by Administrator on 1/9/2018.
 */

public class PrefOpenGamepicker extends Preference {
    public PrefOpenGamepicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        FragmentTransaction ft = ((Activity) getContext()).getFragmentManager().beginTransaction();
        ft.replace(R.id.MyFrameLayout, new PrefFragGamePicker());
        ft.commit();
    }
}
