package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ShuffleCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        BlockingQueue<?> queue = musicManager.scheduler.getQueue();
        int size = queue.size();

        musicManager.scheduler.shuffle();

        event.getChannel().sendMessageEmbeds(
                EmbedUtils.success("Queue Shuffled",
                        EmbedUtils.SHUFFLE + " **" + size + "** tracks have been randomized!").build())
                .queue();
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getHelp() {
        return "Shuffles the current queue";
    }
}
