package yield.input.console;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import yield.test.Collector;

public class StdinTest {
	@Test
	public void simulateTyping() {
		InputStream oldStdin = System.in;
		try {
			String typing = "line 1\nother line";
			ByteArrayInputStream simulatedStream = new ByteArrayInputStream(
					typing.getBytes());
			System.setIn(simulatedStream);
			Collector<String> collector = new Collector<>();
			new Stdin().bind(collector);

			// Allow some time for the Stdin reading thread to get the values.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Assert.assertEquals("line 1", collector.get(0));
			Assert.assertEquals("other line", collector.get(1));
		} finally {
			System.setIn(oldStdin);
		}
	}
}
