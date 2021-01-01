import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.github.mooeypoo.chatmonitor.ChatMonitor;
import io.github.mooeypoo.chatmonitor.WordAction;
import io.github.mooeypoo.chatmonitor.WordManager;

public class WordManagerTest {
	private ServerMock server;
	private ChatMonitor plugin;
	private WordManager wordmanager;

	@Before
	public void setUp()
	{
	    this.server = MockBukkit.mock();
	    this.plugin = (ChatMonitor) MockBukkit.load(ChatMonitor.class);
		this.wordmanager = new WordManager(this.plugin.getLogger(), Paths.get("src","test","resources"), "test_");
	}

	@After
	public void tearDown()
	{
	    MockBukkit.unmock();
	}

	@Test
	public void test() {
		WordAction action = null;

		action = this.wordmanager.processAllWords("there is somebadw0rd in here.");
		assertEquals(action.getOriginalWord(), "badw0rd");
		assertEquals(action.getMatchedRule(), "badw[0o]rd");

		action = this.wordmanager.processAllWords("this is a w0rd in a sentence");
		assertEquals(action.getOriginalWord(), "w0rd");
		assertEquals(action.getMatchedRule(), "\\bw0rd\\b");
}

}
