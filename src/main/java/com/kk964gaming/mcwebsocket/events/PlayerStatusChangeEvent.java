package com.kk964gaming.mcwebsocket.events;

import com.kk964gaming.mcwebsocket.versions.PlayerStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
}
