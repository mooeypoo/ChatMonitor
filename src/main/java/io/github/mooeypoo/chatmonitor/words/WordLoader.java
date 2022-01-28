package io.github.mooeypoo.chatmonitor.words;

import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import io.github.mooeypoo.chatmonitor.configs.ConfigManager;
import io.github.mooeypoo.chatmonitor.configs.ConfigurationException;
import io.github.mooeypoo.chatmonitor.configs.GroupConfigInterface;

@ThreadSafe
public class WordLoader {
    private final ConfigManager configManager;
    private final Logger logger;

    public WordLoader(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    /**
     * Initialize the lists, collect all words and groups from the config files.
     */
    public Words collectWords() {
        ImmutableMap.Builder<String, String> wordmap = ImmutableMap.builder();
        SetMultimap<String, String> mapWordsInCommands = MultimapBuilder.hashKeys().hashSetValues().build();

        // Go over the groups of words
        Set<String> groups = configManager.getGroupNames();

        for (String groupName : groups) {
            try {
                // FIXME: groupConfig can be null
                GroupConfigInterface groupConfig = configManager.getGroupConfigData(groupName);

                for (String word : groupConfig.words()) {
                    // Save in the word map, so we can find the group from the matched word
                    // FIXME: Can't we have the same word in multiple group names? In other words, should this also be a SetMultiMap?
                    //        Is `wordmap == mapWordsInCommands.inverse() ?
                    wordmap.put(word, groupName);

                    // Check if there are commands that this word should be tested against
                    // and add those to the commands map
                    groupConfig.includeCommands().stream()
                            .filter(cmd -> cmd != null && !cmd.isBlank())
                            .forEach(includeCmd -> mapWordsInCommands.get(includeCmd).add(word));
                }
            } catch (ConfigurationException e) {
                logger.warning("Word group loading defaults. Error in configuration file '" + e.getConfigFileName() + "': " + e.getMessage());
            }
        }

        ImmutableSetMultimap<String, String> immutableWordsInCommands = ImmutableSetMultimap.copyOf(mapWordsInCommands);
        return new Words(wordmap.build(), immutableWordsInCommands);
    }
}
