package io.github.mooeypoo.chatmonitor.configs;

import static java.util.Collections.emptySet;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class ConfigManager {
    private final Path dataFolder;
	private final String prefix;
	private ConfigLoader<PluginConfigInterface> mainConfig = null;
	private Map<String, ConfigLoader<GroupConfigInterface>> configs = new HashMap<>();

	public ConfigManager(Path dataFolder, String prefix) throws ConfigurationException {
        this.dataFolder = dataFolder;
        this.prefix = prefix;
        this.reload();
    }

	public void reload() throws ConfigurationException {
		// Main config
		if (this.mainConfig == null) {
			this.mainConfig = ConfigLoader.create(this.dataFolder, "config.yml", PluginConfigInterface.class);
		}
		this.mainConfig.reloadConfig();
		
		// Group configs
		this.configs.clear();
		for (String groupName : this.mainConfig.getConfigData().groups()) {
			ConfigLoader<GroupConfigInterface> groupConfig = ConfigLoader.create(
				dataFolder,
				this.prefix + "_" + groupName + ".yml",
				GroupConfigInterface.class
			);
			// TODO: try/catch
			groupConfig.reloadConfig();
			this.configs.put(groupName, groupConfig);
		}
		
		// TODO: try/catch
		this.mainConfig.reloadConfig();
	}
	
	public ConfigLoader<PluginConfigInterface> getMainConfig() {
		return this.mainConfig;
	}

	public Map<String, ConfigLoader<GroupConfigInterface>> getGroupConfigs() {
		return this.configs;
	}
	
	public Set<String> getGroupNames() {
		return this.configs.keySet();
	}

	@Nullable
	public GroupConfigInterface getGroupConfigData(String groupName) throws ConfigurationException {
		ConfigLoader<GroupConfigInterface> conf = this.configs.get(groupName);
		if (conf == null) {
			return null;
		}
		return conf.getConfigData();
	}
	
	public Set<String> getGroupWords(String groupName) throws ConfigurationException {
		GroupConfigInterface groupConfigData = this.getGroupConfigData(groupName);
		if (groupConfigData == null) {
			return emptySet();
		}
		
		return groupConfigData.words();
	}
}
