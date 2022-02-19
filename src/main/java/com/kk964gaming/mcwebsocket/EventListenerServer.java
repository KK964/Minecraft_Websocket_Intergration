package com.kk964gaming.mcwebsocket;

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

    class WebServer extends WebSocketServer {
        private final Set<WebSocket> authenticated = new HashSet<>();

        public WebServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Bukkit.getLogger().info("New connection from " + conn.getRemoteSocketAddress());
            if (auth != null) {
                conn.send("Authentication needed... Send as \"Bearer {Auth token}\"");
                Bukkit.getLogger().info("Waiting for  " + conn.getRemoteSocketAddress() + " to authenticate");
                return;
            }
            authenticated.add(conn);
            conn.send("Authentication not required... Send commands to run as \"Command {command}\", use the execute command for doing relative positions");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Bukkit.getLogger().info("Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
            authenticated.remove(conn);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            if (!authenticated.contains(conn)) {
                if (!message.startsWith("Bearer ")) {
                    conn.send("You must supply an Authentication Bearer token first!");
                    return;
                }
                String suppliedAuth = message.substring("Bearer ".length());
                if (!auth.equals(suppliedAuth)) {
                    conn.send("Incorrect Authentication Bearer token supplied!");
                    Bukkit.getLogger().warning("authentication failed from " + conn.getRemoteSocketAddress());
                    return;
                }
                authenticated.add(conn);
                conn.send("Successfully Authenticated.... Send commands to run as \"Command {command}\", use the execute command for doing relative positions");
                Bukkit.getLogger().info("authentication succeeded from " + conn.getRemoteSocketAddress());
                return;
            }
            Bukkit.getLogger().info("received message from " + conn.getRemoteSocketAddress() + ": " + message);
            String[] toRun = message.split("\n");
            int totalCommandsAdded = 0;
            for (String r : toRun) {
                if (r.startsWith("Command")) {
                    String cmd = r.substring("Command ".length());
                    MCWebsocketIntegration.addCommand(cmd);
                    totalCommandsAdded++;
                }
            }
            conn.send("Added " + totalCommandsAdded + " to queue!");
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            if (!authenticated.contains(conn)) {
                conn.send("You must supply an Authentication Bearer token first!");
                return;
            }
            Bukkit.getLogger().info("received ByteBuffer from " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            Bukkit.getLogger().warning("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
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
