package io.github.mooeypoo.chatmonitor.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import io.github.mooeypoo.chatmonitor.words.WordAction;

public class MessageHandler {
	private static final List<String> validPlaceholderKeys = List.of("player", "matchrule", "word");

	private MessageHandler() {
		// Utility class should never be constructed
	}

	/**
	 * Replace placeholders in the string with the values given in the map.
	 *
	 * @param message Original message
	 * @param placeholderMap A map representing the keys and their values
	 * @return Message with the placeholders replaced
	 */
	public static String replacePlaceholders(String message, Map<String, String> placeholderMap) {
		String response = message;
		for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
			if (!validPlaceholderKeys.contains(entry.getKey().toLowerCase())) {
				// Sanity check; if the key given isn't in the recognized keys, ignore it
				continue;
			}
			
			String replacer = "(?i)%" + entry.getKey() + "%"; // Replace case insensitive
			response = response.replaceAll(replacer, entry.getValue());
		}

		return response;
	}
	
	public static String replacePlaceholdersFromAction(String msgToReplace, Player player, WordAction action) {
		Map<String, String> valueMap = new HashMap<>();
		valueMap.put("player", player.getName());
		valueMap.put("matchrule", action.getMatchedRule());
		valueMap.put("word", action.getOriginalWord());
		
		return replacePlaceholders(msgToReplace, valueMap);
	}
	
	public static String createLogMessage(Player player, WordAction action, String originalMessage, String responseMessage) {
		List<String> logMessage = new ArrayList<>();
		logMessage.add("*MATCH TRIGGERED: " + action.getOriginalWord() + "[group: " + action.getGroup() + "]*");
		logMessage.add("\n-> Match rule: " + action.getMatchedRule());
		if (action.isPreventSend()) {
			logMessage.add(
				"\n-> MESSAGE MUTED." +
				"\n-> Attempted message: " + player.getName() + "> " + originalMessage
			);
		}
		if (!responseMessage.trim().isEmpty()) {
			if (action.isBroadcast()) {
				// Broadcast the response to everyone
				logMessage.add("\n-> Broadcast message> " + responseMessage);
			} else {
				// Send the response only to the relevant user
				logMessage.add("\n-> Sent to user> " + responseMessage);
			}
		}

		return String.join(" ", logMessage);
	}
}
