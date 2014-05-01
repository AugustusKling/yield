package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.TypedYielder;

public class Listen extends FunctionConfig {

	private String resultEventType = Object.class.getName();

	@Override
	@Nonnull
	protected String shortDescription() {
		return "Switches context to given yielder.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		TypedYielder yielder = context.get(args.trim());
		if (yielder == null) {
			throw new IllegalArgumentException("Cannot find yielder "
					+ args.trim());
		} else {
			this.resultEventType = yielder.type;
			return yielder;
		}
	}

	@Override
	protected String getResultEventType() {
		return this.resultEventType;
	}
}