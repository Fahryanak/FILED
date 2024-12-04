package com.hanz.filed;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {
    private final File configFile;
    private final FileConfiguration config;

    public Config(filedmain plugin) {
        // File config.yml akan berada di direktori plugin
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // Jika config.yml belum ada, buat dengan nilai default
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs(); // Pastikan folder plugin ada
            config.set("downloadThreads", 6);
            config.set("maxDownloadSpeed", 1024); // KBps
            saveConfig(); // Simpan nilai default ke file
        }
    }

    // Mendapatkan jumlah thread untuk download
    public int getDownloadThreads() {
        return config.getInt("downloadThreads", 6); // Default 6
    }

    // Mendapatkan kecepatan download maksimum
    public int getMaxDownloadSpeed() {
        return config.getInt("maxDownloadSpeed", 1024); // Default 1024 KBps
    }

    // Menyimpan konfigurasi ke file
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}