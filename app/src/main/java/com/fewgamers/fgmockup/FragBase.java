package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Administrator on 12/8/2017.
 */

// een overkoepelende klasse voor alle fragments, wordt zelf niet geïmplementeerd

public class FragBase extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraghome, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // checkt of nightmode aan staat
    public boolean nightModeCheck() {

        SharedPreferences sp = getActivity().getSharedPreferences("NightModeActive", Context.MODE_PRIVATE);

        boolean nightModeOn = sp.getBoolean("HAIL_SITHIS", false);

        return nightModeOn;
    }

    // activeert night mode
    public void changeBackGroundColor(RelativeLayout rl, boolean night) {
        if (night) {
            rl.setBackgroundColor(getResources().getColor(R.color.nightBackground));
        } else {
            rl.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    public JSONArray getJSONarrayFromString(String s) {
        try {
            JSONArray res = new JSONArray(s);
            return res;
        } catch (JSONException exception) {
            throw new RuntimeException();
        }
    }
}
