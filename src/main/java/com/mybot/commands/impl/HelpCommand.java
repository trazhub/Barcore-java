package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.commands.CommandManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;

public class HelpCommand implements ICommand {
    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    // Categorise commands for the two embed fields
    private static final List<String> PLAYBACK_CMDS = List.of(
            "play", "pause", "resume", "stop", "skip", "nowplaying",
            "seek", "volume", "repeat", "shuffle", "filter");

    @Override
    @SuppressWarnings("null")
    public void handle(MessageReceivedEvent event, List<String> args) {
        List<ICommand> commands = manager.getCommands();

        StringBuilder playback = new StringBuilder();
        StringBuilder queueUtil = new StringBuilder();

        for (ICommand cmd : commands) {
            String line = "`!" + cmd.getName() + "` — " + cmd.getHelp() + "\n";
            if (PLAYBACK_CMDS.contains(cmd.getName())) {
                playback.append(line);
            } else {
                queueUtil.append(line);
            }
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(EmbedUtils.COLOR_PURPLE)
                .setTitle(EmbedUtils.MUSIC_NOTE + "  BardCore — Command Reference")
                .setDescription("Use `!<command>` to invoke. All commands work in a server text channel.")
                .addField(EmbedUtils.PLAY + " Playback", playback.toString(), false)
                .addField(EmbedUtils.QUEUE_LIST + " Queue & Utility", queueUtil.toString(), false)
                .setFooter("BardCore Music Bot • " + commands.size() + " commands available");

        // Build a select menu that lets users pick a command for quick-reference
        StringSelectMenu menu = StringSelectMenu.create("help_select")
                .setPlaceholder("🔍 Jump to a command...")
                .addOption("▶️ play — Play a song", "play")
                .addOption("⏭️ skip — Skip current track", "skip")
                .addOption("⏸️ pause — Pause playback", "pause")
                .addOption("▶️ resume — Resume playback", "resume")
                .addOption("⏹️ stop — Stop & clear queue", "stop")
                .addOption("📋 queue — View queue", "queue")
                .addOption("🎵 nowplaying — Current song", "nowplaying")
                .addOption("🔊 volume — Set volume", "volume")
                .addOption("🔁 repeat — Toggle repeat", "repeat")
                .addOption("🔀 shuffle — Shuffle queue", "shuffle")
                .addOption("🔧 filter — Audio filters", "filter")
                .addOption("🎤 lyrics — Show lyrics", "lyrics")
                .addOption("🏓 ping — Check latency", "ping")
                .build();

        event.getChannel()
                .sendMessageEmbeds(eb.build())
                .addActionRow(menu)
                .queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp() {
        return "Shows the list of commands";
    }
}
