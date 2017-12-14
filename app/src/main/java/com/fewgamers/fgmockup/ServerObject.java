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
            this.serverName = jsonObject.getString("server");
            this.playerCount = jsonObject.getString("player");
            this.ip = jsonObject.getString("ip");
        }
        catch (JSONException j) {
            Log.e("Corrupt server", "Server data incomplete");
        }
    }
}
