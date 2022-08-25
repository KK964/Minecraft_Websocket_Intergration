package com.kk964gaming.mcwebsocket;

import com.kk964gaming.mcwebsocket.events.CustomEventHandler;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MCWebsocketIntegration extends JavaPlugin {

    private static EventListenerServer server;
    private static MCWebsocketIntegration instance;

    private static final Queue<String> commandQueue = new ConcurrentLinkedQueue<>();

    private static List<String> bannedCommands = new ArrayList<>();

    public static boolean debug = false;
    public static boolean logFailedAuth = true;
    public static boolean hideCommandOutput = true;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        FileConfiguration conf = getConfig();
        int port = conf.getInt("port", 8887);

        String auth = conf.getString("auth", "");

        debug = conf.getBoolean("debug", false);
        logFailedAuth = conf.getBoolean("log-failed-auth", true);
        hideCommandOutput = conf.getBoolean("silence-command-output", true);

        List<String> blacklistedCommands = conf.getStringList("blacklist");
        if (!blacklistedCommands.isEmpty()) bannedCommands = blacklistedCommands;

        server = new EventListenerServer(new InetSocketAddress(port), auth);

        new BukkitEvents(this);
        new CustomEventHandler(this);

        getServer().getScheduler().runTaskTimer(this, ()->{
            String cmd;
            cmdLoop:
            while (!commandQueue.isEmpty() && (cmd = commandQueue.poll()) != null) {
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                for (String banned : bannedCommands) {
                    if (cmd.toLowerCase().startsWith(banned)) continue cmdLoop;
                }

                World world = getServer().getWorlds().get(0);
                if (world == null) break;
                Entity cmdMinecart = world.spawnEntity(new Location(world, 0, 0xFFFFFF, 0), EntityType.MINECART_COMMAND);
                cmdMinecart.setCustomName("MCWS");

                boolean cmdFeedback = world.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK);
                if (hideCommandOutput) world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);

                getServer().dispatchCommand(cmdMinecart, cmd);

                world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, cmdFeedback);
                cmdMinecart.remove();
            }
        }, 1,5);
    }

    @Override
    public void onDisable() {
        try {
            server.end();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void addCommand(String command) {
        commandQueue.add(command);
    }

    public static MCWebsocketIntegration getInstance() {
        return instance;
    }
}
