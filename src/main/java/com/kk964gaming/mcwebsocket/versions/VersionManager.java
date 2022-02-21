package com.kk964gaming.mcwebsocket.versions;

import com.kk964gaming.mcwebsocket.versions.events.BukkitEvents_R1_13;
import com.kk964gaming.mcwebsocket.versions.events.BukkitEvents_R1_16;
import com.kk964gaming.mcwebsocket.versions.events.BukkitEvents_Shared;
import com.kk964gaming.mcwebsocket.versions.playerstatus.PlayerStatus_R1_13;
import com.kk964gaming.mcwebsocket.versions.playerstatus.PlayerStatus_R1_18;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Set;

public class VersionManager {
    public static final HashMap<String, Set<String>> registeredEvents = new HashMap<>();
    public static PlayerStatus playerStatus;

    public VersionManager(JavaPlugin plugin) {
        if (plugin.getServer().getVersion().contains("1.18")) {
            playerStatus = new PlayerStatus_R1_18();
            new BukkitEvents_R1_16(plugin);
        } else if (plugin.getServer().getVersion().contains("1.17")) {
            playerStatus = new PlayerStatus_R1_13();
            new BukkitEvents_R1_16(plugin);
        } else if (plugin.getServer().getVersion().contains("1.16")) {
            playerStatus = new PlayerStatus_R1_13();
            new BukkitEvents_R1_16(plugin);
        } else if (plugin.getServer().getVersion().contains("1.15")) {
            playerStatus = new PlayerStatus_R1_13();
            new BukkitEvents_R1_13(plugin);
        } else if (plugin.getServer().getVersion().contains("1.14")) {
            playerStatus = new PlayerStatus_R1_13();
            new BukkitEvents_R1_13(plugin);
        } else if (plugin.getServer().getVersion().contains("1.13")) {
            playerStatus = new PlayerStatus_R1_13();
            new BukkitEvents_R1_13(plugin);
        } else {
            Bukkit.getLogger().severe("MC WS Integration does not support that version! Please contact KK964 if you think this is an error!");
            Bukkit.getServer().shutdown();
        }

        new BukkitEvents_Shared(plugin);
    }

    public static PlayerStatus playerStatus(Player player) {
        return playerStatus.getPlayerStatus(player);
    }
}
