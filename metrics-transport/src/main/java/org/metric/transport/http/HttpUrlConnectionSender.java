package org.metric.transport.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

import org.metrics.util.IOUtil;

/**
 * {@link HttpURLConnection}-based {@link HttpSender}.
 *
 * @author Jon Schneider
 * @author Johnny Lim
 */

public class HttpUrlConnectionSender implements HttpSender {

	private static final int DEFAULT_CONNECT_TIMEOUT_MS = 1000;
	private static final int DEFAULT_READ_TIMEOUT_MS = 10000;

	private final int connectTimeoutMs;
	private final int readTimeoutMs;
	private final Proxy proxy;

	/**
	 * Creates a sender with the specified timeouts but uses the default proxy
	 * settings.
	 *
	 * @param connectTimeout
	 *            connect timeout when establishing a connection
	 * @param readTimeout
	 *            read timeout when receiving a response
	 */
	public HttpUrlConnectionSender(Duration connectTimeout, Duration readTimeout) {
		this(connectTimeout, readTimeout, null);
	}

	/**
	 * Creates a sender with the specified timeouts and proxy settings.
	 *
	 * @param connectTimeout
	 *            connect timeout when establishing a connection
	 * @param readTimeout
	 *            read timeout when receiving a response
	 * @param proxy
	 *            proxy to use when establishing a connection
	 */
	public HttpUrlConnectionSender(Duration connectTimeout, Duration readTimeout,
			Proxy proxy) {
		this.connectTimeoutMs = (int) connectTimeout.toMillis();
		this.readTimeoutMs = (int) readTimeout.toMillis();
		this.proxy = proxy;
	}

	/**
	 * Use the default timeouts and proxy settings for the sender.
	 */
	public HttpUrlConnectionSender() {
		this.connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;
		this.readTimeoutMs = DEFAULT_READ_TIMEOUT_MS;
		this.proxy = null;
	}

	@Override
	public Response send(Request request) throws IOException {
		HttpURLConnection con = null;
		URL url=request.getUrI().toURL();
		try {
			if (proxy != null) {
				con = (HttpURLConnection) url.openConnection(proxy);
			} else {
				con = (HttpURLConnection) url.openConnection();
			}
			con.setConnectTimeout(connectTimeoutMs);
			con.setReadTimeout(readTimeoutMs);
			Method method = request.getMethod();
			con.setRequestMethod(method.name());

			for (Map.Entry<String, String> header : request.getRequestHeaders()
					.entrySet()) {
				con.setRequestProperty(header.getKey(), header.getValue());
			}

			if (method != Method.GET) {
				con.setDoOutput(true);
				try (OutputStream os = con.getOutputStream()) {
					os.write(request.getEntity());
					os.flush();
				}
			}

			int status = con.getResponseCode();

			String body = null;
			try {
				if (con.getErrorStream() != null) {
					body = IOUtil.toString(con.getErrorStream());
				} else if (con.getInputStream() != null) {
					body = IOUtil.toString(con.getInputStream());
				}
			} catch (IOException ignored) {
			}

			return new Response(status, body);
		} finally {
			try {
				if (con != null) {
					con.disconnect();
				}
			} catch (Exception ignore) {
			}
		}
	}
}
