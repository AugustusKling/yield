package yield.input.directory;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;

import org.junit.Assert;
import org.junit.Test;

import yield.config.TypedYielder;
import yield.config.function.Watch;
import yield.core.EventType;
import yield.core.Yielder;
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

	@Test(expected = IllegalArgumentException.class)
	public void readingFileInvalidConstruction() {
		Watch w = new Watch();
		w.getSource("", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void readingFileInvalidConstruction2() {
		Watch w = new Watch();
		w.getSource("skip=\"true\"", null);
	}

	@Test
	public void readingFileConstruction() throws IOException {
		Path tmp = Files.createTempFile(null, null);
		Files.write(tmp, "Line A".getBytes());
		Watch w = new Watch();
		TypedYielder ty = w.getSource("\"" + tmp.toAbsolutePath()
				+ "\" skip=\"false\" once=\"false\"", null);
		assertTrue(ty.type.isUsableAs(new EventType(String.class)));
		awaitPropagation();

		Collector<Object> lines = new Collector<>();
		@SuppressWarnings("unchecked")
		Yielder<Object> yielder = ty.yielder;
		yielder.bind(lines);
		Files.write(tmp, "Line B".getBytes(), StandardOpenOption.APPEND);

		awaitPropagation();
		Assert.assertEquals("Line B", lines.get(0));
	}

	private void awaitPropagation() {
		// Allow for a little delay to propagate the change.
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// nothing required.
		}
	}
}
