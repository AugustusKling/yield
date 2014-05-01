package yield.core;

/**
 * Combines two events.
 * 
 * @param <InOne>
 *            Type of first event.
 * @param <InTwo>
 *            Type of second event.
 * @param <Out>
 *            Type of merged event.
 */
public interface Joiner<InOne, InTwo, Out> {
	Out join(InOne lastValue, InTwo lastValue2);
}
