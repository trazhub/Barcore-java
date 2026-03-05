package com.mybot;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private String token;
    private String ownerId;
    private String prefix;
    private String cfAccountId;
    private String cfBucket;
    private String cfAccessKey;
    private String cfSecretKey;
    private String spotifyClientId;
    private String spotifyClientSecret;
    private int dashboardPort;

    public Config() {
        // Determine the directory the JAR file lives in so .env can sit next to it.
        // Falls back to the current working directory if running from an IDE/classpath.
        String envDir = resolveJarDirectory();

        Dotenv dotenv = Dotenv.configure()
                .directory(envDir)
                .ignoreIfMissing()
                .load();

        this.token = dotenv.get("TOKEN");
        this.ownerId = dotenv.get("OWNER_ID");
        this.prefix = dotenv.get("PREFIX", "!");
        this.cfAccountId = dotenv.get("CF_ACCOUNT_ID");
        this.cfBucket = dotenv.get("CF_BUCKET");
        this.cfAccessKey = dotenv.get("CF_ACCESS_KEY");
        this.cfSecretKey = dotenv.get("CF_SECRET_KEY");
        this.spotifyClientId = dotenv.get("SPOTIFY_CLIENT_ID");
        this.spotifyClientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");
        this.dashboardPort = Integer.parseInt(dotenv.get("DASHBOARD_PORT", "8080"));

        if (this.token == null || this.token.isEmpty()) {
            log.error("Token is missing! Please check your .env file or environment variables.");
            System.exit(1);
        }
    }

    /**
     * Returns the directory containing the running JAR file.
     * Falls back to the current working directory when running from an IDE.
     */
    private static String resolveJarDirectory() {
        try {
            java.net.URL source = Config.class.getProtectionDomain()
                    .getCodeSource().getLocation();
            java.io.File jarFile = new java.io.File(source.toURI());
            // If we're inside an exploded classes dir (IDE), go up to project root
            if (jarFile.isDirectory()) {
                return System.getProperty("user.dir");
            }
            return jarFile.getParentFile().getAbsolutePath();
        } catch (Exception e) {
            return System.getProperty("user.dir");
        }
    }

    public String getToken() {
        return token;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getCfAccountId() {
        return cfAccountId;
    }

    public String getCfBucket() {
        return cfBucket;
    }

    public String getCfAccessKey() {
        return cfAccessKey;
    }

    public String getCfSecretKey() {
        return cfSecretKey;
    }

    public String getSpotifyClientId() {
        return spotifyClientId;
    }

    public String getSpotifyClientSecret() {
        return spotifyClientSecret;
    }

    public int getDashboardPort() {
        return dashboardPort;
    }
}
