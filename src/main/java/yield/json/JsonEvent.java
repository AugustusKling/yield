package yield.json;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Flat JSON object.
 */
public class JsonEvent {
	private ObjectNode fields;

	public JsonEvent(String source) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode node = mapper.readTree(source);
			if (node.isObject()) {
				fields = (ObjectNode) node;
			} else {
				throw new IOException("Expecting line as JSON object.");
			}
		} catch (IOException e) {
			fields = mapper.createObjectNode();
			fields.put("message", source);
		}
	}

	public JsonEvent(JsonEvent value) {
		fields = value.fields.deepCopy();
	}

	public String get(String key) {
		JsonNode jsonNode = fields.get(key);
		if (jsonNode == null) {
			return null;
		} else if (jsonNode.isTextual()) {
			return ((TextNode) jsonNode).asText();
		} else {
			return jsonNode.toString();
		}
	}

	public void put(String key, String value) {
		fields.put(key, value);
	}

	@Override
	public String toString() {
		return fields.toString();
	}

	public ObjectNode getObject() {
		return fields;
	}
}
