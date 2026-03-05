package com.mybot;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;

/**
 * Utility class for creating consistent, branded Discord embeds with emojis.
 */
public final class EmbedUtils {

    // ─── Brand Colors ───────────────────────────────────────────────────────────
    public static final Color COLOR_PURPLE = new Color(0x7B4FC8); // primary brand
    public static final Color COLOR_GREEN = new Color(0x2ECC71); // success
    public static final Color COLOR_RED = new Color(0xE74C3C); // error
    public static final Color COLOR_ORANGE = new Color(0xF39C12); // warning / in-progress
    public static final Color COLOR_BLUE = new Color(0x3498DB); // info

    // ─── Emoji Constants ────────────────────────────────────────────────────────
    public static final String MUSIC_NOTE = "🎵";
    public static final String NOTES = "🎶";
    public static final String SKIP = "⏩";
    public static final String PAUSE = "⏸️";
    public static final String PLAY = "▶️";
    public static final String STOP = "⏹️";
    public static final String SHUFFLE = "🔀";
    public static final String REPEAT = "🔁";
    public static final String VOL_DOWN = "🔉";
    public static final String VOL_UP = "🔊";
    public static final String QUEUE_LIST = "📋";
    public static final String PING = "🏓";
    public static final String DOWNLOAD = "⬇️";
    public static final String LYRICS = "🎤";
    public static final String SEEK = "⏱️";
    public static final String FILTER = "🔧";
    public static final String TRASH = "🗑️";
    public static final String CHECK = "✅";
    public static final String CROSS = "❌";
    public static final String WARNING = "⚠️";
    public static final String MOVE = "🔄";
    public static final String JUMP = "🎯";
    public static final String CLOCK = "🕐";

    private EmbedUtils() {
    }

    // ─── Embed Factories ────────────────────────────────────────────────────────

    /** Green "success" embed. */
    public static EmbedBuilder success(String title, String description) {
        return base().setColor(COLOR_GREEN)
                .setTitle(CHECK + "  " + title)
                .setDescription(description);
    }

    /** Red "error" embed. */
    public static EmbedBuilder error(String title, String description) {
        return base().setColor(COLOR_RED)
                .setTitle(CROSS + "  " + title)
                .setDescription(description);
    }

    /** Purple "info" embed (general purpose). */
    public static EmbedBuilder info(String title, String description) {
        return base().setColor(COLOR_PURPLE)
                .setTitle(title)
                .setDescription(description);
    }

    /** Orange "warning" embed. */
    public static EmbedBuilder warning(String title, String description) {
        return base().setColor(COLOR_ORANGE)
                .setTitle(WARNING + "  " + title)
                .setDescription(description);
    }

    // ─── Shared Base ────────────────────────────────────────────────────────────

    private static EmbedBuilder base() {
        return new EmbedBuilder()
                .setFooter("BardCore Music Bot", null);
    }

    // ─── Helper Formatters ──────────────────────────────────────────────────────

    /**
     * Formats milliseconds into a MM:SS string.
     */
    public static String formatDuration(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Renders a unicode progress bar.
     * e.g. ▬▬▬▬●──────── 02:15 / 03:50
     *
     * @param position current position in milliseconds
     * @param duration total duration in milliseconds
     * @return formatted progress string
     */
    public static String progressBar(long position, long duration) {
        int BAR_LENGTH = 14;
        double ratio = duration > 0 ? (double) position / duration : 0;
        int filled = (int) Math.round(ratio * BAR_LENGTH);
        filled = Math.max(0, Math.min(BAR_LENGTH, filled));

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < BAR_LENGTH; i++) {
            if (i == filled) {
                bar.append("●");
            } else if (i < filled) {
                bar.append("▬");
            } else {
                bar.append("─");
            }
        }
        return "`" + bar + "`  " + formatDuration(position) + " / " + formatDuration(duration);
    }

    /**
     * Renders a block-based volume bar.
     * e.g. ████████░░ 80/100
     */
    public static String volumeBar(int volume) {
        int BAR_LENGTH = 10;
        int filled = (int) Math.round(volume / 100.0 * BAR_LENGTH);
        String filled_char = "█";
        String empty_char = "░";
        return "`" + filled_char.repeat(filled) + empty_char.repeat(BAR_LENGTH - filled) + "`  **" + volume + "**/100";
    }
}
