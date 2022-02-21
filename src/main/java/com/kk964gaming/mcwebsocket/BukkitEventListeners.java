package com.kk964gaming.mcwebsocket;

import com.kk964gaming.mcwebsocket.events.PlayerStatusChangeEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class BukkitEventListeners implements Listener {

    public static final HashMap<String, Set<String>> registeredEvents = new HashMap<>();

    public BukkitEventListeners(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void emitEventSockets(Event e, Object ...args) {
        emitEventSockets(e.getEventName(), args);
    }

    private void emitEventSockets(String eventName, Object ...args) {
        Set<String> listening = getEventListeners(eventName);
        if (listening.isEmpty()) return;

        JSONArray jsonArray = convertToJSONArray(args);
        String jsonString = jsonArray.toJSONString();

        MCWebsocketIntegration.getInstance().getWebsocket().sendToIn(listening, "Event " + eventName +  " " + jsonString);
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
        emitEventSockets("PlayerChatEvent", e.getPlayer().getName(), e.getMessage());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        emitEventSockets("PlayerDamageEvent", p.getName(), e.getDamage(), p.getHealth(), e.getCause().toString());
    }

    @EventHandler
    public void onPlayerHeal(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        emitEventSockets("PlayerHealEvent", p.getName(), e.getAmount(), p.getHealth(), e.getRegainReason().toString());
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e) {
        emitEventSockets(e, e.getPlayer().getName(), e.getAdvancement().getKey().getKey());
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
        emitEventSockets(e, e.getPlayer().getName(), e.getPlayer().getWorld().getName(), e.getFrom().getName());
    }

    @EventHandler
    public void onPlayerFoodChange(FoodLevelChangeEvent e) {
        emitEventSockets("PlayerFoodChangeEvent", e.getEntity().getName(), e.getFoodLevel(), e.getEntity().getSaturation());
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        Player dmgr = (Player) e.getDamager();
        emitEventSockets("PlayerDamagePlayerEvent", p.getName(), dmgr.getName(), e.getDamage(), p.getHealth(), dmgr.getHealth());
    }

    @EventHandler
    public void onPlayerStatusChange(PlayerStatusChangeEvent e) {
        emitEventSockets(e, e.getPlayer().getName(), e.getStatus().convertToJSON(), e.getOldStatus().convertToJSON());
    }

}
