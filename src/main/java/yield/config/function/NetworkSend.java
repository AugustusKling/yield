package yield.config.function;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventType;
import yield.core.Yielder;
import yield.output.network.SSLSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;

@ShortDocumentation(text = "Forwards to network location using SSL.")
public class NetworkSend extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		ObjectNode parameters = parseArguments(args);

		InputStream ksis;
		if (!parameters.has("keystore")) {
			throw new IllegalArgumentException("Missing parameter 'keystore'.");
		}
		if (!parameters.has("password")) {
			throw new IllegalArgumentException("Missing parameter 'password'.");
		}
		char[] password = parameters.get("password").textValue().toCharArray();
		if (!parameters.has("host")) {
			throw new IllegalArgumentException("Missing parameter 'host'.");
		}
		String host = parameters.get("host").asText();
		if (!parameters.has("port")) {
			throw new IllegalArgumentException("Missing parameter 'port'.");
		}
		int port = parameters.get("port").asInt();
		try {
			ksis = new FileInputStream(parameters.get("keystore").textValue());
		} catch (FileNotFoundException e1) {
			throw new IllegalArgumentException("Cannot read keystore.", e1);
		}

		Yielder<String> input = getYielderTypesafe(String.class,
				ConfigReader.LAST_SOURCE, context);
		SSLSocket socket;
		try {
			socket = new SSLSocket(host, port, ksis, password);
		} catch (KeyManagementException | KeyStoreException
				| NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new IllegalArgumentException(e);
		}
		input.bind(socket);
		return wrapResultingYielder(input);
	}

	@Override
	@Nonnull
	protected EventType getResultEventType() {
		return new EventType(String.class);
	}
}
