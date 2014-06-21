package yield.config.function;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.input.network.SSLServerSocket;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class NetworkListen extends FunctionConfig {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Listens for SSL connections and yields their messages.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		ObjectNode parameters = parseArguments(args);

		InputStream ksis;
		try {
			ksis = new FileInputStream(parameters.get("keystore").textValue());
		} catch (FileNotFoundException e1) {
			throw new IllegalArgumentException("Cannot read keystore.", e1);
		}
		char[] serverPassword = parameters.get("password").textValue()
				.toCharArray();
		int port = parameters.get("port").asInt();

		SSLServerSocket serverSocket;
		try {
			serverSocket = new SSLServerSocket(port, ksis, serverPassword);
		} catch (KeyManagementException | UnrecoverableKeyException
				| NoSuchAlgorithmException | KeyStoreException
				| CertificateException | IOException e) {
			throw new IllegalArgumentException(e);
		}
		return wrapResultingYielder(serverSocket);
	}

	@Override
	protected String getResultEventType() {
		return String.class.getName();
	}
}
