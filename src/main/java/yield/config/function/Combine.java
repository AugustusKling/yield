package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.Yielder;
import yield.filter.combinator.RegExCombinator;

@ShortDocumentation(text = "Concats multiple events until pattern does not match anymore.")
public class Combine extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<String> yielder = getYielderTypesafe(String.class.getName(),
				ConfigReader.LAST_SOURCE, context);
		String pattern;
		if (args.isEmpty()) {
			pattern = "^\\s.*";
		} else {
			pattern = args;
		}
		RegExCombinator combinator = new RegExCombinator(pattern);
		yielder.bind(combinator);
		return wrapResultingYielder(combinator.getQueue());
	}

	@Override
	public String getResultEventType() {
		return String.class.getName();
	}

}