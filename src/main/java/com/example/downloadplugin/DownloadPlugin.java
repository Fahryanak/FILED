package com.example.downloadplugin;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadPlugin extends JavaPlugin {
    private final File linkFile = new File(getDataFolder(), "link.txt");
    private File downloadDir;
    private final Set<String> downloadedLinks = new HashSet<>();
    private int checkInterval;
    private int concurrentDownloads;

    @Override
    public void onEnable() {
        loadConfig();
        setupDownloadDirectory();
        createLinkFile();
        scheduleDownloadTask();
    }

    private void loadConfig() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        checkInterval = config.getInt("check-interval", 60);
        concurrentDownloads = config.getInt("concurrent-downloads", 4);

        String downloadLocation = config.getString("download-location", "plugin");
        String customPath = config.getString("custom-path", "");

        switch (downloadLocation.toLowerCase()) {
            case "server":
                downloadDir = new File(getServer().getWorldContainer(), "downloads");
                break;
            case "custom":
                if (!customPath.isEmpty()) {
                    downloadDir = new File(customPath);
                } else {
                    getLogger().severe("Custom path is empty! Defaulting to plugin directory.");
                    downloadDir = new File(getDataFolder(), "download");
                }
                break;
            case "plugin":
            default:
                downloadDir = new File(getDataFolder(), "download");
                break;
        }
    }

    private void setupDownloadDirectory() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
    }

    private void createLinkFile() {
        if (!linkFile.exists()) {
            try {
                linkFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create link.txt file!");
                e.printStackTrace();
            }
        }
    }

    private void scheduleDownloadTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForNewLinks();
            }
        }.runTaskTimer(this, 0L, 20L * checkInterval);
    }

    private void checkForNewLinks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(linkFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!downloadedLinks.contains(line)) {
                    if (line.endsWith("/")) {
                        downloadDirectory(line);
                    } else {
                        downloadFile(line);
                    }
                    downloadedLinks.add(line);
                }
            }
        } catch (IOException e) {
            getLogger().severe("Error reading link.txt file!");
            e.printStackTrace();
        }
    }

    private URL resolveDownloadLink(String originalLink) throws Exception {
        if (originalLink.contains("spigotmc.org")) {
            return resolveSpigotmcLink(originalLink);
        } else if (originalLink.contains("github.com")) {
            return resolveGitHubLink(originalLink);
        } else if (originalLink.contains("modrinth.com")) {
            return resolveModrinthLink(originalLink);
        }
        return new URL(originalLink);
    }

    private URL resolveSpigotmcLink(String spigotLink) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(spigotLink);
            request.addHeader("User-Agent", "Mozilla/5.0");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    String htmlContent = EntityUtils.toString(response.getEntity());
                    Pattern pattern = Pattern.compile("href=\"(/resources/[^\"]+download[^\"]+)\"");
                    Matcher matcher = pattern.matcher(htmlContent);
                    
                    if (matcher.find()) {
                        String downloadPath = matcher.group(1); return new URL("https://www.spigotmc.org" + downloadPath);
                    }
                }
            }
        }
        throw new Exception("Could not resolve SpigotMC download link");
    }

    private URL resolveGitHubLink(String githubLink) throws Exception {
        Document doc = Jsoup.connect(githubLink)
            .userAgent("Mozilla/5.0")
            .get();

        String downloadLink = doc.select("a.js-permalink-replaceable").first().attr("href");
        return new URL("https://github.com" + downloadLink);
    }

    private URL resolveModrinthLink(String modrinthLink) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(modrinthLink);
            request.addHeader("User -Agent", "Mozilla/5.0");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String htmlContent = EntityUtils.toString(response.getEntity());
                Pattern pattern = Pattern.compile("\"downloadUrl\":\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(htmlContent);
                
                if (matcher.find()) {
                    return new URL(matcher.group(1));
                }
            }
        }
        throw new Exception("Could not resolve Modrinth download link");
    }

    private void downloadFile(String link) {
        getLogger().info("Starting download from: " + link);
        try {
            URL resolvedUrl = resolveDownloadLink(link);
            
            if (resolvedUrl == null) {
                getLogger().severe("Could not resolve download link: " + link);
                return;
            }

            String fileName = extractFileName(resolvedUrl);
            File outputFile = new File(downloadDir, fileName);

            if (outputFile.exists()) {
                getLogger().info("File already exists: " + outputFile.getAbsolutePath());
                return;
            }

            HttpURLConnection connection = (HttpURLConnection) resolvedUrl.openConnection();
            connection.setRequestProperty("User -Agent", "Mozilla/5.0");
            connection.setFollowRedirects(true);

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, outputFile.toPath());
                getLogger().info("Downloaded file to: " + outputFile.getAbsolutePath());
            }

        } catch (Exception e) {
            getLogger().severe("Error downloading file from: " + link);
            e.printStackTrace();
        }
    }

    private String extractFileName(URL url) {
        String path = url.getPath();
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        
        try {
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            // Use original file name if decoding fails
        }

        if (fileName.isEmpty()) {
            fileName = "downloaded_" + System.currentTimeMillis() + ".jar";
        }

        return fileName;
    }
}
