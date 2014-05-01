package yield.config.function;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.input.shipper.ShipperFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ReadFile extends FunctionConfig {
	public ReadFile() {
		super(ShipperFile.class);
	}

	@Override
	@Nonnull
	protected String shortDescription() {
		return "Monitors file any yields each line as event.";
	}

	@Override
	protected ObjectNode parseArguments(String args) {
		ObjectNode config = new ObjectMapper().createObjectNode();
		String filename = args.trim().replaceFirst("^\"", "")
				.replaceFirst("\"$", "");
		config.put("file", filename);
		return config;
	}

	@Override
	public String getResultEventType() {
		return String.class.getName();
	}
}