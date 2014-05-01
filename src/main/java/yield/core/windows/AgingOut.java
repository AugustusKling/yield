package yield.core.windows;

import java.util.Deque;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import yield.core.Window;

/**
 * Time dependent window of events. Events get discarded after they reach a
 * maximum life-time.
 * <p>
 * The size of the window is flexible and varies as time passes or new events
 * get feed in.
 * 
 * @param <T>
 *            Type of event.
 */
public class AgingOut<T> extends Window<T> {
	/**
	 * Retention time in milliseconds.
	 */
	private long retention;

	/**
	 * Events within time window.
	 */
	private Deque<AgeEvent<T>> events = new ConcurrentLinkedDeque<>();

	private Timer t = new Timer(true);

	/**
	 * Reference to upcoming cleanup job that discards all outdated events.
	 */
	private TimerTask publisher;

	/**
	 * Entry in window.
	 */
	private static class AgeEvent<T> {
		/**
		 * Time when the event was received.
		 */
		private long time;
		private T event;

		AgeEvent(long time, T event) {
			this.time = time;
			this.event = event;
		}
	}

	/**
	 * Discards too old events.
	 */
	private class CleanupTask extends TimerTask {
		@Override
		public void run() {
			long cleanupLimit = System.nanoTime() - retention * 1000 * 1000;
			boolean didModify = false;
			while (!events.isEmpty() && events.element().time < cleanupLimit) {
				events.remove();
				didModify = true;
			}
			if (events.isEmpty()) {
				publisher = null;
			} else {
				publisher = new CleanupTask();
				t.schedule(publisher, retention);
			}
			if (didModify) {
				// Refresh to inform aggregators about discarded events.
				refreshAggregates();
			}
		}
	}

	/**
	 * 
	 * @param retention
	 *            Time in milliseconds to retain events.
	 */
	public AgingOut(long retention) {
		this.retention = retention;
	}

	@Override
	public void feed(T e) {
		AgeEvent<T> ageEvent = new AgeEvent<>(System.nanoTime(), e);
		events.add(ageEvent);

		if (publisher == null) {
			publisher = new CleanupTask();
			t.schedule(publisher, retention);
		}

		// Inform aggregators about added events.
		refreshAggregates();
	}

	@Override
	protected Iterable<T> getEvents() {
		@SuppressWarnings("unchecked")
		final AgeEvent<T>[] agingEvents = events.toArray(new AgeEvent[0]);
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					int offset = 0;

					@Override
					public boolean hasNext() {
						return offset < agingEvents.length;
					}

					@Override
					public T next() {
						AgeEvent<T> e = agingEvents[offset];
						offset = offset + 1;
						return e.event;
					}

					@Override
					public void remove() {
						throw new NotImplementedException();
					}
				};
			}
		};
	}

}
