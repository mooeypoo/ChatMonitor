package io.github.mooeypoo.chatmonitor;

import java.util.concurrent.ExecutionException;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.mooeypoo.chatmonitor.commands.ChatMonitorCommandExecutor;
import io.github.mooeypoo.chatmonitor.utils.MessageHandler;
import io.github.mooeypoo.chatmonitor.utils.UpdateChecker;
import io.github.mooeypoo.chatmonitor.words.WordAction;
import io.github.mooeypoo.chatmonitor.words.WordManager;

public class ChatMonitor extends JavaPlugin implements Listener {
	private WordManager wordmanager;
	private int spigotResourceId = 87395;

	@Override
	public void onEnable() {
		new UpdateChecker(this, this.spigotResourceId).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
            	ComparableVersion remoteVersion = new ComparableVersion(version);
            	ComparableVersion localVersion = new ComparableVersion(this.getDescription().getVersion());
            	String message = null;
            	
            	if (localVersion.compareTo(remoteVersion) < 0) {
            		// Local version is lower than remote
            		message = String.format("[ChatMonitor] UPDATE ALERT. ChatMonitor v%s is published. Your server is currently using v%s. Please consider updating.",
	            			version,
	            			this.getDescription().getVersion()
	                	);
            	} else if (localVersion.compareTo(remoteVersion) > 0) {
            		// Local version is higher than remote; probably using SNAPSHOT or local test
            		message = String.format("[ChatMonitor] Version alert. You are using an unpublished version (v%s). Published (supported) version is v%s.",
	            			this.getDescription().getVersion(),
	            			version
	                	);
            	}
            	
            	if (message != null) {
                    this.getLogger().info(message);
            	}
            }
        });
		
		// Initialize word list and config
		this.getLogger().info("Initializing word lists...");
		this.wordmanager = new WordManager(this);

		// Initialize command
		this.getCommand("chatmonitor").setExecutor(new ChatMonitorCommandExecutor(this));

		// Connect events
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, (this));

		this.getLogger().info("ChatMonitor is enabled.");
	}

	@Override
	public void onDisable() {
		this.getLogger().info("ChatMonitor is disabled.");
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		if (p.hasPermission("chatmonitor.ignore")) {
			// Skip if the user's permission allows ignoring what they say
			return;
		}

		String msgFromPlayer = event.getMessage();
		try {
			WordAction action = this.wordmanager.processAllWords(msgFromPlayer);

			if (action != null && !this.processResponse(action, p, msgFromPlayer)) {
				event.setCancelled(true);
			}
		} catch (Exception e) {
			this.getLogger().info(e.getMessage());
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().hasPermission("chatmonitor.ignore")) {
			return;
		}
		// split up to get the elements of the command
		String[] cmdElements = event.getMessage().split(" ");
		String cmdName = cmdElements[0].substring(1);

		// See if we should even look at this for the command
		if (!this.wordmanager.getRelevantCommands().contains(cmdName)) {
			return;
		}

		try {
			WordAction action = this.wordmanager.processWordsInCommand(cmdName, event.getMessage());

			if (action != null && !this.processResponse(action, event.getPlayer(), event.getMessage())) {
				event.setCancelled(true);
			}
		} catch (Exception e) {
			this.getLogger().info("Could not process words. Action skipped: " + e.getMessage());
		}

	}

	/**
	 * Process the resulting action given to send the player a message, process the
	 * log message and activate commands.
	 *
	 * @param action The resulting WordAction object representing the details of the
	 *               word that matched.
	 * @param player The player triggering the text or command
	 * @param msg    The message sent by the user
	 * @return Whether or not the event should continue to process or be aborted
	 */
	public boolean processResponse(WordAction action, Player player, String msg) {
		if (action == null || action.isEmpty()) {
			return true;
		}

		boolean shouldAllowEvent = !action.isPreventSend();
		String response = MessageHandler.replacePlaceholdersFromAction(action.getMessage(), player, action);

		if (!response.isBlank()) {
			String colorResponse = ChatColor.translateAlternateColorCodes('&', response);

			if (action.isBroadcast()) {
				// Broadcast the response to everyone
				Bukkit.broadcastMessage(colorResponse);
			} else {
				// Send the response only to the relevant user
				player.sendMessage(colorResponse);
			}
		}
		this.getLogger().info(MessageHandler.createLogMessage(player, action, msg, response));
		
		// Run the commands for this match
		this.runCommands(player, action);
		return shouldAllowEvent;
	}

	/**
	 * Based on the word that matched, see if the group requires followup actions
	 * and trigger them on a deferred thread.
	 *
	 * @param player The player that evoked the original word match
	 * @param action The details of the matched word
	 */
	private void runCommands(Player player, WordAction action) {
		for (String cmd : action.getCommands()) {
			if (cmd == null || cmd.isBlank()) {
				continue;
			}

			// Replace magic words:
			final String runnableCommand = MessageHandler.replacePlaceholdersFromAction(cmd, player, action);

			// Log and execute:
			this.getLogger().info("Invoking command: " + runnableCommand);
			try {
				getServer().getScheduler().callSyncMethod(this, () -> {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), runnableCommand);
					return false;
				}).get();
			} catch (ExecutionException e) {
				this.getLogger().warning("ExecutionException for command \"" + cmd + "\"");
			} catch (InterruptedException e) {
				this.getLogger().warning("InterruptedException for command \"" + cmd + "\"");
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public WordManager getWordManager() {
		return this.wordmanager;
	}
}
