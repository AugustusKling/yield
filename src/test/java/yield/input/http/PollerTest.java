package yield.input.http;

import java.io.IOException;
import java.net.URL;

import javax.annotation.Nonnull;

import org.junit.Test;

import yield.core.Aggregator;
import yield.core.EventQueue;
import yield.core.EventType;
import yield.core.Producer;
import yield.core.Query;
import yield.core.QueueMapper;
import yield.core.Window;
import yield.core.event.MetaEvent;
import yield.core.windows.FixedSize;
import yield.output.Printer;

public class PollerTest {
	@Test
	public void pollWebFile() throws IOException, InterruptedException {
		Poller poller = new Poller(new URL("http://github.com/"), 3000);
		Query<Double> q = new Query<>(poller).map(
				new QueueMapper<MetaEvent<FileTransfer>, Long>() {
					@Nonnull
					private EventQueue<Long> queue = new EventQueue<>(
							Long.class);

					@Override
					protected Long map(MetaEvent<FileTransfer> e) {
						try {
							return e.get().getDownloadDuration() / 1000 / 1000;
						} catch (Exception e1) {
							return 0L;
						}
					}

					@Override
					@Nonnull
					public EventQueue<Long> getQueue() {
						return this.queue;
					}
				}).within(new Producer<Window<Long>>() {

			@Override
			public Window<Long> make() {
				return new FixedSize<>(3);
			}
		}, new Producer<Aggregator<Long, Double>>() {

			@Override
			public Aggregator<Long, Double> make() {
				return new Aggregator<Long, Double>(new EventType(Double.class)) {

					@Override
					protected void aggregate(Iterable<Long> events) {
						double avg = 0;
						int num = 0;
						for (Long e : events) {
							avg = avg + e;
							num++;
						}
						System.out.println("aggregating " + num);
						getQueue().feed(avg / num);
					}
				};
			}
		});
		poller.bind(new Printer<MetaEvent<FileTransfer>>("got event"));
		q.getQueue().bind(new Printer<Double>("average"));
		Thread.sleep(15000);
		poller.abortPolling();
	}
}
