package com.mybot;

import com.mybot.audio.GuildMusicManager;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.CommandManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import javax.annotation.Nonnull;

import static com.mybot.EmbedUtils.*;

/**
 * Handles button and select-menu interactions created by bot commands.
 */
public class InteractionListener extends ListenerAdapter {

    private final CommandManager commandManager;

    public InteractionListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    private static final float[] BASS_BOOST = {
            0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f
    };

    // ─── Button Interactions ────────────────────────────────────────────────────

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        GuildMusicManager mm;
        try {
            mm = PlayerManager.getInstance().getGuildMusicManager(guild);
        } catch (Exception e) {
            event.reply("❌ Could not access music player.").setEphemeral(true).queue();
            return;
        }

        switch (id) {
            case "np:skip" -> {
                mm.scheduler.nextTrack();
                event.reply(SKIP + " **Skipped!**").setEphemeral(true).queue();
            }
            case "np:pause" -> {
                if (mm.player.getPlayingTrack() == null) {
                    event.reply(CROSS + " Nothing is playing.").setEphemeral(true).queue();
                } else {
                    mm.player.setPaused(true);
                    event.reply(PAUSE + " **Paused.**").setEphemeral(true).queue();
                }
            }
            case "np:resume" -> {
                mm.player.setPaused(false);
                event.reply(PLAY + " **Resumed.**").setEphemeral(true).queue();
            }
            case "np:stop" -> {
                mm.scheduler.stop();
                guild.getAudioManager().closeAudioConnection();
                event.reply(STOP + " **Stopped and cleared the queue.**").setEphemeral(true).queue();
            }
            case "np:shuffle" -> {
                mm.scheduler.shuffle();
                event.reply(SHUFFLE + " **Queue shuffled!**").setEphemeral(true).queue();
            }
            case "np:repeat_toggle" -> {
                boolean nowRepeating = !mm.scheduler.isRepeating();
                mm.scheduler.setRepeating(nowRepeating);
                String msg = nowRepeating
                        ? REPEAT + " **Repeat enabled.**"
                        : REPEAT + " **Repeat disabled.**";
                event.reply(msg).setEphemeral(true).queue();
            }
            default -> event.reply("Unknown button.").setEphemeral(true).queue();
        }
    }

    // ─── Select Menu Interactions ───────────────────────────────────────────────

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        String id = event.getComponentId();
        Guild guild = event.getGuild();
        if (guild == null)
            return;

        if ("help_select".equals(id)) {
            String selected = event.getValues().get(0);
            ICommand cmd = commandManager.getCommand(selected);
            if (cmd == null) {
                event.reply("Command not found.").setEphemeral(true).queue();
                return;
            }
            event.replyEmbeds(
                    info(MUSIC_NOTE + "  `!" + cmd.getName() + "`",
                            cmd.getHelp()).build())
                    .setEphemeral(true).queue();
            return;
        }

        if ("filter_select".equals(id)) {
            String selected = event.getValues().get(0);
            GuildMusicManager mm;
            try {
                mm = PlayerManager.getInstance().getGuildMusicManager(guild);
            } catch (Exception e) {
                event.reply("❌ Could not access music player.").setEphemeral(true).queue();
                return;
            }

            if (mm.player.getPlayingTrack() == null) {
                event.replyEmbeds(error("No Track Playing",
                        "Start playing something before applying filters.").build())
                        .setEphemeral(true).queue();
                return;
            }

            switch (selected) {
                case "clear" -> {
                    mm.player.setFilterFactory(null);
                    event.replyEmbeds(success("Filter Cleared",
                            FILTER + " All audio filters have been removed.").build())
                            .setEphemeral(true).queue();
                }
                case "bassboost" -> {
                    EqualizerFactory eq = new EqualizerFactory();
                    for (int i = 0; i < BASS_BOOST.length; i++)
                        eq.setGain(i, BASS_BOOST[i]);
                    mm.player.setFilterFactory(eq);
                    event.replyEmbeds(success("Bass Boost Applied",
                            FILTER + " **Bass Boost** is now active. 🎸").build())
                            .setEphemeral(true).queue();
                }
                default -> event.reply("Unknown filter.").setEphemeral(true).queue();
            }
        }
    }
}
