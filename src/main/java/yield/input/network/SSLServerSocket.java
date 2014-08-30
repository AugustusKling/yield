package yield.input.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yield.core.EventSource;

/**
 * Opens socket to listen for incoming events.
 */
public class SSLServerSocket extends EventSource<String> {
	private static final Logger logger = LogManager
			.getLogger(SSLServerSocket.class);

	/**
	 * @param port
	 *            Port to listen on.
	 * @param ksis
	 *            JKS key store. Stream will be closed.
	 * @param serverPassword
	 *            Password to open the key store.
	 */
	public SSLServerSocket(int port, InputStream ksis, char[] serverPassword)
			throws NoSuchAlgorithmException, IOException,
			KeyManagementException, UnrecoverableKeyException,
			KeyStoreException, CertificateException {
		// Open key store.
		KeyStore serverKeyStore = KeyStore.getInstance("JKS");
		serverKeyStore.load(ksis, serverPassword);
		ksis.close();

		// Prepare socket factory.
		SSLContext sslContext = SSLContext.getInstance("TLS");
		SecureRandom secureRandom = new SecureRandom();
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
				.getDefaultAlgorithm());
		kmf.init(serverKeyStore, serverPassword);
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(serverKeyStore);
		sslContext.init(kmf.getKeyManagers(),
				trustManagerFactory.getTrustManagers(), secureRandom);
		SSLServerSocketFactory sslServerSocketFactory = sslContext
				.getServerSocketFactory();

		// Open socket.
		final ServerSocket sslServerSocket = sslServerSocketFactory
				.createServerSocket(port);
		logger.debug("Listening on SSL socket with port " + port);

		Thread t = new Thread() {
			@Override
			public void run() {
				Socket socket;
				try {
					socket = sslServerSocket.accept();
					logger.debug("Got new connection.");

					SSLSocketReader reader = new SSLSocketReader(
							SSLServerSocket.this, socket);
					reader.start();
				} catch (IOException e) {
					logger.error("Failed to await socket connection.", e);
					SSLServerSocket.this.getControlQueue().feed(
							new ConnectionFailed(
									"Failed to await socket connection.", e));
				}
			}
		};
		t.setDaemon(false);
		t.start();
	}

	@Override
	public void feedBoundQueues(String logEvent) {
		super.feedBoundQueues(logEvent);
	}
}
