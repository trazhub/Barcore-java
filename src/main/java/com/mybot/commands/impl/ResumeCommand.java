package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class ResumeCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        musicManager.player.setPaused(false);

        String desc = track != null
                ? EmbedUtils.PLAY + " Resumed: **" + track.getInfo().title + "**"
                : EmbedUtils.PLAY + " Playback resumed.";

        event.getChannel().sendMessageEmbeds(
                EmbedUtils.success("Resumed", desc).build()).queue();
    }

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public String getHelp() {
        return "Resumes the music";
    }
}
