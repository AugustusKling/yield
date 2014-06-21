package yield.input.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import yield.config.ConfigLine;
import yield.config.ConfigReader;
import yield.config.ParseException;
import yield.config.TypedYielder;
import yield.output.network.SSLSocket;
import yield.test.Collector;

public class SSLTest {
	@Test
	public void run() throws Exception {
		Collector<String> coll = new Collector<>();

		InputStream keyStore = getClass().getResourceAsStream("/test.jks");
		char[] password = "abcdef".toCharArray();

		SSLServerSocket sslSocket = new SSLServerSocket(9999, keyStore,
				password);
		sslSocket.bind(coll);

		InputStream clientStore = getClass().getResourceAsStream("/test.jks");
		SSLSocket clientSSLSocket = new SSLSocket("localhost", 9999,
				clientStore, password);
		clientSSLSocket.feed("Das ist ein Test");

		// Give some time to thread to dispatch events.
		Thread.sleep(100);

		Assert.assertEquals("Das ist ein Test", coll.get(0));
	}

	@Test
	public void fromConfig() throws IOException {
		testConfigLine("network-send");
		testConfigLine("network-send ");
		Path tmp = Files.createTempFile(null, null);
		InputStream streamReader = getClass().getResourceAsStream("/test.jks");
		byte[] dst = new byte[5000];
		int length = streamReader.read(dst);
		Files.write(tmp, Arrays.copyOf(dst, length));
		testConfigLine("network-send keystore=" + tmp.toAbsolutePath());
		testConfigLine("network-send keystore=" + tmp.toAbsolutePath()
				+ " password=abcdef");
		testConfigLine("network-send keystore=" + tmp.toAbsolutePath()
				+ " password=abcdef host=localhost");
		try {
			testConfigLine("network-send keystore=" + tmp.toAbsolutePath()
					+ " password=abcdef host=localhost port=8443");
		} catch (ParseException e) {
			// Attempt expected to fail since no server started.
			Assert.assertTrue("Connection attempt made.", e.getCause()
					.getCause() instanceof ConnectException);
		}
	}

	private void testConfigLine(final String networkInit) {
		ConfigReader reader = new ConfigReader();
		Iterable<ConfigLine> lines = new Iterable<ConfigLine>() {
			@Override
			public Iterator<ConfigLine> iterator() {
				List<ConfigLine> config = new ArrayList<>();
				config.add(new ConfigLine("stdin", null, 1));
				config.add(new ConfigLine(networkInit, null, 2));
				return config.iterator();
			}
		};
		try {
			reader.toQueues(new HashMap<String, TypedYielder>(), lines);
		} catch (ParseException e) {
			Assert.assertTrue("Complain about missing syntax.",
					e.getCause() instanceof IllegalArgumentException);
		}
	}
}
