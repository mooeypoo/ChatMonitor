package io.github.mooeypoo.chatmonitor.commands;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.mooeypoo.chatmonitor.words.WordAction;
import io.github.mooeypoo.chatmonitor.words.WordManager;


public class ChatMonitorCommandExecutor implements CommandExecutor {
	private static final String PREFIX = "[ChatMonitor] ";

	private final Map<String, String> paramMap = Map.of(
			"reload", "Reload all configuration files and word lists.",
			"test", "Tests a given string. Responds with whether it is caught by any of the lists.");
	private final WordManager wordManager;
	private final Logger logger;

	public ChatMonitorCommandExecutor(WordManager wordManager, Logger logger) {
		this.wordManager = wordManager;
		this.logger = logger;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("chatmonitor")) {
			return false;
		}

		// TODO: move more of the exceptional logic to exceptions
		try {
			if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
				this.outputHelp(sender);
				return true;
			} else if (args[0].equalsIgnoreCase("reload")) {
				checkPermission("chatmonitor.cmd.reload", sender);

				this.outputToPlayerAndConsole("Reloading configuration files", sender);
				wordManager.reload();
				this.outputToPlayerAndConsole("Reload complete.", sender);
				return true;
			} else if (args[0].equalsIgnoreCase("test")) {
				checkPermission("chatmonitor.cmd.test", sender);
				checkArgument(args.length == 1, "Please provide the test text: /chatmonitor test [text to test]");

				boolean toPlayer = (sender instanceof Player);

				// Get the test text:
				String testString = StringUtils.join(ArrayUtils.subarray(args, 1, args.length), " ");
				this.outputToPlayerOrConsole("TESTING: '" + testString + "'", sender);
				// Send to wordmanager
				WordAction action = null;
				try {
					action = wordManager.processAllWords(testString);
				} catch (Exception e) {
					this.outputToPlayerOrConsole("Error testing given text. Please see your console for more information.", sender);
					logger.info(e.getMessage());
					return false;
				}

				if (action == null) {
					// The tested string would not be caught
					this.outputToPlayerOrConsole(
							"RESULT: " + (toPlayer ? ChatColor.GREEN : "") + "PASS",
							sender
					);
				} else {
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
				}
				return true;
			}
		} catch (NotAllowedException notAllowedException) {
			this.outputToPlayerAndConsole("You do not have permission to invoke the process action.", sender);
			return false;
		} catch (IllegalArgumentException illegalArgumentException) {
			this.outputToPlayerAndConsole(illegalArgumentException.getMessage(), sender);
			return false;
		}
		return false;
	}

	private void checkPermission(String permission, CommandSender sender) throws NotAllowedException {
		if (!sender.hasPermission(permission)) throw new NotAllowedException();
	}

	private void outputHelp(CommandSender sender) {
		boolean toPlayer = (sender instanceof Player);

		String header = "Available actions for the /chatmonitor command:";
		
		if (sender instanceof Player) {
			// Send to the player, in-game
			sender.sendMessage(header);
		} else {
			logger.info(header);
		}

		this.outputToPlayerOrConsole("Commands list:", sender);

		for (Map.Entry<String, String> param : this.paramMap.entrySet()) {
			String commandDescription = String.format(
				toPlayer ? "* " + ChatColor.GREEN + "%s" + ChatColor.WHITE + ": %s" : "* %s: %s",
				param.getKey(), param.getValue()
			);
			this.outputToPlayerOrConsole(commandDescription, sender);
		}
		this.outputToPlayerOrConsole("Use the command /chatmonitor [action] to invoke any of the above actions.", sender);
	}

	private void output(String out, CommandSender sender, boolean sendToBoth) {
		String formatColor = ChatColor.BLUE + PREFIX + ChatColor.WHITE + "%s";
		String formatBlank = PREFIX + "%s";
		boolean inGame = sender instanceof Player;
		
		if (sendToBoth || !inGame) {
			logger.info(String.format(formatBlank, out));
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
