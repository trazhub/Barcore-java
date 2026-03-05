package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.List;

public class FilterCommand implements ICommand {

    private static final float[] BASS_BOOST = {
            0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f
    };

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioPlayer player = musicManager.player;

        if (player.getPlayingTrack() == null) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Nothing Playing",
                            "Start playing something before applying a filter.").build())
                    .queue();
            return;
        }

        // No args → show interactive dropdown
        if (args.isEmpty()) {
            StringSelectMenu menu = StringSelectMenu.create("filter_select")
                    .setPlaceholder("🎚️ Choose an audio filter...")
                    .addOption("🎚️ Clear — Remove all filters", "clear")
                    .addOption("🔉 Bass Boost — Enhanced bass", "bassboost")
                    .build();

            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.info(EmbedUtils.FILTER + "  Audio Filters",
                            "Select a filter from the menu below to apply it to the current track.\n\n"
                                    + "• **Clear** — removes all active filters\n"
                                    + "• **Bass Boost** — boosts the low frequencies 🎸")
                            .build())
                    .addActionRow(menu).queue();
            return;
        }

        // Arg provided — apply directly
        applyFilter(event, player, args.get(0).toLowerCase());
    }

    private void applyFilter(MessageReceivedEvent event, AudioPlayer player, String filterName) {
        switch (filterName) {
            case "clear", "reset" -> {
                player.setFilterFactory(null);
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.success("Filters Cleared",
                                EmbedUtils.FILTER + " All audio filters have been removed.").build())
                        .queue();
            }
            case "bassboost" -> {
                EqualizerFactory equalizer = new EqualizerFactory();
                for (int i = 0; i < BASS_BOOST.length; i++)
                    equalizer.setGain(i, BASS_BOOST[i]);
                player.setFilterFactory(equalizer);
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.success("Bass Boost Applied",
                                EmbedUtils.FILTER + " **Bass Boost** is now active. 🎸").build())
                        .queue();
            }
            default -> {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.warning("Unknown Filter",
                                "Available filters: `clear`, `bassboost`\n"
                                        + "Or use `!filter` with no arguments to pick from the menu.")
                                .build())
                        .queue();
            }
        }
    }

    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public String getHelp() {
        return "Applies audio filters — use without args for a menu";
    }
}
