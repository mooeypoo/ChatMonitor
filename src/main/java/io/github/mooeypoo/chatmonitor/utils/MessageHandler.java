package io.github.mooeypoo.chatmonitor.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import io.github.mooeypoo.chatmonitor.words.WordAction;

public class MessageHandler {
	private static List<String> validPlaceholderKeys = Arrays.asList(new String[] {"player", "matchrule", "word"});
	
	public MessageHandler() {}

	/**
	 * Replace placeholders in the string with the values given in the map.
	 *
	 * @param message Original message
	 * @param placeholderMap A map representing the keys and their values
	 * @return Message with the placeholders replaced
	 */
	public static String replacePlaceholders(String message, HashMap<String, String> placeholderMap) {
		String response = message;
		for (String key : placeholderMap.keySet()) {
			if (!validPlaceholderKeys.contains(key.toLowerCase())) {
				// Sanity check; if the key given isn't in the recognized keys, ignore it
				continue;
			}
			
			String replacer = "(?i)%" + key + "%"; // Replace case insensitive
			response = response.replaceAll(replacer, placeholderMap.get(key));
		}

		return response;
	}
	
	public static String replacePlaceholdersFromAction(String msgToReplace, Player player, WordAction action) {
		HashMap<String, String> valueMap = new HashMap<String, String>();
		valueMap.put("player", player.getName());
		valueMap.put("matchrule", action.getMatchedRule());
		valueMap.put("word", action.getOriginalWord());
		
		return replacePlaceholders(msgToReplace, valueMap);
	}
	
	public static String createLogMessage(Player player, WordAction action, String originalMessage, String responseMessage) {
		ArrayList<String> logMessage = new ArrayList<String>();
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
