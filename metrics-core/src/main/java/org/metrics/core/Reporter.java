package org.metrics.core;

import java.io.Closeable;

public interface Reporter extends Closeable{
	public void start();
	public void stop();
}
