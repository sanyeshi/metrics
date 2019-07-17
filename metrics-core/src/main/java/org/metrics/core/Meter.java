package org.metrics.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import com.codahale.metrics.Clock;

public class Meter implements Metric {
	
	private final LongAdder counter = new LongAdder();
	private final AtomicLong lastCounter = new AtomicLong(0);
	private long rate = 0;
	
	private com.codahale.metrics.Meter meter;

	protected Meter() {
		this(Clock.defaultClock());
	}

	protected Meter(Clock clock) {
		this.meter=new com.codahale.metrics.Meter(clock);
	}

	public void mark() {
		counter.add(1);
		meter.mark(1);
	}
	
	public void sample() {
		long currentCount = counter.longValue();
		rate = currentCount - lastCounter.get();
		lastCounter.set(currentCount);
	}

	public long getRate() {
		return rate;
	}
	public long getCount() {
		return meter.getCount();
	}
	
	public double getMeanRate() {
		return meter.getMeanRate();
	}
	
	public double getFifteenMinuteRate() {
		return meter.getOneMinuteRate();
	}

	public double getFiveMinuteRate() {
		return meter.getFiveMinuteRate();
	}
	
	public double getOneMinuteRate() {
		return meter.getFifteenMinuteRate();
	}
}
