package com.kk964gaming.mcwebsocket;

import com.kk964gaming.mcwebsocket.versions.VersionManager;
import com.kk964gaming.mcwebsocket.versions.events.BukkitEvents_R1_16;
import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class EventListenerServer extends Thread {

    private String auth;
    private final InetSocketAddress address;
    private WebServer server;

    public EventListenerServer(InetSocketAddress address) {
        this.address = address;
        start();
    }

    @Override
    public void run() {
        server = new WebServer(address);
        server.run();
    }

    public void end() throws InterruptedException {
        server.kickAll();
        server.stop();
    }

    public void setAuth(String auth) {
        if (auth == null || auth.equals("")) return;
        this.auth = auth;
    }

    public void sendAll(String s) {
        server.broadcast(s);
    }

    public void sendToIn(Set<String> s, String s1) {
        for (WebSocket ws : server.getConnections()) {
            if (s.contains(ws.getRemoteSocketAddress().toString())) {
                sendTo(ws, s1);
            }
        }
    }

    public void sendTo(WebSocket ws, String s1) {
        ws.send(s1);
    }

    class WebServer extends WebSocketServer {
        private final Set<WebSocket> authenticated = new HashSet<>();

        public WebServer(InetSocketAddress address) {
            super(address);
        }

        private static final String commandHelp = "Send commands to run as \"Command {command}\", use the execute command for doing relative positions";
        private static final String listenerHelp = "Listen to events by running \"Listen {event}\", stop listening by running \"Ignore {event}\". The full documentation for Events can be found on https://github.com/KK964/Minecraft_Websocket_Intergration/wiki/Events";

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("New connection from " + conn.getRemoteSocketAddress());
            if (auth != null) {
                conn.send("Authentication needed... Send as \"Bearer {Auth token}\"");
                if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("Waiting for  " + conn.getRemoteSocketAddress() + " to authenticate");
                return;
            }
            authenticated.add(conn);
            conn.send("Authentication not required..." + "\n" + commandHelp + "\n" + listenerHelp);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
            authenticated.remove(conn);
            VersionManager.registeredEvents.forEach((k, v) -> v.remove(conn.getRemoteSocketAddress().toString()));
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            if (!authenticated.contains(conn)) {
                if (!message.startsWith("Bearer ")) {
                    if (MCWebsocketIntegration.debug) conn.send("You must supply an Authentication Bearer token first!");
                    return;
                }
                String suppliedAuth = message.substring("Bearer ".length());
                if (!auth.equals(suppliedAuth)) {
                    conn.send("Incorrect Authentication Bearer token supplied!");
                    if (MCWebsocketIntegration.logFailedAuth || MCWebsocketIntegration.debug) Bukkit.getLogger().warning("authentication failed from " + conn.getRemoteSocketAddress());
                    return;
                }
                authenticated.add(conn);
                conn.send("Successfully Authenticated..." + "\n" + commandHelp + "\n" + listenerHelp);
                if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("authentication succeeded from " + conn.getRemoteSocketAddress());
                return;
            }
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("received message from " + conn.getRemoteSocketAddress() + ": " + message);
            String[] toRun = message.split("\n");
            int totalCommandsAdded = 0;
            int totalListeners = 0;
            for (String r : toRun) {
                if (r.startsWith("Command")) {
                    String cmd = r.substring("Command ".length());
                    MCWebsocketIntegration.addCommand(cmd);
                    totalCommandsAdded++;
                } else if (r.startsWith("Listen")) {
                    String event = r.substring("Listen ".length());
                    BukkitEvents_R1_16.getEventListeners(event).add(conn.getRemoteSocketAddress().toString());
                    totalListeners++;
                } else if (r.startsWith("Ignore")) {
                    String event = r.substring("Ignore ".length());
                    BukkitEvents_R1_16.getEventListeners(event).remove(conn.getRemoteSocketAddress().toString());
                    totalListeners++;
                }
            }
            String returnString = "";
            if (totalCommandsAdded > 0) returnString += "Added " + totalCommandsAdded + " to queue! ";
            if (totalListeners > 0) returnString += "Changed " + totalListeners + " listeners! ";
            if (returnString.length() > 0) conn.send(returnString);
            else conn.send("No changes made.");
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            if (!authenticated.contains(conn)) {
                conn.send("You must supply an Authentication Bearer token first!");
                return;
            }
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().info("received ByteBuffer from " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            if (MCWebsocketIntegration.debug) Bukkit.getLogger().warning("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
        }

        @Override
        public void onStart() {
            Bukkit.getLogger().info("Successfully started websocket server on " + getAddress().getHostName() + ":" + getAddress().getPort());
        }

        public void kickAll() {
            getConnections().forEach((c) -> c.closeConnection(0,"server closed"));
        }
    }
}
