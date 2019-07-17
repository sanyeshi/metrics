package org.metrics.core;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.codahale.metrics.Snapshot;

public class ConsoleReporter extends ScheduledReporter{

	public  ConsoleReporter(MetricRegistry registry) {
		super(registry, "console-reporter");
	}

	@Override
	public void report(Map<String,Meter> meterMap,
			Map<String, Timer> timerMap) {
		
		Date date=new Date();
		
		System.out.println(String.format("--Meter--\n%-30s %-10s %-5s %-5s %-10s %-10s %-10s %-10s",
				"date","name","count","rate","meanRate","m1Rate","m5Rate","m15Rate"));
		for(Entry<String, Meter> entry:meterMap.entrySet()) {
			String name=entry.getKey();
			Meter meter=entry.getValue();
			System.out.println(String.format("%-30s %-10s %-5d %-5d %-10.3f %-10.3f %-10.3f %-10.3f",
					date,
					name,
					meter.getCount(),
					meter.getRate(),
					meter.getMeanRate(),
					meter.getOneMinuteRate(),
					meter.getFiveMinuteRate(),
					meter.getFifteenMinuteRate()
					));
		}
		
		
		System.out.println(String.format("--Timer--\n%-30s %-10s %-5s %-5s %-10s %-10s %-10s %-10s "
				+ "%-5s %-5s %-10s %-10s %-10s %-10s",
				"date","name","count","rate","meanRate",
				"m1Rate","m5Rate","m15Rate","max","min","avg",
				"p95","p98","p99"));
		for(Entry<String, Timer> entry:timerMap.entrySet()) {
			String name=entry.getKey();
			Timer timer=entry.getValue();
			Snapshot snapshot=timer.getSnapshot();
			
			System.out.println(String.format("%-30s %-10s %-5d %-5d %-10.3f %-10.3f %-10.3f %-10.3f "
					+ "%-5d %-5d %-10.3f %-10.3f %-10.3f %-10.3f ",
					date,
					name,
					timer.getCount(),
					timer.getRate(),
					timer.getMeanRate(),
					timer.getOneMinuteRate(),
					timer.getFiveMinuteRate(),
					timer.getFifteenMinuteRate(),
					timer.getMax(),
					timer.getMin(),
					timer.getAvg(),
					snapshot.get95thPercentile(),
					snapshot.get98thPercentile(),
					snapshot.get99thPercentile()
					));
			
		}
		
	}

}
