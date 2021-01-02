package io.github.mooeypoo.chatmonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatMonitor extends JavaPlugin implements Listener {
	private WordManager wordmanager;

	public ChatMonitor() {
		super();
	}

	protected ChatMonitor(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		super(loader, description, dataFolder, file);
	}

	@Override
	public void onEnable() {
		// Initialize word list and config
		this.getLogger().info("Initializing word lists...");
		this.wordmanager = new WordManager(this);

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
		WordAction action = null;
		try {
			action = this.wordmanager.processAllWords(msgFromPlayer);
		} catch (Exception e) {
			this.getLogger().info(e.getMessage());
			return;
		}

		if (action != null && !this.processResponse(action, p, msgFromPlayer)) {
			event.setCancelled(true);
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

		WordAction action = null;
		try {
			action = this.wordmanager.processWordsInCommand(cmdName, event.getMessage());
		} catch (Exception e) {
			this.getLogger().info(e.getMessage());
			return;
		}

		if (action != null && !this.processResponse(action, event.getPlayer(), event.getMessage())) {
			event.setCancelled(true);
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
	public Boolean processResponse(WordAction action, Player player, String msg) {
		if (action == null || action.isEmpty()) {
			return true;
		}

		Boolean shouldAllowEvent = !action.isPreventSend();
		String response = action.getMessage().replace("%player%", player.getName());
		response = response
				.replace("%matchrule%", action.getMatchedRule())
				.replace("%word%", action.getOriginalWord());

		ArrayList<String> logMessage = new ArrayList<String>();
		logMessage.add("*MATCH TRIGGERED: " + action.getOriginalWord() + "[type: " + action.getGroup() + "]*");
		logMessage.add("\n-> Match rule: " + action.getMatchedRule());
		if (action.isPreventSend()) {
			logMessage.add(
				"\n-> MESSAGE MUTED." +
				"\n-> Attempted message: " + player.getName() + "> " + msg
			);
			shouldAllowEvent = false;
		}

		if (!response.isEmpty()) {
			if (action.isBroadcast()) {
				// Broadcast the response to everyone
				Bukkit.broadcastMessage(ChatColor.RED + response);
				logMessage.add("\n-> Broadcast message> " + response);
			} else {
				// Send the response only to the relevant user
				player.sendMessage(ChatColor.RED + response);
				logMessage.add("\n-> Sent to user> " + response);
			}
		}
		this.getLogger().warning(String.join(" ", logMessage));
		
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
			// Replace magic words:
			String replacedCommand = cmd
				.replace("%player%", player.getName())
				.replace("%matchrule%", action.getMatchedRule())
				.replace("%word%", action.getOriginalWord());

			final String runnableCommand = replacedCommand;

			// Log and execute:
			this.getLogger().info("Invoking command: " + runnableCommand);
			try {
				getServer().getScheduler().callSyncMethod(this, new Callable<Boolean>() {
					public Boolean call() {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), runnableCommand);
						return false;
					}
				}).get();
			} catch (ExecutionException e) {
				this.getLogger().warning("ExecutionException for command \"" + cmd + "\"");
			} catch (InterruptedException e) {
				this.getLogger().warning("InterruptedException for command \"" + cmd + "\"");
			}
		}
	}
}
