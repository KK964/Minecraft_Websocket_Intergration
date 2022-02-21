package com.kk964gaming.mcwebsocket.versions;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public abstract class PlayerStatus {

    abstract public PlayerStatus getPlayerStatus(Player player);

    abstract public JSONObject convertToJSON();

    abstract public boolean equals(Object o);

    abstract public int hashCode();
}