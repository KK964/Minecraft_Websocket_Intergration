package com.kk964gaming.mcwebsocket.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class CustomEventHandler implements Runnable {
    public CustomEventHandler(JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1,2);
    }

    @Override
    public void run() {
        checkPlayerStatusUpdate();
    }

    private static final HashMap<Player, PlayerStatusChangeEvent.PlayerStatus> oldStatuses = new HashMap<>();

    private void checkPlayerStatusUpdate() {
        Bukkit.getOnlinePlayers().forEach(this::checkPlayerStatusUpdate);
    }

    private void checkPlayerStatusUpdate(Player player) {
        PlayerStatusChangeEvent.PlayerStatus oldStatus = oldStatuses.computeIfAbsent(player, (s) -> new PlayerStatusChangeEvent.PlayerStatus(player));
        PlayerStatusChangeEvent.PlayerStatus status = new PlayerStatusChangeEvent.PlayerStatus(player);
        if (oldStatus.equals(status)) return;
        oldStatuses.put(player, status);
        PlayerStatusChangeEvent e = new PlayerStatusChangeEvent(player, status, oldStatus);
        Bukkit.getServer().getPluginManager().callEvent(e);
    }
}
