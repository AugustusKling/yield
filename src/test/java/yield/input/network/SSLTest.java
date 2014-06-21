package yield.input.network;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

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
}
