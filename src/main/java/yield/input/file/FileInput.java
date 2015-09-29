package yield.input.file;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import yield.core.EventQueue;
import yield.core.SourceProvider;
import yield.input.ListenerExceutionFailed;
import yield.input.ListenerExecutionAborted;

/**
 * Monitors a single file for changes. This is basically a tail operation.
 */
public class FileInput implements SourceProvider<String> {
	private EventQueue<String> queue = new EventQueue<>(String.class);

	private Path file;

	/**
	 * Creates stale object that needs activation via
	 * {@link #read(boolean, boolean, Charset)}.
	 * 
	 * @param file
	 *            File to watch which holds an event per line.
	 */
	public FileInput(Path file) {
		this.file = file;
	}

	/**
	 * Begins reading file and continuously watches for modifications yielding
	 * an event per line.
	 * 
	 * @param onePassOnly
	 *            No more events are yielded once the file has been fully
	 *            consumed.
	 * @param skipExisting
	 *            When {@code true} the first pass over the file is not yielded.
	 * @param encoding
	 *            Encoding of file contents.
	 */
	public void read(final boolean onePassOnly, final boolean skipExisting,
			final Charset encoding) {
		Thread reader = new Thread() {
			@Override
			public void run() {
				final FileMonitor m = new FileMonitor();
				try {
					m.watch(file, encoding, new FileModificationListener() {
						private boolean shallFeed = !skipExisting;

						@Override
						public void noSuchFile(Path path) {
							queue.getControlQueue().feed(new NoSuchFile(path));
						}

						@Override
						public void lineAdded(Path arg0, String lineContent) {
							if (shallFeed) {
								queue.feed(lineContent);
							}
						}

						@Override
						public void fileRotated(Path path) {
							queue.getControlQueue().feed(new FileRotated(path));
						}

						@Override
						public void completelyRead(Path path) {
							queue.getControlQueue().feed(
									new FileCompletelyRead(path));
							shallFeed = true;
							if (onePassOnly) {
								m.abortWatching();
							}
						}
					});
				} catch (ClosedByInterruptException e) {
					m.abortWatching();
					// Let listeners know that no further output is to be
					// expected.
					queue.getControlQueue()
							.feed(new ListenerExecutionAborted());
				} catch (IOException e) {
					queue.getControlQueue().feed(
							new ListenerExceutionFailed<Path>(file, e));
				}
			}
		};
		reader.start();
	}

	@Override
	public EventQueue<String> getQueue() {
		return queue;
	}

}
