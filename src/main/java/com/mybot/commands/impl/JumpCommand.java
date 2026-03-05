package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class JumpCommand implements ICommand {

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.warning("No Position Provided",
                            "Usage: `!jump <position>`\nSee `!queue` for track positions.").build())
                    .queue();
            return;
        }

        try {
            int index = Integer.parseInt(args.get(0)) - 1;

            GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
            musicManager.scheduler.jump(index);

            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.success("Jumped",
                            EmbedUtils.JUMP + " Jumped to track at position **" + (index + 1) + "**.").build())
                    .queue();
        } catch (NumberFormatException e) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Invalid Input", "Please provide a valid position number.").build()).queue();
        }
    }

    @Override
    public String getName() {
        return "jump";
    }

    @Override
    public String getHelp() {
        return "Jumps to a specific track in the queue. Usage: !jump <position>";
    }
}
