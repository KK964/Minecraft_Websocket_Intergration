package com.kk964gaming.mcwebsocket.events;

import com.kk964gaming.mcwebsocket.BukkitEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.json.JSONObject;

import java.util.Objects;

public class PlayerStatusChangeEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final PlayerStatus status;
    private final PlayerStatus oldStatus;

    public PlayerStatusChangeEvent(Player player, PlayerStatus status, PlayerStatus oldStatus) {
        this.player = player;
        this.status = status;
        this.oldStatus = oldStatus;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public PlayerStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static class PlayerStatus {

        public final JSONObject status;

        public PlayerStatus(Player player) {
            JSONObject status = new JSONObject();
            if ("1.18".equals(BukkitEvents.getVersion())) {
                status.put("isFreezing", player.isFrozen());
            }
            status.put("isBurning", player.getFireTicks() > 0);
            status.put("isFlying", player.isFlying());
            status.put("isGliding", player.isGliding());
            status.put("isSwimming", player.isSwimming());
            this.status = status;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (o instanceof Player) return this.equals(new PlayerStatus((Player) o));
            if (getClass() != o.getClass()) return false;
            PlayerStatus that = (PlayerStatus) o;
            boolean equal = true;
            if (status.has("isFreezing")) {
                equal = status.get("isFreezing").equals(that.status.get("isFreezing"));
            }
            if (equal) equal = status.get("isBurning").equals(that.status.get("isBurning"));
            if (equal) equal = status.get("isFlying").equals(that.status.get("isFlying"));
            if (equal) equal = status.get("isGliding").equals(that.status.get("isGliding"));
            if (equal) equal = status.get("isSwimming").equals(that.status.get("isSwimming"));
            return equal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(status);
        }
    }
}