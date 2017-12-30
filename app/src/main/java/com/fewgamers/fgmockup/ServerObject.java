package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class ServerObject {
    private String game, serverName, playerCount, ip, userCreator;
    private Integer livePlayer, maxPlayer;

    public void defineServer(JSONObject jsonObject) {
        try {
            this.game = jsonObject.getString("game");
            this.serverName = jsonObject.getString("name");
            //this.playerCount = jsonObject.getString("playercount");
            this.playerCount = "0/0";
            this.ip = jsonObject.getString("ip");
            this.userCreator = jsonObject.getString("creator");
        } catch (JSONException j) {
            Log.e("Corrupt server", "Server data incomplete");
        }
        String[] playerCountStrings = playerCount.split("/");
        livePlayer = Integer.parseInt(playerCountStrings[0]);
        maxPlayer = Integer.parseInt(playerCountStrings[1]);
    }

    public String getGame() {
        return game;
    }

    public String getServerName() {
        return serverName;
    }

    public String getPlayerCount() {
        return playerCount;
    }

    public Integer getLivePlayer() {
        return livePlayer;
    }

    public Integer getMaxPlayer() {
        return maxPlayer;
    }

    public String getIp() {
        return ip;
    }

    public String getCreator() {
        return userCreator;
    }
}
