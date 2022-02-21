package com.kk964gaming.mcwebsocket.versions.events;

import com.kk964gaming.mcwebsocket.versions.BukkitEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitEvents_R1_16 extends BukkitEvents {
    public BukkitEvents_R1_16(JavaPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerFoodChange(FoodLevelChangeEvent e) {
        emitEventSockets("PlayerFoodChangeEvent", e.getEntity().getName(), e.getFoodLevel(), e.getEntity().getSaturation());
    }
}
