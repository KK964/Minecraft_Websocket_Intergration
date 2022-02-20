package com.kk964gaming.mcwebsocket;

import org.bukkit.configuration.file.FileConfiguration;
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

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        FileConfiguration conf = getConfig();
        String host = conf.getString("host", "localhost");
        int port = conf.getInt("port", 8887);

        String auth = conf.getString("auth", "");

        debug = conf.getBoolean("debug", false);
        logFailedAuth = conf.getBoolean("log-failed-auth", true);

        List<String> blacklistedCommands = conf.getStringList("blacklist");
        if (!blacklistedCommands.isEmpty()) bannedCommands = blacklistedCommands;

        server = new EventListenerServer(new InetSocketAddress(host, port));
        server.setAuth(auth);

        getServer().getScheduler().runTaskTimer(this, ()->{
            String s = "";
            cmdLoop: while (!commandQueue.isEmpty() && (s = commandQueue.poll()) != null) {
                if (s.startsWith("/")) s = s.substring(1);
                for (String b : bannedCommands) {
                    if (s.toLowerCase().startsWith(b)) {
                        continue cmdLoop;
                    }
                }
                getServer().dispatchCommand(getServer().getConsoleSender(), s);
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

    public EventListenerServer getWebsocket() {
        return server;
    }
}
