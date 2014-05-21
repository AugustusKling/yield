package yield.core;

import yield.input.ControlQueue;

public abstract class BaseControlQueueProvider implements ControlQueueProvider {
	private ControlQueue controlQueue;

	@Override
	public ControlQueue getControlQueue() {
		if (this.controlQueue == null) {
			this.controlQueue = new ControlQueue();
		}
		return this.controlQueue;
	}
}
