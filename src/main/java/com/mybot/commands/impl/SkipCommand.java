package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class SkipCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack current = musicManager.player.getPlayingTrack();
        String title = current != null ? "**" + current.getInfo().title + "**" : "the current track";

        musicManager.scheduler.nextTrack();

        event.getChannel().sendMessageEmbeds(
                EmbedUtils.success("Skipped",
                        EmbedUtils.SKIP + " Skipped: " + title).build())
                .queue();
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getHelp() {
        return "Skips the current track";
    }
}
