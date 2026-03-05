package com.mybot.commands;

import com.mybot.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager extends ListenerAdapter {
    private final Map<String, ICommand> commands = new HashMap<>();
    private final Config config;

    public CommandManager(Config config) {
        this.config = config;
    }

    public void addCommand(ICommand command) {
        commands.put(command.getName(), command);
    }

    public List<ICommand> getCommands() {
        return new ArrayList<>(commands.values());
    }

    public ICommand getCommand(String name) {
        return commands.get(name);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        String message = event.getMessage().getContentRaw();
        if (!message.startsWith(config.getPrefix()))
            return;

        String[] split = message.replaceFirst(config.getPrefix(), "").split("\\s+");
        String commandName = split[0].toLowerCase();

        ICommand command = commands.get(commandName);
        if (command != null) {
            List<String> args = new ArrayList<>(Arrays.asList(split).subList(1, split.length));
            command.handle(event, args);
        }
    }
}
