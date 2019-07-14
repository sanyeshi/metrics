package org.metrics.core;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class ConsoleReporter extends ScheduledReporter{

	public  ConsoleReporter(MetricRegistry registry) {
		super(registry, "console-reporter");
	}

	@Override
	public void report(Map<String,Meter> meterMap,
			Map<String, Timer> timerMap) {
		for(Entry<String, Meter> entry:meterMap.entrySet()) {
			System.out.println(String.format("%s %s %d",
					new Date(),
					entry.getKey(),
					entry.getValue().getCount()));
		}
		for(Entry<String, Timer> entry:timerMap.entrySet()) {
			System.out.println(String.format("%s %s %d %f",
					new Date(),
					entry.getKey(),
					entry.getValue().getCount(),
					entry.getValue().getAvgTime()
					));
		}
		
	}

}
