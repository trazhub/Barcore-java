package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LyricsCommand implements ICommand {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LyricsCommand() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        String songName;

        if (args.isEmpty()) {
            GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
            AudioPlayer player = musicManager.player;
            AudioTrack playingTrack = player.getPlayingTrack();

            if (playingTrack == null) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.error("Nothing Playing",
                                "Provide a song name: `!lyrics <song name>`").build())
                        .queue();
                return;
            }
            songName = playingTrack.getInfo().title;
        } else {
            songName = String.join(" ", args);
        }

        // Searching indicator
        event.getChannel().sendMessageEmbeds(
                EmbedUtils.info(EmbedUtils.LYRICS + "  Searching Lyrics",
                        "Looking up lyrics for **" + songName + "**...").build())
                .queue();

        fetchLyrics(songName).thenAccept(lyrics -> {
            if (lyrics == null || lyrics.isEmpty()) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.error("Lyrics Not Found",
                                "No lyrics were found for **" + songName + "**.").build())
                        .queue();
            } else {
                if (lyrics.length() > 3800) {
                    lyrics = lyrics.substring(0, 3800) + "\n…*(truncated)*";
                }
                EmbedBuilder eb = new EmbedBuilder()
                        .setColor(EmbedUtils.COLOR_PURPLE)
                        .setTitle(EmbedUtils.LYRICS + "  Lyrics — " + songName)
                        .setDescription(lyrics)
                        .setFooter("BardCore Music Bot  •  Powered by Lyrist");
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
            }
        });
    }

    private CompletableFuture<String> fetchLyrics(String songName) {
        String url = "https://lyrist.vercel.app/api/" + songName.replace(" ", "%20");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseLyrics);
    }

    private String parseLyrics(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("lyrics")) {
                return root.get("lyrics").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getName() {
        return "lyrics";
    }

    @Override
    public String getHelp() {
        return "Shows lyrics for the current song or a search query";
    }
}
