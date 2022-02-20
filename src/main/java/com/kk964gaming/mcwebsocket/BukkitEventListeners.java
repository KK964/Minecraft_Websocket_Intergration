package com.kk964gaming.mcwebsocket;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BukkitEventListeners implements Listener {

    public static final HashMap<String, Set<String>> registeredEvents = new HashMap<>();

    public BukkitEventListeners(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void emitEventSockets(Event e, Object ...args) {
        String eventString = e.getEventName();
        Set<String> listening = getEventListeners(eventString);
        if (listening.isEmpty()) return;

        JSONArray jsonArray = convertToJSONArray(args);
        String jsonString = jsonArray.toJSONString();

        MCWebsocketIntegration.getInstance().getWebsocket().sendToIn(listening, "Event " + eventString +  " " + jsonString);
    }

    public static Set<String> getEventListeners(String e) {
        return registeredEvents.computeIfAbsent(e, (s) -> new HashSet<>());
    }

    private JSONArray convertToJSONArray(Object ...args) {
        JSONArray arr = new JSONArray();
        arr.addAll(Arrays.asList(args));
        return arr;
    }

    private JSONObject convertLocationToJSONObject(Location location) {
        JSONObject object = new JSONObject();
        object.put("world", location.getWorld().getName());
        object.put("x", location.getX());
        object.put("y", location.getY());
        object.put("z", location.getZ());
        return object;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        emitEventSockets(e, e.getEntity().getName(), e.getDeathMessage(), convertLocationToJSONObject(e.getEntity().getLocation()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        emitEventSockets(e, e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        emitEventSockets(e, e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        emitEventSockets(e, e.getPlayer().getName(), e.getMessage());
    }
}
