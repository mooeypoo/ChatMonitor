package io.github.mooeypoo.chatmonitor.words;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.bukkit.plugin.java.JavaPlugin;

import io.github.mooeypoo.chatmonitor.configs.ConfigManager;
import io.github.mooeypoo.chatmonitor.configs.ConfigurationException;
import io.github.mooeypoo.chatmonitor.configs.GroupConfigInterface;

@NotThreadSafe // HashMap and ArrayLists are not thread safe and their access isn't protected by synchronization.
public class WordManager {
	private HashMap<String, String> wordmap = new HashMap<>();

	// TODO: this Set should probably be a Guava MultiMap, probably an
	//  	ImmutableSetMultiMap:
	//  	https://guava.dev/releases/snapshot-jre/api/docs/com/google/common/collect/ImmutableSetMultimap.html
	private HashMap<String, ArrayList<String>> mapWordsInCommands = new HashMap<>();

	// FIXME: this seems to use the semantics of a Set, not of a List
	private ArrayList<String> allwords = new ArrayList<>();

	// FIXME: this seems to use the semantics of a Set, not of a List
	private ArrayList<String> relevantCommands = new ArrayList<>();

	// FIXME: Plugin isn't actually used by this class outside of construction.
	//  	A reference to Logger should be kept instead.
	@Nullable private JavaPlugin plugin;
	private ConfigManager configManager;
	
	public WordManager(JavaPlugin plugin) {
		this.plugin = plugin;
		try {
			this.configManager = new ConfigManager(Paths.get(this.plugin.getDataFolder().getPath()), "ChatMonitor_wordgroup");
		} catch (ConfigurationException e) {
			this.plugin.getLogger().warning("Initiation aborted for ChatMonitor. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
			return;
		}
		this.collectWords();
	}
	
	// Used for testing
	public WordManager(Path filepath, String prefix) {
		try {
			this.configManager = new ConfigManager(filepath, prefix);
		} catch (ConfigurationException e) {
			// FIXME: plugin is null in this case
			this.plugin.getLogger().warning("Initiation aborted for ChatMonitor. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
			return;
		}
		this.collectWords();
	}
	/**
	 * Reload the lists and re-process the groups from the config files.
	 */
	public void reload() {
		// Reset lists
		this.wordmap.clear();
		this.allwords.clear();
		this.mapWordsInCommands.clear();
		this.relevantCommands.clear();
		
		// Refresh all configs 
		try {
			this.configManager.reload();
		} catch (ConfigurationException e) {
			this.plugin.getLogger().warning("Reload loading default config. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
		}

		// Redo word collection
		this.collectWords();
	}
	
	/**
	 * Process the given message to see if it triggers a matching word
	 * then grab the details of the word.
	 *
	 * @param chatMessage Given message
	 * @return Details of the matched word from any of the groups, or null if none was matched.
	 * @throws Exception 
	 */
	public WordAction processAllWords(String chatMessage) throws Exception {
		String[] matched = this.getMatchedWord(chatMessage, this.allwords);

		if (matched == null) {
			return null;
		}
		
		return this.getWordAction(matched);
	}

	/**
	 * Process the given command to see if any of its text triggers a matching word
	 * then grab the details of the word.
	 *
	 * @param commandName The name of the command
	 * @param fullmessage Given message
	 * @return Details of the matched word from any of the groups, or null if none was matched.
	 * @throws Exception 
	 */
	public WordAction processWordsInCommand(String commandName, String fullmessage) throws Exception {
		if (!this.mapWordsInCommands.containsKey(commandName)) {	
			return null;
		}

		ArrayList<String> wordListForThisCommand = this.mapWordsInCommands.get(commandName);
		
		String[] matched = this.getMatchedWord(fullmessage, wordListForThisCommand);
		if (matched == null) {
			return null;
		}

		return this.getWordAction(matched);
	}
	
	/**
	 * Initialize the lists, collect all words and groups from the config files.
	 */
	private void collectWords() {
		// Go over the groups of words
		Set<String> groups = this.configManager.getGroupNames();

		for (String groupName : groups) {
			GroupConfigInterface groupConfig = null;
			try {
				groupConfig = this.configManager.getGroupConfigData(groupName);
			} catch (ConfigurationException e) {
				this.plugin.getLogger().warning("Word group loading defaults. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
			}
			// Collect all words from the config group
			this.allwords.addAll(groupConfig.words());

			for (String word : groupConfig.words()) {
				// Save in the word map so we can find the group from the matched word
				this.wordmap.put(word, groupName);
				
				// Check if there are commands that this word should be tested against
				// and add those to the commands map
				this.collectCommandMap(word, groupConfig.includeCommands());
			}
	
		}
	}
	
	/**
	 * Collect the relevant commands per word given, based on the group configuration
	 * Create a map where command names are keys, and the values are a list of all words
	 * that are included in the groups that include this command.
	 *
	 * @param word Given word match
	 * @param commandsInGroup A set of the commands in the group
	 */
	private void collectCommandMap(String word, Set<String> commandsInGroup) {
		for (String includedCmd : commandsInGroup) {
			if (includedCmd == null || includedCmd.isBlank()) {
				// Skip empty lines
				continue;
			}
			// For each command, get the existing list first
			ArrayList<String> wordListForThisCommand = mapWordsInCommands.computeIfAbsent(includedCmd, s -> new ArrayList<>());
			
			// Add the word into the command map, if the word doesn't already exist in it
			if (!wordListForThisCommand.contains(word)) {
				wordListForThisCommand.add(word);
			}

		}

	}

	/**
	 * Produce a WordAction type response from a given word,
	 * based on details of its group and individual config.
	 *
	 * @param matched An array 
	 * @return     Details about the matched word 
	 */
	private WordAction getWordAction(String[] matched) {
		if (matched.length < 2) {
			return null;
		}
		String matchedRule = matched[0];
		String originalWord = matched[1];
		// Find the group this word is in
		String group = this.wordmap.get(matchedRule);
		if (group == null) {
			return null;
		}

		try {
			GroupConfigInterface config = this.configManager.getGroupConfigData(group);

			if (config == null) {
				return null;
			}

			// From the group, get the message and commands
			return new WordAction(
					matchedRule,
					originalWord,
					config.message(),
					config.preventSend(),
					config.broadcast(),
					config.runCommands(),
					group
			);
		} catch (ConfigurationException e) {
			this.plugin.getLogger().warning("Aborting generating action. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
			return null;
		}
	}

	/**
	 * Check whether the test string has any matches from the given word list
	 * and return the matching word.
	 *
	 * @param givenString Given string
	 * @param wordList A word list to test against
	 * @return An array that contains the matching term and original word that was matched,
	 *  or null if none was matched.
	 * @throws Exception 
	 */
	private String[] getMatchedWord(String givenString, List<String> wordList) throws Exception {
    	Matcher matcher;
		if (wordList == null || wordList.isEmpty()) {
			return null;
		}
		
		// Transform to owercase for the match test
		String testString = givenString.toLowerCase();
    	// Check if the string has any of the words in the wordlist
		for (String rule : wordList) {
			try {
	    		Pattern pattern = Pattern.compile(rule);
	    		matcher = pattern.matcher(testString);
	    		if (matcher.find()) {
	    			return new String[] { rule, matcher.group() };
	    		}
			} catch (PatternSyntaxException e) {
				throw new Exception("Error: Could not process rule (" + rule + ")");
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
	public Set<String> getRelevantCommands() {
		return this.mapWordsInCommands.keySet();
	}
}
