package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class VolumeCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());

        if (args.isEmpty()) {
            int volume = musicManager.player.getVolume();
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.info(EmbedUtils.VOL_UP + "  Current Volume",
                            EmbedUtils.volumeBar(volume)).build())
                    .queue();
            return;
        }

        try {
            int volume = Integer.parseInt(args.get(0));
            if (volume < 0 || volume > 100) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.error("Invalid Volume",
                                "Please provide a number between **0** and **100**.").build())
                        .queue();
                return;
            }
            musicManager.player.setVolume(volume);
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.success("Volume Updated",
                            EmbedUtils.VOL_UP + " " + EmbedUtils.volumeBar(volume)).build())
                    .queue();
        } catch (NumberFormatException e) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Invalid Input",
                            "Please provide a valid number between 0 and 100.").build())
                    .queue();
        }
    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getHelp() {
        return "Gets or sets the volume (0–100)";
    }
}
