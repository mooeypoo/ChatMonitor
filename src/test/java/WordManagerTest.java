import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.List;

import static java.util.Arrays.asList;

import org.junit.Test;

import io.github.mooeypoo.chatmonitor.WordAction;
import io.github.mooeypoo.chatmonitor.WordManager;

public class WordManagerTest {
	@Test
	public void testValidAndEmptyWordMatches() throws Exception {
		WordManager wordManager = new WordManager(
			Paths.get("src","test","resources", "validrules"), "test_"
		);
		WordAction action = null;

		action = wordManager.processAllWords("there is somebadw0rd in here.");
		assertEquals(action.getOriginalWord(), "badw0rd");
		assertEquals(action.getMatchedRule(), "badw[0o]rd");

		action = wordManager.processAllWords("this is a w0rd in a sentence");
		assertEquals(action.getOriginalWord(), "w0rd");
		assertEquals(action.getMatchedRule(), "\\bw0rd\\b");

		action = wordManager.processAllWords("There are no matches here.");
		assertNull(action);
	}

	@Test
	public void testEmptyLists() throws Exception {
		WordManager wordManager = new WordManager(
				Paths.get("src","test","resources", "emptylist"), "test_"
			);
		WordAction action = null;
		
		action = wordManager.processAllWords("this should be skipped gracefully since there are no words in this list");
		assertNull(action);
	}

	@Test
	public void testInvalidRules() {
		WordManager wordManager = new WordManager(
			Paths.get("src","test","resources", "invalidrule"), "test_"
		);
		
		try {
			wordManager.processAllWords("There is a validword match here from a problematic invalid rule");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Error: Could not process rule ((invalid)");
		}
	}
	
	@Test
	public void testMatchesInCommands() throws Exception {
		WordManager wordManager = new WordManager(
			Paths.get("src","test","resources", "commands"), "test_"
		);
		WordAction action = null;

		action = wordManager.processWordsInCommand("tell", "This badw0rd should be found.");
		assertNotNull(action);
		assertEquals(action.getOriginalWord(), "badw0rd");
		assertEquals(action.getMatchedRule(), "badw[0o]rd");

		action = wordManager.processWordsInCommand("me", "This badw0rd should not be found because the group doesn't specify that command.");
		assertNull(action);

		action = wordManager.processWordsInCommand("tell", "There is no match in the words even though the command itself is evaluated.");
		assertNull(action);

		action = wordManager.processWordsInCommand("me", "the word justme should match for that command");
		assertNotNull(action);
		action = wordManager.processWordsInCommand("tell", "the word justme should NOT match for that command");
		assertNull(action);
		
		// Check that wordManager.getRelevantCommands() has all commands that have words in them
		List<String> expectedRelevantCommands = asList("me", "tell");
		assertTrue(
			wordManager.getRelevantCommands().containsAll(expectedRelevantCommands) &&
			expectedRelevantCommands.containsAll(wordManager.getRelevantCommands())
		);
	}
}

