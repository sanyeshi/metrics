package org.metrics.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Timer  implements Metric{
	
	private final Clock clock;
	private final AtomicLong startTime=new AtomicLong(0);
	
	private final LongAdder sum=new LongAdder();
	private final LongAdder count=new LongAdder();
	private volatile long max=Long.MIN_VALUE;
	private volatile long min=Long.MAX_VALUE;
	
	private Snapshot last;
	private Snapshot current;
	private Snapshot diff;

	protected Timer(Clock clock) {
		this.clock=clock;
		this.last=new Snapshot();
		this.current=new Snapshot();
		this.diff=new Snapshot();
	}

	public Timer sample() {
		current.update(this);
		diff(current, last, diff);
		last.update(current);
		max=Long.MIN_VALUE;
		min=Long.MAX_VALUE;
		return this;
	}
	
	public void start() {
		startTime.set(clock.getTime());
	}
	
	public void stop() {
		update(clock.getTime()-startTime.get());
	}
	
	private void update(long time) {
		sum.add(time);
		count.add(1);
		synchronized(this) {
			if(time>max) {
				max=time;
			}
			if(time<min) {
				min=time;
			}
		}
	}
	public long getCount() {
		return diff.count;
	}
	public long getSum() {
		return diff.sum;
	}
	
	public long getMax() {
		return diff.max;
	}
	
	public long getMin() {
		return diff.min;
	}
	
	public double getAvg() {
		if(diff.count==0) {
			return 0.0;
		}
		return diff.sum/(double)diff.count;
	}

	
	public void diff(Snapshot current,Snapshot last,Snapshot result) {
		result.sum=current.sum-last.sum;
		result.count=current.count-last.count;
		result.max=current.max;
		result.min=current.min;
	}
	
	
	private class Snapshot {
		long sum=0;
		long count=0;
		long max=0;
		long min=0;
		
		public Snapshot() {
			
		}
		
		public void update(Timer timer) {
			this.sum=timer.sum.longValue();
			this.count=timer.count.longValue();
			this.max=timer.max;
			this.min=timer.min;
		}
		
		public void update(Snapshot snapshot) {
			this.sum=snapshot.sum;
			this.count=snapshot.count;
			this.max=snapshot.max;
			this.min=snapshot.min;
		}
	}
	
}
