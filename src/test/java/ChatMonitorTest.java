import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.github.mooeypoo.chatmonitor.ChatMonitor;

public class ChatMonitorTest {
	private ServerMock server;
	private ChatMonitor plugin;

	@Before
	public void setUp()
	{
	    server = MockBukkit.mock();
	    plugin = (ChatMonitor) MockBukkit.load(ChatMonitor.class);
	}

	@After
	public void tearDown()
	{
	    MockBukkit.unmock();
	}

//	@Test
//	public void test() {
//		fail("Not yet implemented");
//	}

}
