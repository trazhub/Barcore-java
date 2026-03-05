package com.mybot;

import io.javalin.Javalin;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class WebDashboard {
    private static final Logger log = LoggerFactory.getLogger(WebDashboard.class);
    private final Javalin app;
    private final JDA jda;

    public WebDashboard(Config config, JDA jda) {
        this.jda = jda;
        this.app = Javalin.create(c -> {
            c.showJavalinBanner = false;
        }).start(config.getDashboardPort());

        app.get("/", ctx -> ctx.html(getDashboardHtml()));
        app.get("/api/stats", ctx -> ctx.json(getStats()));

        log.info("Web Dashboard started on port {}", config.getDashboardPort());
    }

    private Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // JDA Stats
        stats.put("guilds", jda.getGuilds().size());
        stats.put("users", jda.getUsers().size());
        stats.put("ping", jda.getGatewayPing());

        // System Stats
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedMemory = heapUsage.getUsed() / (1024 * 1024);
        long maxMemory = heapUsage.getMax() / (1024 * 1024);

        stats.put("ram_used", usedMemory);
        stats.put("ram_max", maxMemory);
        stats.put("uptime", formatDuration(Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime())));

        // Audio Stats (Approximate)
        long activePlayers = jda.getGuilds().stream()
                .filter(g -> g.getAudioManager().isConnected())
                .count();
        stats.put("active_players", activePlayers);

        return stats;
    }

    private String getDashboardHtml() {
        return """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>BardCore Dashboard</title>
                        <style>
                            body { font-family: sans-serif; background: #121212; color: #fff; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
                            .card { background: #1e1e1e; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.3); width: 300px; text-align: center; }
                            h1 { margin-top: 0; color: #bb86fc; }
                            .stat { margin: 1rem 0; display: flex; justify-content: space-between; border-bottom: 1px solid #333; padding-bottom: 0.5rem; }
                            .stat-label { color: #888; }
                            .stat-value { font-weight: bold; }
                            .refresh { margin-top: 1rem; font-size: 0.8rem; color: #666; }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <h1>BardCore Status</h1>
                            <div id="stats">
                                <div class="stat"><span class="stat-label">Status</span><span class="stat-value" style="color: #03dac6">Online</span></div>
                                <div class="stat"><span class="stat-label">Ping</span><span class="stat-value" id="ping">...</span></div>
                                <div class="stat"><span class="stat-label">Servers</span><span class="stat-value" id="guilds">...</span></div>
                                <div class="stat"><span class="stat-label">Users</span><span class="stat-value" id="users">...</span></div>
                                <div class="stat"><span class="stat-label">Active Players</span><span class="stat-value" id="active_players">...</span></div>
                                <div class="stat"><span class="stat-label">RAM Usage</span><span class="stat-value" id="ram">...</span></div>
                                <div class="stat"><span class="stat-label">Uptime</span><span class="stat-value" id="uptime">...</span></div>
                            </div>
                        </div>
                        <script>
                            async function fetchStats() {
                                try {
                                    const res = await fetch('/api/stats');
                                    const data = await res.json();
                                    document.getElementById('ping').innerText = data.ping + ' ms';
                                    document.getElementById('guilds').innerText = data.guilds;
                                    document.getElementById('users').innerText = data.users;
                                    document.getElementById('active_players').innerText = data.active_players;
                                    document.getElementById('ram').innerText = data.ram_used + ' / ' + data.ram_max + ' MB';
                                    document.getElementById('uptime').innerText = data.uptime;
                                } catch (e) {
                                    console.error(e);
                                }
                            }
                            setInterval(fetchStats, 2000);
                            fetchStats();
                        </script>
                    </body>
                    </html>
                """;
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        return String.format("%dd %02dh %02dm", days, hours, minutes);
    }
}
