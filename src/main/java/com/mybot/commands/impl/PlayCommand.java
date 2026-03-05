package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class PlayCommand implements ICommand {

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        Member member = event.getMember();
        if (member == null)
            return;

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null)
            return;

        if (!voiceState.inAudioChannel()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Not in Voice Channel",
                            EmbedUtils.WARNING + " You need to be in a voice channel to play music.").build())
                    .queue();
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();
        if (selfVoiceState == null)
            return;

        if (!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(voiceState.getChannel());
        } else {
            if (selfVoiceState.getChannel() != voiceState.getChannel()) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.error("Wrong Voice Channel",
                                EmbedUtils.WARNING + " I'm already playing in **" +
                                        selfVoiceState.getChannel().getName()
                                        + "**. Join that channel or use `!stop` first.")
                                .build())
                        .queue();
                return;
            }
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.warning("No Query Provided",
                            "Usage: `!play <song name or URL>`").build())
                    .queue();
            return;
        }

        String link = String.join(" ", args);
        boolean isUrl = isUrl(link);
        if (!isUrl) {
            link = "ytsearch:" + link;
        }

        PlayerManager.getInstance().loadAndPlay(voiceState.getChannel(), event.getChannel(), link);

        // A "searching…" embed — the PlayerManager will send a second embed when track
        // is loaded
        event.getChannel().sendMessageEmbeds(
                EmbedUtils.info(EmbedUtils.MUSIC_NOTE + "  Searching...",
                        EmbedUtils.NOTES + " Looking up: **" + String.join(" ", args) + "**").build())
                .queue();
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getHelp() {
        return "Plays a song from a link or search query";
    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
