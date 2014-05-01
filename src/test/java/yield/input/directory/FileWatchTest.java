package yield.input.directory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;

import org.junit.Assert;
import org.junit.Test;

import yield.input.directory.DirectoryEvent;
import yield.input.directory.DirectoryWatcher;
import yield.output.Printer;
import yield.test.Collector;

public class FileWatchTest {
	@Test
	public void workflow() throws IOException {
		Path tmpDir = Files.createTempDirectory(null);
		DirectoryWatcher dirWatch = new DirectoryWatcher(tmpDir);

		Collector<DirectoryEvent> dirEvents = new Collector<>();
		dirWatch.getQueue().bind(dirEvents);

		dirWatch.getQueue().bind(new Printer<DirectoryEvent>("tmp>"));

		Path tmp1 = Files.createTempFile(tmpDir, null, "");
		awaitPropagation();
		Assert.assertEquals(new DirectoryEvent(
				StandardWatchEventKinds.ENTRY_CREATE, tmp1), dirEvents
				.getLast());

		Files.write(tmp1, "test".getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.APPEND);
		awaitPropagation();
		Assert.assertEquals(new DirectoryEvent(
				StandardWatchEventKinds.ENTRY_MODIFY, tmp1), dirEvents
				.getLast());
		Assert.assertEquals("test", new String(Files.readAllBytes(tmp1),
				StandardCharsets.UTF_8));

		Files.delete(tmp1);
		awaitPropagation();
		Assert.assertEquals(new DirectoryEvent(
				StandardWatchEventKinds.ENTRY_DELETE, tmp1), dirEvents
				.getLast());
	}

	private void awaitPropagation() {
		// Allow for a little delay to propagate the change.
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// nothing required.
		}
	}
}
