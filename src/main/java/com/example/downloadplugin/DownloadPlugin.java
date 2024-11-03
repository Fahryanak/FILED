package com.example.downloadplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DownloadPlugin extends JavaPlugin {

    private final File linkFile = new File(getDataFolder(), "link.txt");
    private final File downloadDir = new File(getDataFolder(), "download");
    private final Set<String> downloadedLinks = new HashSet<>();

    @Override
    public void onEnable() {
        // Buat direktori plugin jika belum ada
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Buat file link.txt jika belum ada
        if (!linkFile.exists()) {
            try {
                linkFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create link.txt file!");
                e.printStackTrace();
            }
        }

        // Buat direktori download jika belum ada
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                checkForNewLinks();
            }
        }.runTaskTimer(this, 0L, 20L * 60); // Check every minute
    }

    private void checkForNewLinks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(linkFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!downloadedLinks.contains(line)) {
                    downloadFile(line);
                    downloadedLinks.add(line);
                }
            }
        } catch (IOException e) {
            getLogger().severe("Error reading link.txt file!");
            e.printStackTrace();
        }
    }

    private void downloadFile(String link) {
        getLogger().info("Downloading file from: " + link);
        try {
            URL url = new URL(link);
            String fileName = Paths.get(url.getPath()).getFileName().toString();
            File outputFile = new File(downloadDir, fileName);
            Files.copy(url.openStream(), outputFile.toPath());
            getLogger().info("Downloaded file to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            getLogger().severe("Error downloading file from: " + link);
            e.printStackTrace();
        }
    }
}
