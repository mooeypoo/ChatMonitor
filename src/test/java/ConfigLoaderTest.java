import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import io.github.mooeypoo.chatmonitor.configs.ConfigLoader;
import io.github.mooeypoo.chatmonitor.configs.ConfigurationException;
import io.github.mooeypoo.chatmonitor.configs.GroupConfigInterface;
import io.github.mooeypoo.chatmonitor.configs.PluginConfigInterface;

public class ConfigLoaderTest {
	@After
	public void tearDown() throws IOException {
		// Remove temp folder
		String path = Paths.get("src", "test", "resources", "temp").normalize().toString();
		System.out.println("Deleting contents of folder '" + path + "'");
		FileUtils.cleanDirectory(new File(path));
	}

	@Test
	public void testMalformedYaml() throws IOException {
		// Mock filesystem
		String fileName = "malformed.yml";
		Path path = Paths.get("src", "test", "resources", "temp", fileName);

		this.makeFileWithContent(path, "name: foo: bar: baz");
		ConfigLoader<PluginConfigInterface> config = ConfigLoader.create(
				Paths.get("src", "test", "resources", "temp"),
				fileName, PluginConfigInterface.class
		);
		
		try {
			config.reloadConfig();
			fail("Expected a ConfigurationException to be thrown");
		} catch (ConfigurationException e) {
			assertEquals(e.getConfigFileName(), "malformed.yml");
			assertEquals(e.getMessage(), "The yaml syntax of this file is malformed. Using defaults, instead.");
		}
		
		this.deleteFile(path);
	}

	@Test
	public void testBadConfigurationKeyvals() throws IOException {
		// Mock filesystem
		String fileName = "badconfig.yml";
		Path path = Paths.get("src", "test", "resources", "temp", fileName);

		this.makeFileWithContent(path, "preventsend: stringy");
		ConfigLoader<GroupConfigInterface> config = ConfigLoader.create(
				Paths.get("src", "test", "resources", "temp"),
				fileName, GroupConfigInterface.class
		);
		
		try {
			config.reloadConfig();
			fail("Expected a ConfigurationException to be thrown");
		} catch (ConfigurationException e) {
			assertEquals(e.getConfigFileName(), "badconfig.yml");
			assertEquals(e.getMessage(), "The keys and values used in this file are malformed. Using defaults, instead.");
		}
		
		this.deleteFile(path);
	}

	private void makeFileWithContent(Path path, String content) {
		try {
	    	String fullPath = path.normalize().toString();
	    	System.out.println("PATH --> " + fullPath);
			File newFile = new File(fullPath);
			if (newFile.createNewFile()) {
	          System.out.println("\n>> File created: " + newFile.getName());
	        } else {
	          System.out.println("\n>> File already exists.");
	        }
			if (newFile != null) {
				try (FileWriter writer = new FileWriter(newFile, UTF_8)) {
					writer.write(content);
				}
			}
		} catch (IOException e) {
		    System.out.println("An error occurred.");
		    e.printStackTrace();
		}
	}
	
	private void deleteFile(Path path) {
		String fullPath = path.normalize().toString();
    	System.out.println("DELETE PATH --> " + fullPath);
        File newFile = new File(fullPath);
        
        newFile.delete();
	}
}
