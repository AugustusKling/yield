package yield.core;

import javax.annotation.Nonnull;

/**
 * Combines queues such that an event is yielded which merges the newest events
 * of the input queues.
 * 
 * @param <InOne>
 *            First input queue event type.
 * @param <InTwo>
 *            Second input queue event type.
 * @param <Out>
 *            Output queue event type.
 */
public abstract class Join<InOne, InTwo, Out> implements SourceProvider<Out>,
		Joiner<InOne, InTwo, Out> {
	class LastValue<T> extends BaseControlQueueProvider implements
			EventListener<T> {
		T lastValue;
		boolean hasValue = false;

		@Override
		public void feed(T e) {
			lastValue = e;
			hasValue = true;

			emitJoined();
		}

		@Override
		@Nonnull
		public EventType getInputType() {
			return EventType.ALL;
		}

	}

	LastValue<InOne> valueOne = new LastValue<>();
	LastValue<InTwo> valueTwo = new LastValue<>();
	private final EventQueue<Out> queue;

	public Join(Yielder<InOne> one, Yielder<InTwo> two,
			@Nonnull EventType outType) {
		this.queue = new EventQueue<>(outType);

		one.bind(valueOne);
		two.bind(valueTwo);
	}

	private void emitJoined() {
		if (valueOne.hasValue && valueTwo.hasValue) {
			queue.feed(join(valueOne.lastValue, valueTwo.lastValue));
		}
	}

	@Override
	public EventQueue<Out> getQueue() {
		return this.queue;
	}
}
