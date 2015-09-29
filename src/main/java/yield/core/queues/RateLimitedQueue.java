package yield.core.queues;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import yield.core.EventQueue;

/**
 * Forwards received events unless they exceed rate. The rate is considered
 * exceeded whenever the period between 2 consecutive is shorter than the
 * configured time span. Optionally, an event can be yielded when the rate is
 * first exceeded.
 * 
 * @param <EventType>
 *            Any event type.
 */
public class RateLimitedQueue<EventType> extends EventQueue<EventType> {
	/**
	 * Duration the queue discards elements after receiving an event in
	 * nanoseconds.
	 */
	private long inactiveDuration;

	/**
	 * Whether to create informational event about first discarded event. The
	 * informational event is of type {@link RateLimitExceededEvent} and gets
	 * posted to the control queue.
	 */
	private boolean postDiscardState;

	/**
	 * {@code true} whilst discarding events due to exceeding rate.
	 */
	private boolean isInCooldown = false;

	/**
	 * {@code true} after first event has been received during inactivity
	 * duration.
	 */
	private boolean discardStateEmitted = false;

	private ScheduledExecutorService schedulerPool;

	public RateLimitedQueue(long inactiveDuration, TimeUnit delayUnit,
			boolean postDiscardState) {
		super(yield.core.EventType.ALL);

		this.postDiscardState = postDiscardState;
		this.inactiveDuration = delayUnit.toNanos(inactiveDuration);
		this.schedulerPool = Executors.newScheduledThreadPool(1);
	}

	@Override
	public void feed(EventType logEvent) {
		if (isInCooldown) {
			if (postDiscardState && !discardStateEmitted) {
				// Emit informational event about first discarded event.
				this.getControlQueue().feed(new RateLimitExceededEvent());
				this.discardStateEmitted = true;
			}
		} else {
			this.feedBoundQueues(logEvent);
			this.isInCooldown = true;

			// Reset flags after delay.
			schedulerPool.schedule(new Runnable() {
				@Override
				public void run() {
					RateLimitedQueue.this.isInCooldown = false;
					RateLimitedQueue.this.discardStateEmitted = false;
				}
			}, this.inactiveDuration, TimeUnit.NANOSECONDS);
		}
	}
}
