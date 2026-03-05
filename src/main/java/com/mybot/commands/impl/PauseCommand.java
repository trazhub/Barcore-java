package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class PauseCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        if (track == null) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Nothing Playing", "There is no music currently playing.").build()).queue();
            return;
        }

        musicManager.player.setPaused(true);
        event.getChannel().sendMessageEmbeds(
                EmbedUtils.info(EmbedUtils.PAUSE + "  Paused",
                        "Paused: **" + track.getInfo().title + "**\n"
                                + "Use `!resume` to continue playback.")
                        .build())
                .queue();
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getHelp() {
        return "Pauses the current song";
    }
}
