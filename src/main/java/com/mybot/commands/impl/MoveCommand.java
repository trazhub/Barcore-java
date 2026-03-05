package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class MoveCommand implements ICommand {

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        if (args.size() < 2) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.warning("Missing Arguments",
                            "Usage: `!move <from> <to>`\nExample: `!move 3 1` moves track 3 to position 1.").build())
                    .queue();
            return;
        }

        try {
            int from = Integer.parseInt(args.get(0)) - 1;
            int to = Integer.parseInt(args.get(1)) - 1;

            GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
            musicManager.scheduler.move(from, to);

            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.success("Track Moved",
                            EmbedUtils.MOVE + " Moved track from position **" + (from + 1)
                                    + "** to **" + (to + 1) + "**.")
                            .build())
                    .queue();
        } catch (NumberFormatException e) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Invalid Input", "Please provide valid position numbers.").build()).queue();
        }
    }

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public String getHelp() {
        return "Moves a track in the queue. Usage: !move <from> <to>";
    }
}
