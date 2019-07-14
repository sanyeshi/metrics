package org.metrics.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Timer  implements Metric{
	
	private final Clock clock;
	private final LongAdder counter=new LongAdder();
	private final LongAdder timeCounter=new LongAdder();
	
	private final AtomicLong lastCounter=new AtomicLong(0);
	private final AtomicLong lastTimeCounter=new AtomicLong(0);
	
	private final AtomicLong startTime=new AtomicLong();
	private long deltaCount;
	private long deltaTime;
	
	
	protected Timer(Clock clock) {
		this.clock=clock;
	}

	public Timer sample() {
		long currentCounter=counter.longValue();
		long currentTimeCounter=timeCounter.longValue();
		
		deltaCount=currentCounter-lastCounter.get();
		deltaTime=currentTimeCounter-lastTimeCounter.get();
		
		lastCounter.set(currentCounter);
		lastTimeCounter.set(currentTimeCounter);
		
		return this;
	}
	
	public void start() {
		startTime.set(clock.getTime());
	}
	public void stop() {
		counter.add(1);
		timeCounter.add(clock.getTime()-startTime.get());
	}

	public long getCount() {
		return deltaCount;
	}

	public double getAvgTime() {
		if(deltaCount==0) {
			return 0.0;
		}
		return ((double)deltaTime)/deltaCount;
	}
	
}
