package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public class NowPlayingCommand implements ICommand {

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        if (track == null) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Nothing Playing",
                            "There is no track currently playing. Use `!play` to start.").build())
                    .queue();
            return;
        }

        long position = track.getPosition();
        long duration = track.getDuration();
        int volume = musicManager.player.getVolume();
        boolean paused = musicManager.player.isPaused();
        boolean repeating = musicManager.scheduler.isRepeating();

        String statusIcon = paused ? EmbedUtils.PAUSE : EmbedUtils.PLAY;

        EmbedBuilder eb = new EmbedBuilder()
                .setColor(EmbedUtils.COLOR_PURPLE)
                .setTitle(EmbedUtils.MUSIC_NOTE + "  Now Playing")
                .setDescription("**[" + track.getInfo().title + "](" + track.getInfo().uri + ")**\n"
                        + "by **" + track.getInfo().author + "**")
                .addField(statusIcon + " Progress", EmbedUtils.progressBar(position, duration), false)
                .addField(EmbedUtils.VOL_UP + " Volume",
                        EmbedUtils.volumeBar(volume), true)
                .addField(EmbedUtils.REPEAT + " Loop",
                        repeating ? "**On** " + EmbedUtils.REPEAT : "Off", true)
                .setFooter("BardCore Music Bot");

        // Control button row
        Button pauseBtn = paused
                ? Button.success("np:resume", "▶️ Resume")
                : Button.primary("np:pause", "⏸️ Pause");
        Button skipBtn = Button.secondary("np:skip", "⏩ Skip");
        Button stopBtn = Button.danger("np:stop", "⏹️ Stop");
        Button shuffleBtn = Button.secondary("np:shuffle", "🔀 Shuffle");
        Button repeatBtn = Button.secondary("np:repeat_toggle", "🔁 Repeat");

        event.getChannel()
                .sendMessageEmbeds(eb.build())
                .addActionRow(pauseBtn, skipBtn, stopBtn, shuffleBtn, repeatBtn)
                .queue();
    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String getHelp() {
        return "Shows the currently playing song with controls";
    }
}
