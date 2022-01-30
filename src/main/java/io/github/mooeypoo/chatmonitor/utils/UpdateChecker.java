package io.github.mooeypoo.chatmonitor.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class UpdateChecker {

    private JavaPlugin plugin;
    private int resourceId;

    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (
            		InputStream inputStream = new URL(
            			"https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId
            		).openStream();
            		Scanner scanner = new Scanner(inputStream, UTF_8)
            ) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                this.plugin.getLogger().info("Update validation failed: " + exception.getMessage());
            }
        });
    }
}