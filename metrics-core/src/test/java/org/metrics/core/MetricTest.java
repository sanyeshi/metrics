package org.metrics.core;

import org.junit.Test;

public class MetricTest {
	
	@Test
	public void metricTest() throws InterruptedException{
		
		MetricRegistry registry=new MetricRegistry();
		ConsoleReporter reporter=new ConsoleReporter(registry);
		reporter.start();
		
		Meter meter=registry.meter("meter");
		Timer timer=registry.timber("timer");
		for(int i=0;i<1000;i++) {
			timer.start();
			Thread.sleep(10);
			timer.stop();
			meter.mark();
		}
		reporter.close();
	}
	
}
