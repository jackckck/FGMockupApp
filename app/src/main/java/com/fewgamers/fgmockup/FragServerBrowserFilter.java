package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * Created by Administrator on 12/26/2017.
 */

public class FragServerBrowserFilter extends Fragment {
    EditText playerMinDisplay, playerMaxDisplay, maxPlayerMinDisplay, maxPlayerMaxDisplay;
    Button applyButton;

    MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragserverbrowserfilter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();

        playerMinDisplay = (EditText) getActivity().findViewById(R.id.playerCountMinimum);
        playerMaxDisplay = (EditText) getActivity().findViewById(R.id.playerCountMaximum);
        maxPlayerMinDisplay = (EditText) getActivity().findViewById(R.id.maxPlayerCountMinimum);
        maxPlayerMaxDisplay = (EditText) getActivity().findViewById(R.id.maxPlayerCountMaximum);

        setFilterDisplays();

        applyButton = (Button) getActivity().findViewById(R.id.applyButton);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter();

                FragServerBrowser fragment = new FragServerBrowser();

                if (fragment != null) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.MyFrameLayout, fragment);
                    ft.commit();
                }
            }
        });
    }

    private void setFilterDisplays() {
        Integer[] playerLimit = mainActivity.playerCountLimit;

        if (playerLimit[0] != null) {
            playerMinDisplay.setText(playerLimit[0].toString());
        }
        if (playerLimit[1] != null) {
            playerMaxDisplay.setText(playerLimit[1].toString());
        }
        if (playerLimit[2] != null) {
            maxPlayerMinDisplay.setText(playerLimit[2].toString());
        }
        if (playerLimit[3] != null) {
            maxPlayerMaxDisplay.setText(playerLimit[3].toString());
        }
    }

    private void applyFilter() {
        MainActivity mainActivity = (MainActivity) getActivity();
        Integer[] playerCountLimit = new Integer[4];

        try {
            playerCountLimit[0] = Integer.parseInt(playerMinDisplay.getText().toString());
        } catch (NumberFormatException exception) {
            playerCountLimit[0] = 0;
        }
        try {
            playerCountLimit[1] = Integer.parseInt(playerMaxDisplay.getText().toString());
        } catch (NumberFormatException exception) {
            playerCountLimit[1] = 999;
        }
        try {
            playerCountLimit[2] = Integer.parseInt(maxPlayerMinDisplay.getText().toString());
        } catch (NumberFormatException exception) {
            playerCountLimit[2] = 0;
        }
        try {
            playerCountLimit[3] = Integer.parseInt(maxPlayerMaxDisplay.getText().toString());
        } catch (NumberFormatException exception) {
            playerCountLimit[3] = 999;
        }

        mainActivity.playerCountLimit = playerCountLimit;
    }

    private Integer myIntParseMethod(String s) {
        if (s == "" || s == null) {
            return null;
        } else {
            return Integer.parseInt(s);
        }
    }
}
