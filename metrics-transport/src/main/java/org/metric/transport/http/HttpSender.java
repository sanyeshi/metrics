package org.metric.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

import org.metrics.util.StringUtil;

/**
 *
 * @author Jon Schneider
 * @since 1.1.0
 */

public interface HttpSender {
	Response send(Request request) throws Throwable;

	default Request.Builder post(String uri) {
		return newRequest(uri).method(Method.POST);
	}

	default Request.Builder head(String uri) {
		return newRequest(uri).method(Method.HEAD);
	}

	default Request.Builder put(String uri) {
		return newRequest(uri).method(Method.PUT);
	}

	default Request.Builder get(String uri) {
		return newRequest(uri).method(Method.GET);
	}

	default Request.Builder delete(String uri) {
		return newRequest(uri).method(Method.DELETE);
	}

	default Request.Builder options(String uri) {
		return newRequest(uri).method(Method.OPTIONS);
	}

	default Request.Builder newRequest(String uri) {
		return new Request.Builder(uri, this);
	}

	class Request {
		private final URI uri;
		private final byte[] entity;
		private final Method method;
		private final Map<String, String> requestHeaders;

		private Request(URI uri, byte[] entity, Method method,
				Map<String, String> requestHeaders) {
			this.uri = uri;
			this.entity = entity;
			this.method = method;
			this.requestHeaders = requestHeaders;
		}

		public URI getUrI() {
			return uri;
		}

		public byte[] getEntity() {
			return entity;
		}

		public Method getMethod() {
			return method;
		}

		public Map<String, String> getRequestHeaders() {
			return requestHeaders;
		}

		public static Builder build(String uri, HttpSender sender) {
			return new Builder(uri, sender);
		}

		@Override
		public String toString() {
			StringBuilder printed = new StringBuilder(method.toString()).append(" ")
					.append(uri.toString()).append("\n");
			if (entity.length == 0) {
				printed.append("<no request body>");
			} else if ("application/json".equals(requestHeaders.get("Content-Type"))) {
				printed.append(new String(entity));
			} else {
				printed.append(new String(entity));
			}
			return printed.toString();
		}

		public static class Builder {
			private static final String APPLICATION_JSON = "application/json";
			private static final String TEXT_PLAIN = "text/plain";

			private final URI uri;
			private final HttpSender sender;

			private byte[] entity = new byte[0];
			private Method method;
			private Map<String, String> requestHeaders = new LinkedHashMap<>();

			Builder(String uri, HttpSender sender) {
				this.uri = URI.create(uri);
				this.sender = sender;
			}

			/**
			 * Add a header to the request.
			 *
			 * @param name
			 *            The name of the header.
			 * @param value
			 *            The value of the header.
			 * @return This request builder.
			 */
			public final Builder header(String name, String value) {
				requestHeaders.put(name, value);
				return this;
			}

			/**
			 * If user and password are non-empty, set basic authentication on the
			 * request.
			 *
			 * @param user
			 *            A user name, if available.
			 * @param password
			 *            A password, if available.
			 * @return This request builder.
			 */
			public final Builder basicAuthentication(String user, String password) {
				if (user != null && StringUtil.isNotBlank(user)) {
					String encoded = Base64.getEncoder()
							.encodeToString((user.trim() + ":"
									+ (password == null ? "" : password.trim()))
											.getBytes(StandardCharsets.UTF_8));
					header("Authorization", "Basic " + encoded);
				}
				return this;
			}

			/**
			 * Set the request body as JSON.
			 *
			 * @param content
			 *            The request body.
			 * @return This request builder.
			 */
			public final Builder jsonContent(String content) {
				return content(APPLICATION_JSON, content);
			}

			/**
			 * Set the request body as JSON.
			 *
			 * @param content
			 *            The request body.
			 * @return This request builder.
			 */
			public final Builder plainText(String content) {
				return content(TEXT_PLAIN, content);
			}

			/**
			 * Set the request body.
			 *
			 * @param type
			 *            The value of the "Content-Type" header to add.
			 * @param content
			 *            The request body.
			 * @return This request builder.
			 */
			public final Builder content(String type, String content) {
				return content(type, "UTF-8",content.getBytes(StandardCharsets.UTF_8));
			}

			/**
			 * Set the request body.
			 *
			 * @param type
			 *            The value of the "Content-Type" header to add.
			 * @param content
			 *            The request body.
			 * @return This request builder.
			 */
			public final Builder content(String type,String encode,byte[] content) {
				header("Content-Type", type);
				header("Content-Encoding", "UTF-8");
				entity = content;
				return this;
			}

			/**
			 * Add header to accept {@code application/json} data.
			 *
			 * @return This request builder.
			 */
			public Builder acceptJson() {
				return accept(APPLICATION_JSON);
			}

			/**
			 * Add accept header.
			 *
			 * @param type
			 *            The value of the "Accept" header to add.
			 * @return This request builder.
			 */
			public Builder accept(String type) {
				return header("Accept", type);
			}

			/**
			 * Set the request method.
			 *
			 * @param method
			 *            An HTTP method.
			 * @return This request builder.
			 */
			public final Builder method(Method method) {
				this.method = method;
				return this;
			}

			/**
			 * Add a "Content-Encoding" header of "gzip" and compress the request body.
			 *
			 * @return This request builder.
			 * @throws IOException
			 *             If compression fails.
			 */
			public final Builder compress() throws IOException {
				header("Content-Encoding", "gzip");
				this.entity = gzip(entity);
				return this;
			}

			/**
			 * Add a "Content-Encoding" header of "gzip" and compress the request body
			 * when the supplied condition is true.
			 *
			 * @param when
			 *            Condition that governs when to compress the request body.
			 * @return This request builder.
			 * @throws IOException
			 *             If compression fails.
			 */
			public final Builder compressWhen(Supplier<Boolean> when) throws IOException {
				if (when.get())
					return compress();
				return this;
			}

			private static byte[] gzip(byte[] data) throws IOException {
				ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
				try (GZIPOutputStream out = new GZIPOutputStream(bos)) {
					out.write(data);
				}
				return bos.toByteArray();
			}

			public Response send() throws Throwable {
				return sender.send(new Request(uri, entity, method, requestHeaders));
			}
		}
	}

	class Response {
		public static final String NO_RESPONSE_BODY = "<no response body>";
		private final int code;
		private final String body;

		public Response(int code, String body) {
			this.code = code;
			this.body = StringUtil.isBlank(body) ? NO_RESPONSE_BODY : body;
		}

		public int code() {
			return code;
		}

		public String body() {
			return body;
		}

		public Response onSuccess(Consumer<Response> onSuccess) {
			switch (HttpStatus.valueOf(code)) {
			case INFORMATIONAL:
			case SUCCESS:
				onSuccess.accept(this);
			default:
				break;
			}
			return this;
		}

		public Response onError(Consumer<Response> onError) {
			switch (HttpStatus.valueOf(code)) {
			case CLIENT_ERROR:
			case SERVER_ERROR:
				onError.accept(this);
			default:
				break;
			}
			return this;
		}

		public boolean isSuccessful() {
			switch (HttpStatus.valueOf(code)) {
			case INFORMATIONAL:
			case SUCCESS:
				return true;
			default:
				return false;
			}
		}
	}

	enum Method {
		GET, HEAD, POST, PUT, DELETE, OPTIONS
	}
}
