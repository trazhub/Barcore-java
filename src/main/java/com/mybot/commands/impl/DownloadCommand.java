package com.mybot.commands.impl;

import com.mybot.Config;
import com.mybot.EmbedUtils;
import com.mybot.audio.PlayerManager;
import com.mybot.commands.ICommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DownloadCommand implements ICommand {
    private final Config config;

    public DownloadCommand(Config config) {
        this.config = config;
    }

    @Override
    public void handle(MessageReceivedEvent event, List<String> args) {
        String url;

        if (args.isEmpty()) {
            try {
                com.mybot.audio.GuildMusicManager musicManager = PlayerManager.getInstance()
                        .getGuildMusicManager(event.getGuild());
                com.sedmelluq.discord.lavaplayer.track.AudioTrack track = musicManager.player.getPlayingTrack();

                if (track == null) {
                    event.getChannel().sendMessageEmbeds(
                            EmbedUtils.error("Nothing Playing",
                                    "Provide a URL: `!download <url>`").build())
                            .queue();
                    return;
                }
                url = track.getInfo().uri;
            } catch (Exception e) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.error("Error", "Could not determine current track. Please provide a URL.").build())
                        .queue();
                return;
            }
        } else {
            url = args.get(0);
        }

        if (config.getCfAccountId() == null || config.getCfAccountId().startsWith("YOUR_")) {
            event.getChannel().sendMessageEmbeds(
                    EmbedUtils.error("Not Configured",
                            "Cloudflare R2 is not configured in `config.yml`.").build())
                    .queue();
            return;
        }

        final String finalUrl = url;

        // Downloading in-progress embed
        event.getChannel().sendMessageEmbeds(
                EmbedUtils.info(EmbedUtils.DOWNLOAD + "  Downloading...",
                        "Fetching: `" + finalUrl + "`\nThis may take a moment ⏳").build())
                .queue();

        CompletableFuture.runAsync(() -> {
            try {
                String downloadDir = "downloads";
                new File(downloadDir).mkdirs();

                String fileId = String.valueOf(System.currentTimeMillis());
                String outputPath = downloadDir + "/" + fileId + ".%(ext)s";

                ProcessBuilder pb = new ProcessBuilder(
                        "yt-dlp",
                        "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]",
                        "-o", outputPath,
                        "--no-playlist",
                        finalUrl);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    event.getChannel().sendMessageEmbeds(
                            EmbedUtils.error("Download Failed",
                                    "Failed to download with `yt-dlp`. Make sure it is installed on the host.").build())
                            .queue();
                    return;
                }

                File dir = new File(downloadDir);
                File[] files = dir.listFiles((d, name) -> name.startsWith(fileId));

                if (files == null || files.length == 0) {
                    event.getChannel().sendMessageEmbeds(
                            EmbedUtils.error("File Not Found", "Download succeeded but the file could not be located.")
                                    .build())
                            .queue();
                    return;
                }

                File file = files[0];

                // Uploading in-progress embed
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.info("☁️  Uploading to Cloud...",
                                "Uploading `" + file.getName() + "` to Cloudflare R2...").build())
                        .queue();

                String endpoint = "https://" + config.getCfAccountId() + ".r2.cloudflarestorage.com";

                try (S3Client s3 = S3Client.builder()
                        .endpointOverride(URI.create(endpoint))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getCfAccessKey(), config.getCfSecretKey())))
                        .build()) {

                    PutObjectRequest putOb = PutObjectRequest.builder()
                            .bucket(config.getCfBucket())
                            .key(file.getName())
                            .build();

                    s3.putObject(putOb, file.toPath());

                    event.getChannel().sendMessageEmbeds(
                            EmbedUtils.success("Upload Complete",
                                    EmbedUtils.DOWNLOAD + " Filename: `" + file.getName() + "`").build())
                            .queue();
                }

                file.delete();

            } catch (Exception e) {
                event.getChannel().sendMessageEmbeds(
                        EmbedUtils.error("Unexpected Error", e.getMessage()).build()).queue();
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getName() {
        return "download";
    }

    @Override
    public String getHelp() {
        return "Downloads a video and uploads it to cloud storage";
    }
}
