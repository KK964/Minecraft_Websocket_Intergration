package com.kk964gaming.mcwebsocket;

import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class EventListenerServer extends Thread {

    private static final String commandHelp = "Send commands to run as \"Command {command}\", use the execute command for doing relative positions";
    private static final String listenerHelp = "Listen to events by running \"Listen {event}\", stop listening by running \"Ignore {event}\".";
    private static final String eventHelp = "The full documentation for Events can be found on https://github.com/KK964/Minecraft_Websocket_Intergration/wiki/Events";

    private static byte[] auth;
    private final InetSocketAddress address;
    private WebServer server;

    public static final HashMap<String, HashSet<WebSocket>> listenedEvents = new HashMap<>();

    public EventListenerServer(InetSocketAddress address, String auth) {
        this.address = address;
        if (auth != null && !auth.equals("")) EventListenerServer.auth = hash(auth);
        start();
    }

    public static byte[] hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        return new byte[0];
    }

    @Override
    public void run() {
        server = new WebServer(address);
        server.run();
    }

    public void end() throws InterruptedException {
        server.getConnections().forEach((c) -> c.closeConnection(500, "Server Closed"));
        server.stop();
    }

    static class WebServer extends WebSocketServer {
        private final Set<WebSocket> authenticated = new HashSet<>();

        public WebServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("New connection from " + conn.getRemoteSocketAddress());
            if (auth != null) {
                conn.send("Authentication needed... Send as \"Bearer {Auth token}\"");
                if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("Waiting for  " + conn.getRemoteSocketAddress() + " to authenticate");
                return;
            }
            authenticated.add(conn);
            conn.send("Authentication not required...\n" + commandHelp + "\n" + listenerHelp + "\n" + eventHelp);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
            authenticated.remove(conn);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            if (!authenticated.contains(conn)) {
                if (!message.startsWith("Bearer ")) {
                    if (MCWebsocketIntegration.debug) conn.send("You must supply an Authentication Bearer token first!");
                    return;
                }
                String suppliedAuth = message.substring("Bearer ".length());
                if (!Arrays.equals(auth, hash(suppliedAuth))) {
                    conn.send("Incorrect Authentication Bearer token supplied!");
                    if (MCWebsocketIntegration.logFailedAuth || MCWebsocketIntegration.debug) Bukkit.getLogger().warning("authentication failed from " + conn.getRemoteSocketAddress());
                    return;
                }
                authenticated.add(conn);
                conn.send("Successfully Authenticated...\n" + commandHelp + "\n" + listenerHelp + "\n" + eventHelp);
                if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("authentication succeeded from " + conn.getRemoteSocketAddress());
                return;
            }

            if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("received message from " + conn.getRemoteSocketAddress() + ": " + message);

            String[] toRun = message.split("\n");

            for (String checking : toRun) {
                String action = checking.split(" ")[0].toLowerCase();
                String input = checking.substring(action.length() + 1);
                switch (action) {
                    case "command": {
                        MCWebsocketIntegration.addCommand(input);
                        break;
                    }
                    case "listen": {
                        listenedEvents.computeIfAbsent(input, (s) -> new HashSet<>()).add(conn);
                        break;
                    }
                    case "ignore": {
                        listenedEvents.computeIfAbsent(input, (s) -> new HashSet<>()).remove(conn);
                        break;
                    }
                }
            }
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {}

        @Override
        public void onError(WebSocket conn, Exception ex) {
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().warning("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
        }

        @Override
        public void onStart() {
            Bukkit.getLogger().info("Successfully started websocket server on " + getAddress().getHostName() + ":" + getAddress().getPort());
        }
    }
}
