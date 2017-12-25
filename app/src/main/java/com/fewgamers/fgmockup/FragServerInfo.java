package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Administrator on 12/19/2017.
 */

public class FragServerInfo extends Fragment {

    String thisGame, thisServerName, thisServerIP, thisServerCreator;
    TextView thisGameDisplay, thisServerNameDisplay, thisServerIPDisplay, thisServerCreatorDisplay;

    public void setServer(ServerObject thisServer) {
        this.thisGame = thisServer.getGame();
        this.thisServerName = thisServer.getServerName();
        this.thisServerIP = thisServer.getIp();
        this.thisServerCreator = thisServer.getCreator();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragserverinfo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        thisGameDisplay = (TextView) getActivity().findViewById(R.id.serverInfoGameDisplay);
        thisServerNameDisplay = (TextView) getActivity().findViewById(R.id.serverInfoServerNameDisplay);
        thisServerIPDisplay = (TextView) getActivity().findViewById(R.id.serverInfoIPAddressDisplay);
        thisServerCreatorDisplay = (TextView) getActivity().findViewById(R.id.serverInfoCreatorUserDisplay);

        setServerDisplayTexts();
    }

    private void setServerDisplayTexts() {
        thisGameDisplay.setText(thisGame);
        thisServerNameDisplay.setText(thisServerName);
        thisServerIPDisplay.setText(thisServerIP);
        thisServerCreatorDisplay.setText(thisServerCreator);
    }
}
