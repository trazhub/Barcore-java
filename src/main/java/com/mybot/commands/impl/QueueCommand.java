package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand implements ICommand {

    private static final int PAGE_SIZE = 10;

    @Override
    @SuppressWarnings("null")
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        if (queue.isEmpty()) {
            AudioTrack playing = musicManager.player.getPlayingTrack();
            String desc = playing != null
                    ? EmbedUtils.MUSIC_NOTE + " Now playing: **" + playing.getInfo().title
                            + "**\n\nNo tracks queued after this one."
                    : "The queue is empty. Use `!play` to add songs.";
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.info(EmbedUtils.QUEUE_LIST + "  Queue", desc).build()).queue();
            return;
        }

        // Determine page
        int page = 0;
        if (!args.isEmpty()) {
            try {
                page = Math.max(0, Integer.parseInt(args.get(0)) - 1);
            } catch (NumberFormatException ignored) {
            }
        }

        List<AudioTrack> trackList = new ArrayList<>(queue);
        int totalPages = (int) Math.ceil((double) trackList.size() / PAGE_SIZE);
        page = Math.min(page, totalPages - 1);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, trackList.size());

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            AudioTrack t = trackList.get(i);
            sb.append("**").append(i + 1).append(".** ")
                    .append(t.getInfo().title)
                    .append(" — `").append(EmbedUtils.formatDuration(t.getDuration())).append("`\n");
        }

        AudioTrack nowPlaying = musicManager.player.getPlayingTrack();
        String footer = "BardCore Music Bot"
                + (nowPlaying != null ? "  •  Now: " + nowPlaying.getInfo().title : "");

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(EmbedUtils.COLOR_PURPLE)
                .setTitle(EmbedUtils.QUEUE_LIST + "  Queue — Page " + (page + 1) + "/" + totalPages
                        + "  |  " + trackList.size() + " tracks")
                .setDescription(sb.toString())
                .setFooter(footer);

        // Pagination buttons
        Button prevBtn = Button.secondary("queue:prev:" + page, "◀ Previous");
        Button nextBtn = Button.secondary("queue:next:" + page, "Next ▶");

        if (page == 0)
            prevBtn = prevBtn.asDisabled();
        if (page >= totalPages - 1)
            nextBtn = nextBtn.asDisabled();

        event.getChannel()
                .sendMessageEmbeds(eb.build())
                .addActionRow(prevBtn, nextBtn)
                .queue();
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getHelp() {
        return "Shows the current queue with pagination";
    }
}
