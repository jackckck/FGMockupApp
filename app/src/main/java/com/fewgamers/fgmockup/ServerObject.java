package com.fewgamers.fgmockup;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

// instances of this class are used to represent users in an ArrayList<ServerObject>, which in turn
// is translate by an instance of ContactsListAdapter to display a ListView of contacts onscreen
public class ServerObject {
    // fields for all of a server's information
    private String gameUUID, serverName, serverUUID, playerCap, ip, userCreator, additionalInfo;
    private Integer maxPlayer;

    public void defineServer(JSONObject jsonObject) throws JSONException {
        this.gameUUID = jsonObject.getString("game");
        this.serverName = jsonObject.getString("name");
        this.serverUUID = jsonObject.getString("uuid");
        this.ip = jsonObject.getString("ip");
        this.userCreator = jsonObject.getString("creator");
        this.additionalInfo = jsonObject.getString("additionaldata");

        String cap = jsonObject.getString("playercount");
        if (cap.equals("None")) {
            this.playerCap = "-";
            maxPlayer = 0;
        } else {
            this.playerCap = cap;
            maxPlayer = Integer.parseInt(this.playerCap);
        }
    }

    String getGameUUID() {
        return gameUUID;
    }

    String getServerName() {
        return serverName;
    }

    String getPlayerCap() {
        return playerCap;
    }

    Integer getMaxPlayer() {
        return maxPlayer;
    }

    String getAdditionalInfo() {
        return additionalInfo;
    }

    String getIp() {
        return ip;
    }

    String getCreator() {
        return userCreator;
    }

    String getServerUUID() {
        return serverUUID;
    }
}
