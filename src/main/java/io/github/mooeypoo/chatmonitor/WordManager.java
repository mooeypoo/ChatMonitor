package io.github.mooeypoo.chatmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class WordManager {
	private HashMap<String, ConfigAccessor> configs = new HashMap<String, ConfigAccessor>();
	private HashMap<String, String> wordmap = new HashMap<String, String>();
	private HashMap<String, ArrayList<String>> mapWordsInCommands = new HashMap<String, ArrayList<String>>();
	private ArrayList<String> allwords = new ArrayList<String>();
	private ArrayList<String> relevantCommands = new ArrayList<String>();
	private Configuration mainConfig;
	private String defaultMessage;
	private JavaPlugin plugin;
	
	public WordManager(JavaPlugin plugin, Configuration mainConfig) {
		this.plugin = plugin;
		this.mainConfig = mainConfig;
		this.defaultMessage = this.mainConfig.getString("defaultmessage");
		this.collectWords();
	}
	
	/**
	 * Reload the lists and re-process the groups from the config files.
	 */
	public void reload() {
		// Refresh all configs 
		for (Map.Entry<String, ConfigAccessor> entry : this.configs.entrySet()) {
			entry.getValue().reloadConfig();
		}
		
		// Reset lists
		this.wordmap.clear();
		this.allwords.clear();
		this.mapWordsInCommands.clear();
		this.relevantCommands.clear();
		
		// Redo word collection
		this.collectWords();
	}
	
	/**
	 * Process the given message to see if it triggers a matching word
	 * then grab the details of the word.
	 *
	 * @param chatMessage Given message
	 * @return Details of the matched word from any of the groups, or null if none was matched.
	 */
	public WordAction processAllWords(String chatMessage) {
		String matchedWord = this.getMatchedWord(chatMessage, this.allwords);

		if (matchedWord == null) {
			return null;
		}
		
		return this.getWordAction(matchedWord);
	}

	/**
	 * Process the given command to see if any of its text triggers a matching word
	 * then grab the details of the word.
	 *
	 * @param commandName The name of the command
	 * @param fullmessage Given message
	 * @return Details of the matched word from any of the groups, or null if none was matched.
	 */
	public WordAction processWordsInCommand(String commandName, String fullmessage) {
		ArrayList<String> wordListForThisCommand = this.mapWordsInCommands.get(commandName);

		String matchedWord = this.getMatchedWord(fullmessage, wordListForThisCommand);
		if (matchedWord == null) {
			return null;
		}

		return this.getWordAction(matchedWord);
	}
	
	/**
	 * Initialize the lists, collect all words and groups from the config files.
	 */
	private void collectWords() {
		// Go over the groups of words
		List<String> groups = this.mainConfig.getStringList("groups");
		if (groups == null) {
			this.plugin.getLogger().warning("Could not find word groups in configuration.");
			return;
		}
		for (String group : groups) {
			// Create config
			this.plugin.getLogger().info("Adding group config: " + group);
			ConfigAccessor conf = new ConfigAccessor(this.plugin, group, "words");
			this.configs.put(group, new ConfigAccessor(this.plugin, group, "words"));
			
			// Save config and defaults
			conf.getConfig().addDefault("mesage", this.defaultMessage);
			conf.saveConfig();
			
			// Collect all words from the config file
			for (String word : conf.getConfig().getStringList("words")) {
				// Save in full list
				this.allwords.add(word);
				
				// Save in the word map so we can find the group from the matched word
				this.wordmap.put(word, group);
				
				// Check if there are commands that this word should be tested against
				// and add those to the commands map
				List<String> includedCommands = conf.getConfig().getStringList("incluecommands");
				if (!includedCommands.isEmpty()) {
					for (String includedCmd : includedCommands) {
						ArrayList<String> listForThisCommand = this.mapWordsInCommands.get(includedCmd);
						if (listForThisCommand == null) {
							// Create a list for this command if one doesn't exist
							listForThisCommand = new ArrayList<String>();
							this.mapWordsInCommands.put(includedCmd, listForThisCommand);

							// Add the command to the list
							this.relevantCommands.add(includedCmd);
						}
						
						// Add the word
						listForThisCommand.add(word);
					}
				}
			}
		}
		
	}

	/**
	 * Produce a WordAction type response from a given word,
	 * based on details of its group and individual config.
	 *
	 * @param word Given matching word
	 * @return     Details about the matched word 
	 */
	private WordAction getWordAction(String word) {
		// Find the group this word is in
		String group = this.wordmap.get(word);
		if (group == null) {
			return null;
		}
		FileConfiguration config = this.configs.get(group).getConfig();
		
		if (config == null) {
			return null;
		}

		// From the group, get the message and commands
		return new WordAction(
			word,
			config.getString("message"),
			config.getBoolean("preventsend"),
			config.getBoolean("broadcast"),
			config.getStringList("runcommands"),
			group
		);
		
	}

	/**
	 * Check whether the test string has any matches from the given word list
	 * and return the matching word.
	 *
	 * @param testString Given string
	 * @param wordList A word list to test against
	 * @return Matching word, or null if none was matched.
	 */
	private String getMatchedWord(String testString, List<String> wordList) {
    	Matcher matcher;
		if (wordList == null || wordList.isEmpty()) {
			return null;
		}
    	// Check if the string has any of the words in the wordlist
		for (String word : wordList) {
    		Pattern pattern = Pattern.compile(word);
    		matcher = pattern.matcher(testString);
    		if (matcher.find()) {
    			return word;
    		}
		}
		
		return null;
		
	}
	
	/**
	 * Get a list of the commands that the plugin should
	 * know about. This is used to do a naive preliminary
	 * check about whether or not to even process incoming
	 * commands before checking the deeper details, or ignoring
	 * them if they are irrelevant.
	 *
	 * @return List of commands that have words that may match in a string
	 */
	public ArrayList<String> getRelevantCommands() {
		return this.relevantCommands;
	}
}
