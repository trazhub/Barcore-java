package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SeekCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.warning("No Timestamp",
                            "Usage: `!seek <mm:ss>` or `!seek <seconds>`\nExample: `!seek 1:30` or `!seek 90`").build())
                    .queue();
            return;
        }

        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        if (track == null) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Nothing Playing", "There is no track to seek in.").build()).queue();
            return;
        }

        if (!track.isSeekable()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Not Seekable", "This track does not support seeking.").build()).queue();
            return;
        }

        String input = args.get(0);
        long milliseconds;

        try {
            if (Pattern.matches("\\d+:\\d+", input)) {
                String[] parts = input.split(":");
                long minutes = Long.parseLong(parts[0]);
                long seconds = Long.parseLong(parts[1]);
                milliseconds = TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);
            } else {
                long seconds = Long.parseLong(input);
                milliseconds = TimeUnit.SECONDS.toMillis(seconds);
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Invalid Format",
                            "Use seconds `!seek 90` or mm:ss `!seek 1:30`").build())
                    .queue();
            return;
        }

        if (milliseconds > track.getDuration()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Beyond Track Length",
                            "Cannot seek past the end of the track (`"
                                    + EmbedUtils.formatDuration(track.getDuration()) + "`).")
                            .build())
                    .queue();
            return;
        }

        track.setPosition(milliseconds);
        event.getChannel().sendMessageEmbeds(
                EmbedUtils.success("Seeked",
                        EmbedUtils.SEEK + " Jumped to **" + EmbedUtils.formatDuration(milliseconds) + "** / "
                                + EmbedUtils.formatDuration(track.getDuration()))
                        .build())
                .queue();
    }

    @Override
    public String getName() {
        return "seek";
    }

    @Override
    public String getHelp() {
        return "Seeks to a timestamp in the current song (e.g. !seek 1:30)";
    }
}
