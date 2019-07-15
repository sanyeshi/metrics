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
		
		System.out.println(String.format("\n%-30s %-10s %-5s %-5s %-5s %-5s",
				"date","name","count","max","min","avg"));
		for(Entry<String, Meter> entry:meterMap.entrySet()) {
			System.out.println(String.format("%-30s %-10s %-5d",
					new Date(),
					entry.getKey(),
					entry.getValue().getCount()));
		}
		
		
		for(Entry<String, Timer> entry:timerMap.entrySet()) {
			System.out.println(String.format("%-30s %-10s %-5d %-5d %-5d %-3.2f",
					new Date(),
					entry.getKey(),
					entry.getValue().getCount(),
					entry.getValue().getMax(),
					entry.getValue().getMin(),
					entry.getValue().getAvg()
					));
		}
		
	}

}
