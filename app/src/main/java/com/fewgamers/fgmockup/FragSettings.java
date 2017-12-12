package com.fewgamers.fgmockup;

import android.app.Application;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

/**
 * Created by Administrator on 12/6/2017.
 */

// de settings fragment. hier zit op het moment alleen nog night mode als werkende setting in
public class FragSettings extends FragBase {

    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragsettings, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        final RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.backgroundSettings);

        final boolean nightModeCheck = nightModeCheck();
        changeBackGroundColor(getRelativeLayout("settings"), nightModeCheck);

        super.onViewCreated(view, savedInstanceState);

        // zorgt dat de switch op zijn plaats blijft bij het switchen van fragments.
        Switch switch0 = (Switch) view.findViewById(R.id.switch0);
        if (nightModeCheck) { switch0.setChecked(true); }

        // noteert nightmode-stand in sharedpreferences.
        switch0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                sharedPreferences = getActivity().getSharedPreferences("NightModeActive", Context.MODE_PRIVATE);
                SharedPreferences.Editor speditor = sharedPreferences.edit();

                speditor.putBoolean("HAIL_SITHIS", isChecked);
                speditor.apply();

                changeBackGroundColor(relativeLayout, isChecked);
            }
        });
    }

    // een rare methode die waarschijnlijk niet meer gebruikt gaat worden. maar ik heb het nog even laten staan.
    private void changeBackGroundColor1(View view, boolean night){
        final RelativeLayout blockedBG, favouritesBG, friendsBG, homeBG, myServersBG, profileBG,
                serverBrowserBG, settingsBG;
        final int newBackGround;

        blockedBG = (RelativeLayout) view.findViewById(R.id.backgroundSettings);
        favouritesBG= (RelativeLayout) view.findViewById(R.id.backgroundFavourites);
        friendsBG = (RelativeLayout) view.findViewById(R.id.backgroundFriends);
        homeBG = (RelativeLayout) view.findViewById(R.id.backgroundHome);
        myServersBG = (RelativeLayout) view.findViewById(R.id.backgroundMyServers);
        profileBG = (RelativeLayout) view.findViewById(R.id.backgroundProfile);
        serverBrowserBG = (RelativeLayout) view.findViewById(R.id.backgroundServerBrowser);
        settingsBG = (RelativeLayout) view.findViewById(R.id.backgroundSettings);

        final RelativeLayout[] backgroundArray = {blockedBG, favouritesBG, friendsBG, homeBG, myServersBG
                , profileBG, serverBrowserBG, settingsBG};

        if (night){
            newBackGround = getResources().getColor(R.color.nightBackground);
            for (RelativeLayout rl: backgroundArray) {
                rl.setBackgroundColor(newBackGround);
            }
        }
        else {
            newBackGround = getResources().getColor(R.color.background);
            for (RelativeLayout rl: backgroundArray){
                //rl.setBackgroundColor(newBackGround);
            }
        }
    }
}
