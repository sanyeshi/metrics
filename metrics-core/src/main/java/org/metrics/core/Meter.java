package org.metrics.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Meter implements Metric {
	private final LongAdder counter = new LongAdder();
	private final AtomicLong lastCounter = new AtomicLong(0);

	private long count = 0;

	protected Meter() {
	}

	public void mark() {
		counter.add(1);
	}

	public Meter sample() {
		long currentCount = counter.longValue();
		count= currentCount - lastCounter.get();
		lastCounter.set(currentCount);
		return this;
	}

	public long getCount() {
		return count;
	}
}
