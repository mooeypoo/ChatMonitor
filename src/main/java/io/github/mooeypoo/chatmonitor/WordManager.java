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
	private ArrayList<String> allwords = new ArrayList<String>();
	private Configuration mainConfig;
	private String defaultMessage;
	private JavaPlugin plugin;
	
	public WordManager(JavaPlugin plugin, Configuration mainConfig) {
		this.plugin = plugin;
		this.mainConfig = mainConfig;
		this.defaultMessage = this.mainConfig.getString("defaultmessage");
		this.collectWords();
	}
	
	public void reload() {
		// Refresh all configs 
		for (Map.Entry<String, ConfigAccessor> entry : this.configs.entrySet()) {
			entry.getValue().reloadConfig();
		}
		
		// Reset lists
		this.wordmap.clear();
		this.allwords.clear();
		
		// Redo word collection
		this.collectWords();
	}
	
	public WordAction process(String chatMessage) {
		String matchedWord = this.getMatchedWord(chatMessage);

		if (matchedWord == null) {
			return null;
		}
		
		// Find the group this word is in
		String group = this.wordmap.get(matchedWord);
		FileConfiguration config = this.configs.get(group).getConfig();
		// From the group, get the message and commands
		return new WordAction(
			matchedWord,
			config.getString("message"),
			config.getStringList("runcommands"),
			group
		);
	}

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
			for (String word : conf.getConfig().getStringList("badwords")) {
				// Save in full list
				this.allwords.add(word);
				
				// Save in the word map so we can find the group from the matched word
				this.wordmap.put(word, group);
			}
		}
		
	}
	
	private String getMatchedWord(String testString) {
    	Matcher matcher;
		if (this.allwords == null || this.allwords.isEmpty()) {
			return null;
		}
    	
    	// Check if the string has any of the words in the wordlist
		for (String word : this.allwords) {
    		Pattern pattern = Pattern.compile(word);
    		matcher = pattern.matcher(testString);
    		if (matcher.find()) {
    			return word;
    		}
		}
		
		return null;
	}
//	
//
}
