package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragBlocked extends ListFragBase {

    ArrayList<String> blockedList;

    BlockedAdapter blockedAdapter;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragblocked, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        blockedList = new ArrayList<>();
        blockedList.add("steve jobs");
        blockedList.add("peter pan");
        blockedList.add("peter ad infinitum");

        blockedAdapter = new BlockedAdapter(getActivity(), blockedList);
        setListAdapter(blockedAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(getActivity(), blockedList.get(position), Toast.LENGTH_SHORT).show();
    }
}
