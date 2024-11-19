package com.filed;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Filed extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("FILED plugin has been enabled!");
        // Load configuration if needed
    }

    @Override
    public void onDisable() {
        getLogger().info("FILED plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("filed")) {
            if (args.length == 0) {
                return false; // Show usage
            }

            switch (args[0].toLowerCase()) {
                case "download":
                    if (args.length < 2) {
                        sender.sendMessage("Usage: /filed download <link>");
                        return true;
                    }
                    String link = args[1];
                    downloadFile(link, sender);
                    return true;

                case "reload":
                    // Reload configuration logic here
                    sender.sendMessage("Configuration reloaded!");
                    return true;

                default:
                    return false; // Show usage
            }
        }
        return false;
    }

    private void downloadFile(String fileURL, CommandSender sender) {
        new Thread(() -> {
            try {
                URL url = new URL(fileURL);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("GET");
                httpConn.connect();

                if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    sender.sendMessage("No file to download. Server replied HTTP code: " + httpConn.getResponseCode());
                    return;
                }

                // Save file to plugins directory
                File outputFile = new File(getDataFolder(), "downloaded_file"); // Change the filename as needed
                try (InputStream in = new BufferedInputStream(httpConn.getInputStream());
                     FileOutputStream out = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                sender.sendMessage("File downloaded successfully: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                sender.sendMessage("Error downloading file: " + e.getMessage());
            } finally {
                httpConn.disconnect();
            }
        }).start();
    }
  }
