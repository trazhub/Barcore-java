package com.mybot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.mybot.Config;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();

        // Register the new YouTube source manager
        dev.lavalink.youtube.YoutubeAudioSourceManager youtube = new dev.lavalink.youtube.YoutubeAudioSourceManager();
        playerManager.registerSourceManager(youtube);

        Config config = new Config();
        String spotifyId = config.getSpotifyClientId();
        String spotifySecret = config.getSpotifyClientSecret();
        if (spotifyId != null && !spotifyId.isBlank()
                && spotifySecret != null && !spotifySecret.isBlank()) {
            playerManager.registerSourceManager(new SpotifySourceManager(null, spotifyId,
                    spotifySecret, "US", playerManager));
        }

        // Register other sources manually
        playerManager.registerSourceManager(
                com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(
                new com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager());

        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        return musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(playerManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
            return musicManager;
        });
    }

    public void loadAndPlay(AudioChannel channel, MessageChannel messageChannel, String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl,
                new com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(com.sedmelluq.discord.lavaplayer.track.AudioTrack track) {
                        play(musicManager, track);
                        messageChannel.sendMessage("Adding to queue: " + track.getInfo().title).queue();
                    }

                    @Override
                    public void playlistLoaded(com.sedmelluq.discord.lavaplayer.track.AudioPlaylist playlist) {
                        com.sedmelluq.discord.lavaplayer.track.AudioTrack firstTrack = playlist.getSelectedTrack();
                        if (firstTrack == null) {
                            firstTrack = playlist.getTracks().get(0);
                        }
                        play(musicManager, firstTrack);
                        messageChannel.sendMessage("Added playlist: " + playlist.getName() + " (first track: "
                                + firstTrack.getInfo().title + ")").queue();
                    }

                    @Override
                    public void noMatches() {
                        messageChannel.sendMessage("Nothing found by " + trackUrl).queue();
                    }

                    @Override
                    public void loadFailed(com.sedmelluq.discord.lavaplayer.tools.FriendlyException exception) {
                        messageChannel.sendMessage("Could not play: " + exception.getMessage()).queue();
                    }
                });
    }

    private void play(GuildMusicManager musicManager, com.sedmelluq.discord.lavaplayer.track.AudioTrack track) {
        musicManager.scheduler.queue(track);
    }
}
