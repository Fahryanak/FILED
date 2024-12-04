package com.hanz.filed;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class filed extends JavaPlugin {
    private Config config;

    @Override
    public void onEnable() {
        getLogger().info("DownloadPlugin is enabled!");

        // Load or create config
        saveDefaultConfig();
        config = new Config(getConfig().getInt("downloadThreads", 4),
                            getConfig().getInt("maxDownloadSpeed", 1024));

        // Register command
        getCommand("download").setExecutor(new DownloadCommand(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("DownloadPlugin is disabled!");
    }

    public Config getPluginConfig() {
        return config;
    }
}