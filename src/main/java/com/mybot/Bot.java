package com.mybot;

import com.mybot.commands.CommandManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws Exception {
        Config config = new Config();

        if (config.getToken().equals("YOUR_BOT_TOKEN_HERE")) {
            log.error("Please set your bot token in your .env file or environment variables.");
            return;
        }

        CommandManager commandManager = new CommandManager(config);
        commandManager.addCommand(new com.mybot.commands.impl.PlayCommand());
        commandManager.addCommand(new com.mybot.commands.impl.SkipCommand());
        commandManager.addCommand(new com.mybot.commands.impl.StopCommand());
        commandManager.addCommand(new com.mybot.commands.impl.QueueCommand());
        commandManager.addCommand(new com.mybot.commands.impl.NowPlayingCommand());
        commandManager.addCommand(new com.mybot.commands.impl.PauseCommand());
        commandManager.addCommand(new com.mybot.commands.impl.ResumeCommand());
        commandManager.addCommand(new com.mybot.commands.impl.VolumeCommand());
        commandManager.addCommand(new com.mybot.commands.impl.RepeatCommand());
        commandManager.addCommand(new com.mybot.commands.impl.ShuffleCommand());
        commandManager.addCommand(new com.mybot.commands.impl.PingCommand());
        commandManager.addCommand(new com.mybot.commands.impl.DownloadCommand(config));
        commandManager.addCommand(new com.mybot.commands.impl.RemoveCommand());
        commandManager.addCommand(new com.mybot.commands.impl.SeekCommand());
        commandManager.addCommand(new com.mybot.commands.impl.FilterCommand());
        commandManager.addCommand(new com.mybot.commands.impl.LyricsCommand());
        commandManager.addCommand(new com.mybot.commands.impl.MoveCommand());
        commandManager.addCommand(new com.mybot.commands.impl.JumpCommand());
        commandManager.addCommand(new com.mybot.commands.impl.HelpCommand(commandManager));

        JDA jda = JDABuilder.createDefault(config.getToken())
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_VOICE_STATES)
                .setActivity(Activity.playing("Type " + config.getPrefix() + "help"))
                .addEventListeners(commandManager, new InteractionListener(commandManager))
                .build();

        // Clear any slash commands to remove the "Supports Commands" badge
        jda.updateCommands().queue();

        // Start Web Dashboard
        new WebDashboard(config, jda);
    }
}
