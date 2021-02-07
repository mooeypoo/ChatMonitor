package io.github.mooeypoo.chatmonitor.commands;

import java.awt.Color;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.mooeypoo.chatmonitor.ChatMonitor;
import io.github.mooeypoo.chatmonitor.words.WordAction;


public class ChatMonitorCommandExecutor  implements CommandExecutor {
	private static String PREFIX = "[ChatMonitor] ";

	private ChatMonitor plugin;
	private HashMap<String, String> paramMap = new HashMap<String, String>();

	public ChatMonitorCommandExecutor(ChatMonitor plugin) {
		this.plugin = plugin;
		this.generateParameterMap();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("chatmonitor")) {
			return false;
		}

		if (args.length == 0 || args[0].equalsIgnoreCase("help") ) {
			this.outputHelp(sender);
			return true;
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("chatmonitor.cmd.reload")) {
				this.outputToPlayerAndConsole("You do not have permission to invoke the process action.", sender);
				return false;
			}

			this.outputToPlayerAndConsole("Reloading configuration files", sender);
			this.plugin.getWordManager().reload();
			this.outputToPlayerAndConsole("Reload complete.", sender);
			return true;
		} else if (args[0].equalsIgnoreCase("test")) {
			if (!sender.hasPermission("chatmonitor.cmd.test")) {
				this.outputToPlayerAndConsole("You do not have permission to invoke the process action.", sender);
				return false;
			}
			
			if (args.length == 1) {
				this.outputToPlayerAndConsole("Please provide the test text: /chatmonitor test [text to test]", sender);
				return false;
			}
			Boolean toPlayer = (sender instanceof Player);
		
			// Get the test text:
			String testString = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");
			this.outputToPlayerOrConsole("TESTING: '" + testString + "'", sender);
			// Send to wordmanager
			WordAction action = null;
			try {
				action = this.plugin.getWordManager().processAllWords(testString);
			} catch (Exception e) {
				this.outputToPlayerOrConsole("Error testing given text. Please see your console for more information.", sender);
				this.plugin.getLogger().info(e.getMessage());
				return false;
			}

			if (action == null) {
				// The tested string would not be caught
				this.outputToPlayerOrConsole(
					"RESULT: " + (toPlayer ? ChatColor.GREEN : "" ) + "PASS",
					sender
				);
				return true;
			}
			
			ChatColor isMutedColor = action.isPreventSend() ? ChatColor.RED : ChatColor.GREEN;
			String isMutedString = action.isPreventSend() ? "MUTED" : "NOT MUTED";
			// Something in that string was caught
			this.outputToPlayerOrConsole(
				String.format("%s %s",
						String.format(
							"-> %sCAUGHT: %s%s (GROUP: %s%s%s) ",
							(toPlayer ? ChatColor.RED : ""),
							action.getOriginalWord(),
							(toPlayer ? ChatColor.WHITE : ""),
							(toPlayer ? ChatColor.BLUE : ""),
							action.getGroup(),
							(toPlayer ? ChatColor.WHITE : "")
						),
						String.format(
							"%s%s",
							toPlayer ? isMutedColor : "",
							isMutedString
						)
				),
				sender
			);
			this.outputToPlayerOrConsole("-> MATCHED RULE: " + (toPlayer ? ChatColor.RED : "") + action.getMatchedRule(), sender);
			return true;
		}

		return false;
	}

	private void generateParameterMap() {
		this.paramMap.clear();
		this.paramMap.put("reload", "Reload all configuration files and word lists.");
		this.paramMap.put("test", "Tests a given string. Responds with whether it is caught by any of the lists.");
	}

	private void outputHelp(CommandSender sender) {
		String output = "";
		Boolean toPlayer = (sender instanceof Player);
		
		output = "Available actions for the /chatmonitor command:";
		
		if (sender instanceof Player) {
			// Send to the player, in-game
			sender.sendMessage(output);
		} else {
			this.plugin.getLogger().info(output);
		}

		this.outputToPlayerOrConsole("Commands list:", sender);

		for (String param : this.paramMap.keySet()) {
			output = String.format(
				toPlayer ? "* " + ChatColor.GREEN + "%s" + ChatColor.WHITE + ": %s" : "* %s: %s",
				param, this.paramMap.get(param)
			);
			this.outputToPlayerOrConsole(output, sender);
		}
		this.outputToPlayerOrConsole("Use the command /chatmonitor [action] to invoke any of the above actions.", sender);
	}
	private void output(String out, CommandSender sender, Boolean sendToBoth) {
		String formatColor = ChatColor.BLUE + PREFIX + ChatColor.WHITE + "%s";
		String formatBlank = PREFIX + "%s";
		Boolean inGame = sender instanceof Player;
		
		if (sendToBoth || !inGame) {
			this.plugin.getLogger().info(String.format(formatBlank, out));
		}
		
		if (inGame) {
			// Send to the player, in-game
			// Only do that if this was invoked from in-game
			sender.sendMessage(String.format(formatColor, out));
		}
		
	}

	private void outputToPlayerOrConsole(String out, CommandSender sender) {
		this.output(out, sender, false);
	}
	
	private void outputToPlayerAndConsole(String out, CommandSender sender) {
		this.output(out, sender, true);
	}
}
