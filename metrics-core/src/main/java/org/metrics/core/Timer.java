package org.metrics.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.Histogram;

public class Timer implements Metric {

	private final LongAdder sum;
	private volatile long max = Long.MIN_VALUE;
	private volatile long min = Long.MAX_VALUE;

	private Snapshot last;
	private Snapshot current;
	private Snapshot diff;

	private final Clock clock;
	private final AtomicLong startTime;
	private final Meter meter;
	private final Histogram histogram;

	protected Timer() {
		this(Clock.defaultClock());
	}

	protected Timer(Clock clock) {
		this.sum = new LongAdder();
		this.last = new Snapshot();
		this.current = new Snapshot();
		this.diff = new Snapshot();
		this.clock = clock;
		this.startTime = new AtomicLong(clock.getTime());
		this.meter = new Meter(clock);
		this.histogram = new Histogram(new ExponentiallyDecayingReservoir());
	}

	public void sample() {
		meter.sample();
		current.update(this);
		diff(current, last, diff);
		last.update(current);
		max = Long.MIN_VALUE;
		min = Long.MAX_VALUE;
	}

	public void start() {
		startTime.set(clock.getTime());
	}

	public void stop() {
		update(clock.getTime() - startTime.get());
	}

	private void update(long duration) {
		if (duration < 0) {
			return;
		}
		sum.add(duration);
		meter.mark();
		histogram.update(duration);
		synchronized (this) {
			if (duration > max) {
				max = duration;
			}
			if (duration < min) {
				min = duration;
			}
		}
	}
	
	public long getRate() {
		return meter.getRate();
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

	public long getMax() {
		return diff.max;
	}

	public long getMin() {
		return diff.min;
	}

	public double getAvg() {
		if (meter.getRate() == 0) {
			return 0.0;
		}
		return diff.sum / (double) meter.getRate();
	}

	public com.codahale.metrics.Snapshot getSnapshot() {
		return histogram.getSnapshot();
	}

	private void diff(Snapshot current, Snapshot last, Snapshot result) {
		result.sum = current.sum - last.sum;
		result.max = current.max;
		result.min = current.min;
	}

	private class Snapshot {
		long sum = 0;
		long max = 0;
		long min = 0;

		public Snapshot() {

		}
		public void update(Timer timer) {
			this.sum = timer.sum.longValue();
			this.max = timer.max;
			this.min = timer.min;
		}

		public void update(Snapshot snapshot) {
			this.sum = snapshot.sum;
			this.max = snapshot.max;
			this.min = snapshot.min;
		}
	}

}
