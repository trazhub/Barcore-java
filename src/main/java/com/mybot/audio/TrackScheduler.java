package com.mybot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private boolean repeating = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (repeating) {
                player.startTrack(track.makeClone(), false);
                return;
            }
            nextTrack();
        }
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void shuffle() {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        Collections.shuffle(tracks);
        queue.clear();
        queue.addAll(tracks);
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void move(int fromIndex, int toIndex) {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        if (fromIndex >= 0 && fromIndex < tracks.size() && toIndex >= 0 && toIndex < tracks.size()) {
            AudioTrack track = tracks.remove(fromIndex);
            tracks.add(toIndex, track);
            queue.clear();
            queue.addAll(tracks);
        }
    }

    public void jump(int index) {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        if (index >= 0 && index < tracks.size()) {
            queue.clear();
            queue.addAll(tracks.subList(index, tracks.size()));
            player.stopTrack();
            nextTrack();
        }
    }

    public void stop() {
        queue.clear();
        player.stopTrack();
    }
}
