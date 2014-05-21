package yield.core;

import yield.input.ControlQueue;

public interface ControlQueueProvider {
	/**
	 * @return Means of control for error handling and other exceptional
	 *         circumstances. Multiple invocations return the same object.
	 */
	public ControlQueue getControlQueue();
}
