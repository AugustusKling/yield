package yield.output.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.input.ListenerExceutionFailed;

/**
 * Opens SSL connection to a server and forwards {@link String} events to it.
 */
public class SSLSocket extends BaseControlQueueProvider implements
		EventListener<String> {
	private OutputStream os;

	public SSLSocket(String host, int port, InputStream ksis, char[] password)
			throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, KeyManagementException {
		// Open key store.
		KeyStore serverKeyStore = KeyStore.getInstance("JKS");
		serverKeyStore.load(ksis, password);
		ksis.close();

		// Prepare socket factory.
		SSLContext sslContext = SSLContext.getInstance("TLS");
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(serverKeyStore);
		sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
		SSLSocketFactory sslsocketfactory = sslContext.getSocketFactory();

		// Open socket / connect.
		Logger logger = Logger.getLogger(getClass());
		logger.debug("Attempting to connection to " + host + ":" + port);
		Socket clientSslsocket = sslsocketfactory.createSocket(host, port);
		logger.debug("Connected to server " + host + ":" + port);

		os = clientSslsocket.getOutputStream();
	}

	@Override
	public void feed(String e) {
		try {
			os.write(e.getBytes());
			os.write("\n".getBytes());
			os.flush();
		} catch (IOException e1) {
			getControlQueue().feed(
					new ListenerExceutionFailed<>(e, "Could not send event.",
							e1));
		}
	}

}
