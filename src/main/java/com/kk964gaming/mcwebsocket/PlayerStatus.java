package com.kk964gaming.mcwebsocket;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.Objects;

public class PlayerStatus {
    public boolean isBurning;
    public boolean isSwimming;
    public boolean isFlying;
    public boolean isGliding;
    public boolean isFreezing;

    public PlayerStatus(Player player) {
        this.isBurning = player.getFireTicks() > 0 || player.isVisualFire();
        this.isSwimming = player.isSwimming();
        this.isFlying = player.isFlying();
        this.isGliding = player.isGliding();
        this.isFreezing = player.isFrozen();
    }

    public JSONObject convertToJSON() {
        JSONObject object = new JSONObject();
        object.put("isBurning", isBurning);
        object.put("isSwimming", isSwimming);
        object.put("isFlying", isFlying);
        object.put("isGliding", isGliding);
        object.put("isFreezing", isFreezing);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Player) return this.equals(new PlayerStatus((Player) o));
        if (getClass() != o.getClass()) return false;
        PlayerStatus that = (PlayerStatus) o;
        return isBurning == that.isBurning &&
                isSwimming == that.isSwimming &&
                isFlying == that.isFlying &&
                isGliding == that.isGliding &&
                isFreezing == that.isFreezing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isBurning, isSwimming, isFlying, isGliding, isFreezing);
    }
}