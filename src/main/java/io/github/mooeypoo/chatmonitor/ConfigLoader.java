package io.github.mooeypoo.chatmonitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import space.arim.dazzleconf.ConfigurationFactory;
import space.arim.dazzleconf.ConfigurationOptions;
import space.arim.dazzleconf.error.ConfigFormatSyntaxException;
import space.arim.dazzleconf.error.InvalidConfigException;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlConfigurationFactory;
import space.arim.dazzleconf.ext.snakeyaml.SnakeYamlOptions;
import space.arim.dazzleconf.helper.ConfigurationHelper;

public class ConfigLoader<C> extends ConfigurationHelper<C> {
	private volatile C configData;
	private String fileName = null;

	private ConfigLoader(Path configFolder, String fileName, ConfigurationFactory<C> factory) {
		super(configFolder, fileName, factory);
		this.fileName = fileName;
	}

	public static <C> ConfigLoader<C> create(Path configFolder, String fileName, Class<C> configClass) {
		// SnakeYaml example
		SnakeYamlOptions yamlOptions = new SnakeYamlOptions.Builder()
				.useCommentingWriter(true) // Enables writing YAML comments
				.build();
		return new ConfigLoader<>(configFolder, fileName,
				new SnakeYamlConfigurationFactory<>(configClass, ConfigurationOptions.defaults(), yamlOptions));
	}

	public void reloadConfig() {
		try {
			configData = reloadConfigData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);

		} catch (ConfigFormatSyntaxException ex) {
			configData = getFactory().loadDefaults();
			System.err.println("Uh-oh! The syntax of your configuration ('" + this.fileName + "') are invalid. "
					+ "Check your YAML syntax with a tool such as https://yaml-online-parser.appspot.com/");
			ex.printStackTrace();

		} catch (InvalidConfigException ex) {
			configData = getFactory().loadDefaults();
			System.err.println("Uh-oh! The values in your configuration ('" + this.fileName + "') are invalid. "
					+ "Check to make sure you have specified the right data types.");
			ex.printStackTrace();
		}
	}

	public C getConfigData() {
		C configData = this.configData;
		if (configData == null) {
			throw new IllegalStateException("Configuration ('" + this.fileName + "') has not been loaded yet");
		}
		return configData;
	}
}
