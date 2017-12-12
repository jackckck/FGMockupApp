package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragMyServers extends FragBase {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmyservers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        changeBackGroundColor(getRelativeLayout("myServers"), nightModeCheck());
        super.onViewCreated(view, savedInstanceState);
    }
}
