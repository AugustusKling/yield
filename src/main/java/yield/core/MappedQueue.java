package yield.core;

import javax.annotation.Nonnull;

/**
 * Converter that transforms each event to another corresponding event.
 * 
 * @param <In>
 *            Type of input event.
 * @param <Out>
 *            Type of transformed event.
 */
public class MappedQueue<In, Out> extends QueueMapper<In, Out> {
	private ValueMapper<In, Out> mapper;
	@Nonnull
	private EventType inputType;
	@Nonnull
	private EventQueue<Out> queue;

	public MappedQueue(ValueMapper<In, Out> mapper,
			@Nonnull EventType inputType, @Nonnull EventType ouputType) {
		this.mapper = mapper;
		this.inputType = inputType;
		this.queue = new EventQueue<>(ouputType);
	}

	public MappedQueue(ValueMapper<In, Out> mapper,
			@Nonnull Class<In> inputType, @Nonnull Class<Out> ouputType) {
		this(mapper, new EventType(inputType), new EventType(ouputType));
	}

	@Override
	protected Out map(In e) {
		return mapper.map(e);
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return this.inputType;
	}

	@Override
	@Nonnull
	public EventQueue<Out> getQueue() {
		return this.queue;
	}

}
