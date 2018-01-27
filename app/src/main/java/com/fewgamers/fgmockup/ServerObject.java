package com.fewgamers.fgmockup;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 12/14/2017.
 */

public class ServerObject {
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

    public String getGameUUID() {
        return gameUUID;
    }

    public String getServerName() {
        return serverName;
    }

    public String getPlayerCap() {
        return playerCap;
    }

    public Integer getMaxPlayer() {
        return maxPlayer;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getIp() {
        return ip;
    }

    public String getCreator() {
        return userCreator;
    }

    public String getServerUUID() {
        return serverUUID;
    }
}
