package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class RemoveCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        if (queue.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Queue Empty", "The queue is currently empty.").build()).queue();
            return;
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.warning("No Index Provided",
                            "Usage: `!remove <position>`\nSee `!queue` for track positions.").build())
                    .queue();
            return;
        }

        try {
            int index = Integer.parseInt(args.get(0));
            if (index < 1 || index > queue.size()) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.error("Invalid Index",
                                "Please provide a number between **1** and **" + queue.size() + "**.").build())
                        .queue();
                return;
            }

            List<AudioTrack> trackList = new ArrayList<>(queue);
            AudioTrack removedTrack = trackList.remove(index - 1);

            queue.clear();
            queue.addAll(trackList);

            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.success("Track Removed",
                            EmbedUtils.TRASH + " Removed: **" + removedTrack.getInfo().title + "**").build())
                    .queue();

        } catch (NumberFormatException e) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Invalid Input", "Please provide a valid number.").build()).queue();
        }
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getHelp() {
        return "Removes a song from the queue by position";
    }
}
