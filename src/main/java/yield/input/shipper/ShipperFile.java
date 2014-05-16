package yield.input.shipper;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import shipper.FileModificationListener;
import shipper.FileMonitor;
import yield.core.EventQueue;
import yield.core.SourceProvider;

/**
 * Monitors a single file for changes. This is basically a tail operation.
 */
public class ShipperFile implements SourceProvider<String> {
	private EventQueue<String> queue = new EventQueue<>();

	/**
	 * Creates stale object that needs activation via {@link #readOnce(Path)}.
	 */
	public ShipperFile() {
	}

	/**
	 * Instantly begins reading file and continuously watches for modifications.
	 * 
	 * @param file
	 *            Path to watch.
	 */
	public ShipperFile(final Path file) {
		Thread reader = new Thread() {
			@Override
			public void run() {
				try {
					final FileMonitor m = new FileMonitor();
					m.watch(file, Charset.forName("UTF-8"),
							new FileModificationListener() {

								@Override
								public void noSuchFile(Path arg0) {
								}

								@Override
								public void lineAdded(Path arg0,
										String lineContent) {
									queue.feed(lineContent);
								}

								@Override
								public void fileRotated(Path arg0) {
								}

								@Override
								public void completelyRead(Path arg0) {
								}
							});
				} catch (ClosedByInterruptException e) {

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		reader.start();
	}

	/**
	 * Read a file yielding an event per line. No more events are yielded once
	 * the file has been fully consumed.
	 * 
	 * @param file
	 *            File which holds an event per line.
	 */
	public void readOnce(final Path file) {
		Thread reader = new Thread() {
			@Override
			public void run() {
				try {
					final FileMonitor m = new FileMonitor();
					m.watch(file, Charset.forName("UTF-8"),
							new FileModificationListener() {

								@Override
								public void noSuchFile(Path arg0) {
								}

								@Override
								public void lineAdded(Path arg0,
										String lineContent) {
									queue.feed(lineContent);
								}

								@Override
								public void fileRotated(Path arg0) {
								}

								@Override
								public void completelyRead(Path arg0) {
									m.abortWatching();
								}
							});
				} catch (ClosedByInterruptException e) {

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		reader.start();
		boolean reading = true;
		while (reading) {
			try {
				reader.join();
				reading = false;
			} catch (InterruptedException e) {
				// try again.
			}
		}
	}

	@Override
	public EventQueue<String> getQueue() {
		return queue;
	}

}
