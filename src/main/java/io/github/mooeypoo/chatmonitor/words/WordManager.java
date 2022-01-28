package io.github.mooeypoo.chatmonitor.words;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import io.github.mooeypoo.chatmonitor.configs.ConfigManager;
import io.github.mooeypoo.chatmonitor.configs.ConfigurationException;
import io.github.mooeypoo.chatmonitor.configs.GroupConfigInterface;

@NotThreadSafe
public class WordManager {
	private final Logger logger;
	private WordLoader wordLoader;
	private ConfigManager configManager;

	@Nonnull private SetMultimap<String, String> mapWordsInCommands = ImmutableSetMultimap.of();
	@Nonnull private Map<String, String> wordMap = Map.of();

	// Used for testing
	public WordManager(Path dataFolder, String prefix, Logger logger) {
		this.logger = logger;
		try {
			configManager = new ConfigManager(dataFolder, prefix);
			wordLoader = new WordLoader(configManager, logger);
			setWords(wordLoader.collectWords());
		} catch (ConfigurationException e) {
			this.logger.warning("Initiation aborted for ChatMonitor. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
		}
	}

	/**
	 * Reload the lists and re-process the groups from the config files.
	 */
	public void reload() {
		try {
			// Refresh all configs
			this.configManager.reload();
			// Redo word collection
			setWords(wordLoader.collectWords());
		} catch (ConfigurationException e) {
			logger.warning("Reload loading default config. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
		}
	}

	private void setWords(@Nonnull Words words) {
		this.mapWordsInCommands = words.mapWordsInCommands();
		this.wordMap = words.wordmap();
	}

	/**
	 * Process the given message to see if it triggers a matching word
	 * then grab the details of the word.
	 *
	 * @param chatMessage Given message
	 * @return Details of the matched word from any of the groups, or null if none was matched.
	 * @throws Exception 
	 */
	@Nullable
	public WordAction processAllWords(String chatMessage) throws Exception {
		String[] matched = this.getMatchedWord(chatMessage, this.wordMap.keySet());

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
	@Nullable
	public WordAction processWordsInCommand(String commandName, String fullmessage) throws Exception {
		if (!this.mapWordsInCommands.containsKey(commandName)) {	
			return null;
		}

		Set<String> wordListForThisCommand = this.mapWordsInCommands.get(commandName);
		
		String[] matched = this.getMatchedWord(fullmessage, wordListForThisCommand);
		if (matched == null) {
			return null;
		}

		return this.getWordAction(matched);
	}

	/**
	 * Produce a WordAction type response from a given word,
	 * based on details of its group and individual config.
	 *
	 * @param matched An array 
	 * @return     Details about the matched word 
	 */
	@Nullable
	private WordAction getWordAction(String[] matched) {
		if (matched.length < 2) {
			return null;
		}
		String matchedRule = matched[0];
		String originalWord = matched[1];
		// Find the group this word is in
		String group = this.wordMap.get(matchedRule);
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
			logger.warning("Aborting generating action. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
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
	 */
	@Nullable
	private String[] getMatchedWord(String givenString, @Nonnull Set<String> wordList) throws Exception {
		// Transform to lowercase for the match test
		String testString = givenString.toLowerCase();

		// Check if the string has any of the words in the wordlist
		for (String rule : wordList) {
			try {
				// FIXME: compiling patterns is somewhat expensive and should be done only once, when loading the configuration
	    		Pattern pattern = Pattern.compile(rule);
				Matcher matcher = pattern.matcher(testString);
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
	@Nonnull
	public Set<String> getRelevantCommands() {
		return this.mapWordsInCommands.keySet();
	}
}
