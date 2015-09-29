package yield.config.function;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ParameterMap;
import yield.config.ParameterMap.Param;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventQueue;
import yield.core.queues.RateLimitExceededEvent;
import yield.core.queues.RateLimitedQueue;
import yield.input.ControlEvent;
import yield.input.ControlQueue;

@ShortDocumentation(text = "Forwards received events unless they exceed rate.")
public class RateLImit extends FunctionConfig {
	private static enum Parameters implements Param {
		@ShortDocumentation(text = "Duration the queue discards elements after receiving an event in seconds.")
		inactiveDuration {
			@Override
			public Object getDefault() {
				throw new UnsupportedOperationException();
			}
		},
		@ShortDocumentation(text = "Whether to create informational event about first discarded event.")
		postDiscardState {
			@Override
			public Boolean getDefault() {
				return Boolean.TRUE;
			}
		}
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		ParameterMap<Parameters> parameters = parseArguments(args,
				Parameters.class);
		final long inactiveDuration = parameters
				.getInteger(Parameters.inactiveDuration);
		boolean postDiscardState = parameters
				.getBoolean(Parameters.postDiscardState);

		RateLimitedQueue<Object> rateLimited = new RateLimitedQueue<>(
				inactiveDuration, TimeUnit.SECONDS, postDiscardState);
		getYielderTypesafe(Object.class, ConfigReader.LAST_SOURCE, context)
				.bind(rateLimited);

		final EventQueue<Object> ordinaryEventAndRateExceeded = new EventQueue<>(
				Object.class);
		rateLimited.bind(ordinaryEventAndRateExceeded);
		rateLimited.getControlQueue().bind(new ControlQueue() {
			@Override
			public void feed(ControlEvent logEvent) {
				if (logEvent instanceof RateLimitExceededEvent) {
					ordinaryEventAndRateExceeded
							.feed("Discarded events because configured limit exceeded. Period between 2 consecutive events was less than "
									+ inactiveDuration + "s.");
				}
			}
		});

		return wrapResultingYielder(ordinaryEventAndRateExceeded);
	}

	@Override
	@Nullable
	public <Parameter extends Enum<Parameter> & Param> Class<? extends Param> getParameters() {
		return Parameters.class;
	}
}
