package yield.core.queues;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import yield.core.EventQueue;

/**
 * Queue which delays events by the given time.
 */
public class DelayedQueue<EventType> extends EventQueue<EventType> {
	/**
	 * Events awaiting expiration of their timeouts/delays.
	 */
	private DelayQueue<DelayedElement<EventType>> events = new DelayQueue<>();

	/**
	 * Queue delay in nanoseconds.
	 */
	private long delay;

	public DelayedQueue(long delay, TimeUnit delayUnit) {
		this.delay = delayUnit.toNanos(delay);

		Thread feeder = new Thread(getClass().getName() + " feeder") {
			@Override
			public void run() {
				try {
					while (true) {
						DelayedElement<EventType> delayedElement = DelayedQueue.this.events
								.take();
						DelayedQueue.this.feedBoundQueues(delayedElement
								.getEvent());
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		feeder.setDaemon(true);
		feeder.start();
	}

	@Override
	public void feed(EventType logEvent) {
		this.events.add(new DelayedElement<EventType>(logEvent, this.delay,
				TimeUnit.NANOSECONDS));
	}

}
