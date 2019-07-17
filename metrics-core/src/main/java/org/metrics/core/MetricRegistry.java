package org.metrics.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.codahale.metrics.Clock;

public class MetricRegistry {
	
	private ConcurrentMap<String, Meter> meterMap=new ConcurrentHashMap<String, Meter>(64);
	private ConcurrentMap<String, Timer> timerMap=new ConcurrentHashMap<String, Timer>(64);
	private Clock clock;
	
	public MetricRegistry() {
		this(Clock.defaultClock());
	}
	
	public MetricRegistry(Clock clock) {
		this.clock=clock;
	}
	
	public Meter meter(String name) {
		if(meterMap.containsKey(name)) {
			return meterMap.get(name);
		}
		Meter meter=new Meter();
		meterMap.put(name, meter);
		return meter;
	}
	
	public Timer timber(String name) {
		if(timerMap.containsKey(name)) {
			return timerMap.get(name);
		}
		Timer timer=new Timer(clock);
		timerMap.put(name, timer);
		return timer;
	}
	
	public Clock getClock() {
		return clock;
	}

	public ConcurrentMap<String, Meter> meterMap() {
		return meterMap;
	}
	
	public ConcurrentMap<String, Timer> timerMap() {
		return timerMap;
	}
}
