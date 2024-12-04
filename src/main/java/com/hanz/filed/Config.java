package com.hanz.filed;

public class Config {
    private final int downloadThreads;
    private final int maxDownloadSpeed;

    public Config(int downloadThreads, int maxDownloadSpeed) {
        this.downloadThreads = downloadThreads;
        this.maxDownloadSpeed = maxDownloadSpeed;
    }

    public int getDownloadThreads() {
        return downloadThreads;
    }

    public int getMaxDownloadSpeed() {
        return maxDownloadSpeed;
    }
}