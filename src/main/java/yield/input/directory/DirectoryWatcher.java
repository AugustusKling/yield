package yield.input.directory;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import yield.core.EventQueue;
import yield.core.EventSource;
import yield.core.Main;
import yield.core.SourceProvider;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Monitors a directory for changes.
 */
public class DirectoryWatcher implements SourceProvider<DirectoryEvent> {

	private static class KeyLoop extends Thread implements
			SourceProvider<DirectoryEvent> {
		private final EventQueue<DirectoryEvent> queue = new EventQueue<>();

		private Path path;
		private WatchService watcher;

		public KeyLoop(Path path) throws IOException {
			setDaemon(true);
			this.path = path;

			watcher = FileSystems.getDefault().newWatchService();
			path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		}

		@Override
		public void run() {
			while (true) {

				// wait for key to be signaled
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					// This key is registered only
					// for ENTRY_CREATE events,
					// but an OVERFLOW event can
					// occur regardless if events
					// are lost or discarded.
					if (kind == OVERFLOW) {
						continue;
					}

					// The filename is the
					// context of the event.
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();

					Path child = path.resolve(filename);

					@SuppressWarnings("unchecked")
					WatchEvent.Kind<Path> standardWatchEvent = (WatchEvent.Kind<Path>) kind;
					DirectoryEvent directoryEvent = new DirectoryEvent(
							standardWatchEvent, child);
					queue.feed(directoryEvent);
				}

				// Reset the key -- this step is critical if you want to
				// receive further watch events. If the key is no longer valid,
				// the directory is inaccessible so exit the loop.
				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}
		}

		@Override
		public EventQueue<DirectoryEvent> getQueue() {
			return queue;
		}
	}

	private KeyLoop keyLoop;

	public DirectoryWatcher(Path path) throws IOException {
		startWatchThread(path);
	}

	public DirectoryWatcher(Main main, ObjectNode config) throws IOException {
		Path path = Paths.get(config.get("path").textValue());

		startWatchThread(path);
	}

	private void startWatchThread(Path path) throws IOException {
		keyLoop = new KeyLoop(path);
		keyLoop.start();
	}

	@Override
	public EventSource<DirectoryEvent> getQueue() {
		return keyLoop.getQueue();
	}

}
