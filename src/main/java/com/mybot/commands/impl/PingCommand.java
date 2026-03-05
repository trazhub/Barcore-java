package com.mybot.commands.impl;

import com.mybot.EmbedUtils;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class PingCommand implements ICommand {
    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        long gatewayPing = event.getJDA().getGatewayPing();
        event.getJDA().getRestPing().queue(restPing -> {
            String quality = restPing < 100 ? "🟢 Excellent" : restPing < 250 ? "🟡 Good" : "🔴 High";

            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(EmbedUtils.COLOR_GREEN)
                    .setTitle(EmbedUtils.PING + "  Pong!")
                    .addField("🌐 Gateway", gatewayPing + " ms", true)
                    .addField("📡 REST", restPing + " ms", true)
                    .addField("📶 Quality", quality, true)
                    .setFooter("BardCore Music Bot");

            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        });
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getHelp() {
        return "Checks the bot latency";
    }
}
