package yield.core;

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

	public MappedQueue(ValueMapper<In, Out> mapper) {
		this.mapper = mapper;
	}

	@Override
	protected Out map(In e) {
		return mapper.map(e);
	}

}
