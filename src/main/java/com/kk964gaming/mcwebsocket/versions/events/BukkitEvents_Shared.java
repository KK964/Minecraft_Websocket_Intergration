package com.kk964gaming.mcwebsocket.versions.events;

import com.kk964gaming.mcwebsocket.events.PlayerStatusChangeEvent;
import com.kk964gaming.mcwebsocket.versions.BukkitEvents;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class BukkitEvents_Shared extends BukkitEvents {
    public BukkitEvents_Shared(JavaPlugin plugin) {
        super(plugin);
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
        emitEventSockets("PlayerFoodChangeEvent", e.getEntity().getName(), e.getFoodLevel());
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

    private static final HashMap<Player, Block> lastBlock = new HashMap<>();
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        Block last = lastBlock.computeIfAbsent(e.getPlayer(), (b) -> e.getTo().getBlock());
        if (last.equals(e.getTo().getBlock())) return;
        lastBlock.put(e.getPlayer(), e.getTo().getBlock());
        emitEventSockets(e, e.getPlayer().getName(),
                convertLocationToJSONObject(e.getTo()),
                convertLocationToJSONObject(e.getFrom()),
                e.getTo().getBlock().getType().getKey().getKey(),
                e.getTo().getBlock().getRelative(BlockFace.DOWN).getType().getKey().getKey());
    }
}
