package io.github.mooeypoo.chatmonitor.configs;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.ImmutableMap;

@ThreadSafe
public class ConfigManager {
    private final Path dataFolder;
	private final String prefix;
	private final ConfigLoader<PluginConfigInterface> mainConfig;
	private Map<String, GroupConfigInterface> configs = Map.of();

	public ConfigManager(Path dataFolder, String prefix) throws ConfigurationException {
        this.dataFolder = dataFolder;
        this.prefix = prefix;
		this.mainConfig = ConfigLoader.create(this.dataFolder, "config.yml", PluginConfigInterface.class);
        this.reload();
    }

	public void reload() throws ConfigurationException {
		this.mainConfig.reloadConfig();
		
		// Group configs
		ImmutableMap.Builder<String, GroupConfigInterface> configsBuilder = ImmutableMap.builder();
		for (String groupName : this.mainConfig.getConfigData().groups()) {
			ConfigLoader<GroupConfigInterface> groupConfig = ConfigLoader.create(
				dataFolder,
				this.prefix + "_" + groupName + ".yml",
				GroupConfigInterface.class
			);
			// TODO: try/catch
			groupConfig.reloadConfig();
			configsBuilder.put(groupName, groupConfig.getConfigData());
		}

		synchronized (this) {
			configs = configsBuilder.build();
		}

		// TODO: try/catch
		this.mainConfig.reloadConfig();
	}

	public synchronized Set<Map.Entry<String, GroupConfigInterface>> getConfigs() {
		return configs.entrySet();
	}

	@Nullable
	public synchronized GroupConfigInterface getGroupConfigData(String groupName) {
		return this.configs.get(groupName);
	}

}
