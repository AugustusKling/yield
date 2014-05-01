package yield.input.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import yield.core.EventSource;
import yield.core.Main;
import yield.core.event.FailureEvent;
import yield.core.event.MetaEvent;
import yield.core.event.SuccessEvent;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Continuously requests data from an URL and yields download meta data.
 */
public class Poller extends EventSource<MetaEvent<FileTransfer>> {
	private ScheduledFuture<?> pollingTask;

	public Poller(Main main, ObjectNode config) throws IOException {
		startPolling(new URL(config.get("website").textValue()),
				config.get("interval").longValue());
	}

	public Poller(URL website, long pollingPeriod) throws IOException {
		startPolling(website, pollingPeriod);
	}

	private void startPolling(URL website, long pollingPeriod) {
		pollingTask = Executors.newSingleThreadScheduledExecutor()
				.scheduleAtFixedRate(new Retriever(website, this), 0,
						pollingPeriod, TimeUnit.MILLISECONDS);
	}

	public void abortPolling() {
		this.pollingTask.cancel(true);
	}

	private static class Retriever implements Runnable {
		private static final int TIMEOUT = 1000;
		private URL website;
		private Poller poller;

		public Retriever(URL website, Poller poller) {
			this.website = website;
			this.poller = poller;
		}

		@Override
		public void run() {
			FileTransfer transfer = new FileTransfer(website);

			long startTime = System.nanoTime();
			URLConnection connection;
			try {
				connection = website.openConnection();
				connection.setConnectTimeout(TIMEOUT);
				transfer.setResponseHeaders(connection.getHeaderFields());

				ReadableByteChannel rbc = Channels.newChannel(connection
						.getInputStream());
				try (OutputStream content = new ByteArrayOutputStream(
						100 * 1024);) {
					transfer.setContent(content);

					WritableByteChannel outputChannel = Channels
							.newChannel(content);
					final ByteBuffer buffer = ByteBuffer
							.allocateDirect(16 * 1024);
					long totalBytesRead = 0;
					while (true) {
						int bytesRead = rbc.read(buffer);
						if (bytesRead == -1) {
							break;
						} else {
							totalBytesRead = totalBytesRead + bytesRead;
							buffer.flip();
							outputChannel.write(buffer);
							buffer.clear();
						}
					}
					transfer.setDownloadDuration(System.nanoTime() - startTime);
					transfer.setContentLength(totalBytesRead);
				}

				poller.feedBoundQueues(new SuccessEvent<>(transfer));
			} catch (IOException e) {
				poller.feedBoundQueues(new FailureEvent<FileTransfer>(e));
			}
		}
	}
}
