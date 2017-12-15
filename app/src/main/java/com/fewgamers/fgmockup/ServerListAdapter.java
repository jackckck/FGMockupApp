package com.fewgamers.fgmockup;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 12/14/2017.
 */

public class ServerListAdapter extends ArrayAdapter<ServerObject> {
    private final Activity context;
    private final ArrayList<ServerObject> serverList;

    public ServerListAdapter(Activity context, ArrayList<ServerObject> serverList) {
        super(context, R.layout.server_list_item, serverList);

        this.context = context;
        this.serverList = serverList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.server_list_item, parent, false);

        TextView serverName, playerCount, serverIP, gameIdentifier;
        serverName = (TextView) resView.findViewById(R.id.serverName);
        playerCount = (TextView) resView.findViewById(R.id.playerCount);
        serverIP = (TextView) resView.findViewById(R.id.serverIP);
        gameIdentifier = (TextView) resView.findViewById(R.id.gameIdentifier);

        serverName.setText(serverList.get(position).getServerName());
        playerCount.setText(serverList.get(position).getPlayerCount());
        serverIP.setText(serverList.get(position).getIp());
        gameIdentifier.setText(serverList.get(position).getGame());

        return resView;
    }

}
