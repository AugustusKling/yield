package yield.core.queues;

import java.util.concurrent.TimeUnit;

/**
 * Event that is only valid after delay passes.
 */
class DelayedElement<EventType> implements java.util.concurrent.Delayed {
	/**
	 * Time when event gets valid, nanoseconds relative to
	 * {@link System#nanoTime()}'s offset.
	 */
	private long expiryTime;

	/**
	 * Payload / delayed event.
	 */
	private EventType event;

	public DelayedElement(EventType event, long delay, TimeUnit delayUnit) {
		this.event = event;
		this.expiryTime = System.nanoTime() + delayUnit.toNanos(delay);
	}

	@Override
	public int compareTo(java.util.concurrent.Delayed o) {
		long comp = this.getDelay(TimeUnit.NANOSECONDS)
				- o.getDelay(TimeUnit.NANOSECONDS);
		if (comp < 0) {
			return -1;
		} else if (comp > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return this.expiryTime - System.nanoTime();
	}

	public EventType getEvent() {
		return this.event;
	}

}