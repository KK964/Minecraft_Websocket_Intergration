package com.kk964gaming.mcwebsocket.versions;

import com.kk964gaming.mcwebsocket.MCWebsocketIntegration;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class BukkitEvents implements Listener {
    public BukkitEvents(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void emitEventSockets(Event e, Object ...args) {
        emitEventSockets(e.getEventName(), args);
    }

    public void emitEventSockets(String eventName, Object ...args) {
        Set<String> listening = getEventListeners(eventName);
        if (listening.isEmpty()) return;

        JSONArray jsonArray = convertToJSONArray(args);
        String jsonString = jsonArray.toJSONString();

        MCWebsocketIntegration.getInstance().getWebsocket().sendToIn(listening, "Event " + eventName +  " " + jsonString);
    }

    public static Set<String> getEventListeners(String e) {
        return VersionManager.registeredEvents.computeIfAbsent(e, (s) -> new HashSet<>());
    }

    public JSONArray convertToJSONArray(Object ...args) {
        JSONArray arr = new JSONArray();
        arr.addAll(Arrays.asList(args));
        return arr;
    }

    public JSONObject convertLocationToJSONObject(Location location) {
        JSONObject object = new JSONObject();
        object.put("world", location.getWorld().getName());
        object.put("x", location.getX());
        object.put("y", location.getY());
        object.put("z", location.getZ());
        return object;
    }
}
