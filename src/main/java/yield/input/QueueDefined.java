package yield.input;

import yield.core.Yielder;

public class QueueDefined implements ControlEvent {

	private String name;

	public QueueDefined(String name, Yielder<?> queue) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "QueueDefined(" + name + ")";
	}
}
