package com.fewgamers.fewgamers;

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

// this class translates an ArrayList<ServerObject> to a visual display of a servers list
public class ServerListAdapter extends ArrayAdapter<ServerObject> {
    private final MainActivity mainActivityContext;
    private final ArrayList<ServerObject> serverList;

    // constructor that passes on the Activity's context, and the list from which a ListView is
    // populated
    ServerListAdapter(Activity context, ArrayList<ServerObject> serverList) {
        super(context, R.layout.server_list_item, serverList);

        this.mainActivityContext = (MainActivity) context;
        this.serverList = serverList;
    }

    // method in which the fields of a ServerObject are added to a ListView item
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mainActivityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.server_list_item, parent, false);

        TextView serverNameText, playerCapText, serverIPText, gameIdentifier;
        serverNameText = (TextView) resView.findViewById(R.id.serverName);
        playerCapText = (TextView) resView.findViewById(R.id.playerCap);
        serverIPText = (TextView) resView.findViewById(R.id.serverIP);
        gameIdentifier = (TextView) resView.findViewById(R.id.gameIdentifier);

        String name = limitString(serverList.get(position).getServerName(), 18);
        serverNameText.setText(name);

        String playerCap = serverList.get(position).getPlayerCap();
        playerCapText.setText(playerCap);

        serverIPText.setText(serverList.get(position).getIp());

        String gameUUID = serverList.get(position).getGameUUID();
        String game = mainActivityContext.getGameNameFromUUID(gameUUID);
        gameIdentifier.setText(game);

        return resView;
    }

    // limits overly long server names to fit the user's screen
    private String limitString(String string, int limit) {
        String res = string;
        if (res.length() > limit) {
            res = res.substring(0, limit + 1) + "...";
        }

        return res;
    }
}
