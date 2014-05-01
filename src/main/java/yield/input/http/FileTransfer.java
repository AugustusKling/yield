package yield.input.http;

import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * File download.
 */
public class FileTransfer {

	private Map<String, List<String>> responseHeaders;
	private long contentLength;
	private OutputStream content;

	public long getDownloadDuration() {
		return downloadDuration;
	}

	private URL website;
	private long downloadDuration;

	public FileTransfer(URL website) {
		this.website = website;
	}

	public void setResponseHeaders(Map<String, List<String>> headerFields) {
		this.responseHeaders = headerFields;

	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;

	}

	public void setContent(OutputStream content) {
		this.content = content;

	}

	@Override
	public String toString() {
		return contentLength + " bytes content in "
				+ getDownloadDurationMilli() + " ms from " + website
				+ System.lineSeparator() + responseHeaders
				+ System.lineSeparator() + content;
	}

	private long getDownloadDurationMilli() {
		return downloadDuration / 1000 / 1000;
	}

	public Map<String, List<String>> getReponseHeaders() {
		return responseHeaders;
	}

	public URL getWebsite() {
		return website;
	}

	public OutputStream getContent() {
		return content;
	}

	/**
	 * @param downloadDuration
	 *            nano seconds.
	 */
	public void setDownloadDuration(long downloadDuration) {
		this.downloadDuration = downloadDuration;

	}
}
