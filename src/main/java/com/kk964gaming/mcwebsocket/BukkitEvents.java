package com.kk964gaming.mcwebsocket;

import com.kk964gaming.mcwebsocket.events.PlayerStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BukkitEvents implements Listener {

    public BukkitEvents(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void sendEvent(Event event, JSONObject object) {
        sendEvent(event.getEventName(), object);
    }

    public static void sendEvent(String event, JSONObject object) {
        JSONObject obj = new JSONObject();
        for (String e : object.keySet()) {
            Object v = object.get(e);
            Object nv = v;
            if (v instanceof Player) nv = convertPlayer((Player) v);
            else if (v instanceof Location) nv = convertLocation((Location) v);
            else if (v instanceof Block) nv = convertBlock((Block) v);
            else if (v instanceof Material) nv = convertMaterial((Material) v);
            obj.put(e,nv);
        }

        Queue<WebSocket> nonExistentSockets = new ConcurrentLinkedQueue<>();

        EventListenerServer.listenedEvents.computeIfAbsent(event, (s) -> new HashSet<>())
                .forEach((ws) -> {
                    if (ws.isClosed()) {
                        nonExistentSockets.add(ws);
                        return;
                    }
                    ws.send(String.format("Event %s %s", event, obj.toString()));
                });

        WebSocket ws;
        while (!nonExistentSockets.isEmpty() && (ws = nonExistentSockets.poll()) != null) {
            EventListenerServer.listenedEvents.get(event).remove(ws);
        }
    }

    public static JSONObject convertPlayer(Player player) {
        JSONObject po = new JSONObject();
        po.put("username", player.getName());
        po.put("uuid", player.getUniqueId().toString());
        return po;
    }

    public static JSONObject convertLocation(Location location) {
        JSONObject loco = new JSONObject();
        loco.put("x", location.getX());
        loco.put("y", location.getY());
        loco.put("z", location.getZ());
        loco.put("world", location.getWorld().getName());
        return loco;
    }

    public static JSONObject convertBlock(Block block) {
        JSONObject bo = new JSONObject();
        bo.put("x", block.getX());
        bo.put("y", block.getY());
        bo.put("z", block.getZ());
        bo.put("material", convertMaterial(block.getType()));
        return bo;
    }

    public static String convertMaterial(Material material) {
        return material.getKey().getKey();
    }

    public static String getVersion() {
        Plugin plugin = MCWebsocketIntegration.getInstance();
        if (plugin.getServer().getVersion().contains("1.18")) {
            return "1.18";
        } else if (plugin.getServer().getVersion().contains("1.17")) {
            return "1.17";
        } else if (plugin.getServer().getVersion().contains("1.16")) {
            return "1.16";
        } else if (plugin.getServer().getVersion().contains("1.15")) {
            return "1.15";
        } else if (plugin.getServer().getVersion().contains("1.14")) {
            return "1.14";
        } else if (plugin.getServer().getVersion().contains("1.13")) {
            return "1.13";
        }
        return "Unknown";
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        JSONObject death = new JSONObject();
        death.put("player", e.getEntity());
        death.put("message", e.getDeathMessage());
        death.put("location", e.getEntity().getLocation());
        sendEvent(e, death);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        JSONObject join = new JSONObject();
        join.put("player", e.getPlayer());
        sendEvent(e, join);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        JSONObject leave = new JSONObject();
        leave.put("player", e.getPlayer());
        sendEvent(e, leave);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        JSONObject msg = new JSONObject();
        msg.put("player", e.getPlayer());
        msg.put("message", e.getPlayer());
        sendEvent("PlayerChatEvent", msg);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        JSONObject dmg = new JSONObject();
        dmg.put("player", p);
        dmg.put("damage", e.getDamage());
        dmg.put("health", p.getHealth());
        dmg.put("cause", e.getCause().toString());
        sendEvent("PlayerDamageEvent", dmg);
    }

    @EventHandler
    public void onPlayerHeal(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        JSONObject heal = new JSONObject();
        heal.put("player", p);
        heal.put("regained", e.getAmount());
        heal.put("health", p.getHealth());
        heal.put("cause", e.getRegainReason().toString());
        sendEvent("PlayerHealEvent", heal);
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent e) {
        JSONObject adv = new JSONObject();
        adv.put("player", e.getPlayer());
        adv.put("advancement", e.getAdvancement().getKey().getKey());
        sendEvent(e, adv);
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent e) {
        JSONObject world = new JSONObject();
        world.put("player", e.getPlayer());
        world.put("world",  e.getPlayer().getWorld().getName());
        world.put("fromWorld", e.getFrom().getName());
        sendEvent(e, world);
    }

    @EventHandler
    public void onPlayerFoodChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        JSONObject food = new JSONObject();
        food.put("player", p);
        food.put("food", e.getFoodLevel());
        switch (getVersion()) {
            case "1.18":
            case "1.17":
            case "1.16":
                food.put("saturation", p.getSaturation());
                break;
        }
        sendEvent("PlayerFoodChangeEvent", food);
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        Player dmgr = (Player) e.getDamager();
        JSONObject dmg = new JSONObject();
        dmg.put("player", p);
        dmg.put("damager", dmgr);
        dmg.put("damage", e.getDamage());
        dmg.put("health", p.getHealth());
        dmg.put("damagerHealth", dmgr.getHealth());
        sendEvent("PlayerDamagePlayerEvent", dmg);
    }

    @EventHandler
    public void onPlayerStatusChange(PlayerStatusChangeEvent e) {
        JSONObject status = new JSONObject();
        status.put("player", e.getPlayer());
        status.put("status", e.getStatus().status);
        status.put("oldStatus", e.getOldStatus().status);
        sendEvent(e, status);
    }

    private static final HashMap<Player, Block> lastBlockOn = new HashMap<>();
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        Block current = e.getTo().getBlock();
        Block last = lastBlockOn.computeIfAbsent(e.getPlayer(), (b) -> current);
        if (last.equals(current)) return;
        lastBlockOn.put(e.getPlayer(), current);
        JSONObject move = new JSONObject();
        move.put("player", e.getPlayer());
        move.put("location", e.getTo());
        move.put("fromLocation", e.getFrom());
        move.put("block", current);
        move.put("standingOn", current.getRelative(BlockFace.DOWN));
        sendEvent(e, move);
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent e) {
        JSONObject xp = new JSONObject();
        xp.put("player", e.getPlayer());
        xp.put("level", e.getNewLevel());
        xp.put("oldLevel", e.getOldLevel());
        xp.put("xp", Math.round(e.getPlayer().getExp() * 10));
        xp.put("neededToLevelUp", e.getPlayer().getExpToLevel());
        sendEvent("PlayerExpChangeEvent", xp);
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        JSONObject xp = new JSONObject();
        Player p = e.getPlayer();
        xp.put("player", p);
        xp.put("level", p.getLevel());
        xp.put("oldLevel", p.getLevel());
        xp.put("xp", Math.round(e.getPlayer().getExp() * 10));
        xp.put("neededToLevelUp", e.getPlayer().getExpToLevel());
        sendEvent("PlayerExpChangeEvent", xp);
    }
}
