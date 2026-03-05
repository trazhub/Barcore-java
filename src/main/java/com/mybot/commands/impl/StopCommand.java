package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class StopCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        musicManager.scheduler.stop();
        event.getGuild().getAudioManager().closeAudioConnection();

        event.getChannel().sendMessageEmbeds(
                EmbedUtils.info(EmbedUtils.STOP + "  Stopped",
                        "Music stopped and the queue has been cleared.\n"
                                + "Use `!play` to start again " + EmbedUtils.MUSIC_NOTE)
                        .build())
                .queue();
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getHelp() {
        return "Stops the music and clears the queue";
    }
}
