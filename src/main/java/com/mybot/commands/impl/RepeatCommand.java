package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class RepeatCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(event.getGuild());
        boolean nowRepeating = !musicManager.scheduler.isRepeating();
        musicManager.scheduler.setRepeating(nowRepeating);

        String desc = nowRepeating
                ? EmbedUtils.REPEAT + " The current track will now **loop**."
                : EmbedUtils.REPEAT + " Repeat has been **disabled**.";

        event.getChannel().sendMessageEmbeds(
                EmbedUtils.success("Repeat " + (nowRepeating ? "Enabled" : "Disabled"), desc).build()).queue();
    }

    @Override
    public String getName() {
        return "repeat";
    }

    @Override
    public String getHelp() {
        return "Toggles repeating the current song";
    }
}
