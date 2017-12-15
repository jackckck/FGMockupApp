package com.fewgamers.fgmockup;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class ServerObject {
    String game;
    String serverName;
    String playerCount;
    String ip;

    public void defineServer(JSONObject jsonObject) {
        try {
            this.game = jsonObject.getString("game");
            this.serverName = jsonObject.getString("serverName");
            this.playerCount = jsonObject.getString("playercount");
            this.ip = jsonObject.getString("ip");
        } catch (JSONException j) {
            Log.e("Corrupt server", "Server data incomplete");
        }
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

    public String getIp() {
        return ip;
    }
}
