package org.metrics.util;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Utilities for IO.
 *
 * @author Johnny Lim
 */
public final class IOUtil {

	private static final int EOF = -1;

	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private IOUtil() {
	}

	/**
	 * Create a {@code String} from {@link InputStream} with {@link Charset}.
	 *
	 * @param inputStream
	 *            source {@link InputStream}
	 * @param charset
	 *            source {@link Charset}
	 * @return created {@code String}
	 */
	public static String toString(InputStream inputStream, Charset charset) {
		if (inputStream == null)
			return "";

		try (StringWriter writer = new StringWriter();
				InputStreamReader reader = new InputStreamReader(inputStream, charset);
				BufferedReader bufferedReader = new BufferedReader(reader)) {
			char[] chars = new char[DEFAULT_BUFFER_SIZE];
			int readChars;
			while ((readChars = bufferedReader.read(chars)) != EOF) {
				writer.write(chars, 0, readChars);
			}
			return writer.toString();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * Create a {@code String} from {@link InputStream} with default
	 * {@link Charset}.
	 *
	 * @param inputStream
	 *            source {@link InputStream}
	 * @return created {@code String}
	 */
	public static String toString(InputStream inputStream) {
		return toString(inputStream, Charset.defaultCharset());
	}
}