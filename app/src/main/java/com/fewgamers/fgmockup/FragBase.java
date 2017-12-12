package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 12/8/2017.
 */

// een overkoepelende klasse voor alle fragments, wordt zelf niet geïmplementeerd

public class FragBase extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragblocked, container, false);
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

    // om de relativelayout van alle achtergronden te vinden. een beetje clunky. waarschijnlijk
    // wordt night mode anders geïmplementeerd later
    public RelativeLayout getRelativeLayout(String s) {
        RelativeLayout rl = null;

        switch (s) {
            case "blocked":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundBlocked);
                break;
            case "favourites":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundFavourites);
                break;
            case "friends":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundFriends);
                break;
            case "home":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundHome);
                break;
            case "myServers":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundMyServers);
                break;
            case "profile":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundProfile);
                break;
            case "serverBrowser":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundServerBrowser);
                break;
            case "settings":
                rl = (RelativeLayout) getActivity().findViewById(R.id.backgroundSettings);
                break;
        }

        return rl;
    }
}
