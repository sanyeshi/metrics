package org.metric.transport.http;

/**
 * The class of HTTP status. Simplification of {@code io.netty.handler.codec.http.HttpStatusClass}.
 *
 * @author Jon Schneider
 */

public enum HttpStatus {
	INFORMATIONAL(100, 200),
    SUCCESS(200, 300),
    REDIRECTION(300, 400),
    CLIENT_ERROR(400, 500),
    SERVER_ERROR(500, 600),
    UNKNOWN(0, 0) {
        @Override
        public boolean contains(int code) {
            return code < 100 || code >= 600;
        }
    };

    /**
     * Returns the class of the specified HTTP status code.
     */
    public static HttpStatus valueOf(int code) {
        if (INFORMATIONAL.contains(code)) {
            return INFORMATIONAL;
        }
        if (SUCCESS.contains(code)) {
            return SUCCESS;
        }
        if (REDIRECTION.contains(code)) {
            return REDIRECTION;
        }
        if (CLIENT_ERROR.contains(code)) {
            return CLIENT_ERROR;
        }
        if (SERVER_ERROR.contains(code)) {
            return SERVER_ERROR;
        }
        return UNKNOWN;
    }

    private final int min;
    private final int max;

    HttpStatus(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Returns {@code true} if and only if the specified HTTP status code falls into this class.
     */
    public boolean contains(int code) {
        return code >= min && code < max;
    }
}
