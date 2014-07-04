package yield.input.shipper;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import shipper.FileModificationListener;
import shipper.FileMonitor;
import yield.core.EventQueue;
import yield.core.SourceProvider;
import yield.input.ListenerExceutionFailed;
import yield.input.ListenerExecutionAborted;

/**
 * Monitors a single file for changes. This is basically a tail operation.
 */
public class ShipperFile implements SourceProvider<String> {
	private EventQueue<String> queue = new EventQueue<>();
	private Path file;

	/**
	 * Creates stale object that needs activation via {@link #read(Path)}.
	 * 
	 * @param file
	 *            File to watch which holds an event per line.
	 */
	public ShipperFile(Path file) {
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
	 */
	public void read(final boolean onePassOnly, final boolean skipExisting) {
		Thread reader = new Thread() {
			@Override
			public void run() {
				final FileMonitor m = new FileMonitor();
				try {
					m.watch(file, Charset.forName("UTF-8"),
							new FileModificationListener() {
								private boolean shallFeed = !skipExisting;

								@Override
								public void noSuchFile(Path path) {
									queue.getControlQueue().feed(
											new NoSuchFile(path));
								}

								@Override
								public void lineAdded(Path arg0,
										String lineContent) {
									if (shallFeed) {
										queue.feed(lineContent);
									}
								}

								@Override
								public void fileRotated(Path path) {
									queue.getControlQueue().feed(
											new FileRotated(path));
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
