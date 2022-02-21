package com.kk964gaming.mcwebsocket.versions.playerstatus;

import com.kk964gaming.mcwebsocket.versions.PlayerStatus;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.Objects;

public class PlayerStatus_R1_13 extends PlayerStatus {
    public boolean isBurning;
    public boolean isSwimming;
    public boolean isFlying;
    public boolean isGliding;

    public PlayerStatus_R1_13(){}

    public PlayerStatus_R1_13(Player player) {
        this.isBurning = player.getFireTicks() > 0;
        this.isFlying = player.isFlying();
        this.isGliding = player.isGliding();
        this.isSwimming = player.isSwimming();
    }

    @Override
    public PlayerStatus getPlayerStatus(Player player) {
        return new PlayerStatus_R1_13(player);
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject object = new JSONObject();
        object.put("isBurning", isBurning);
        object.put("isFlying", isFlying);
        object.put("isGliding", isGliding);
        object.put("isSwimming", isSwimming);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Player) return this.equals(new PlayerStatus_R1_13((Player) o));
        if (getClass() != o.getClass()) return false;
        PlayerStatus_R1_13 that = (PlayerStatus_R1_13) o;
        return isBurning == that.isBurning &&
                isFlying == that.isFlying &&
                isGliding == that.isGliding;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isBurning, isFlying, isGliding);
    }
}
