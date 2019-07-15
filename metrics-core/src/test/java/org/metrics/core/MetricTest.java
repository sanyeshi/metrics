package org.metrics.core;

import java.util.Random;

import org.junit.Test;

public class MetricTest {
	
	@Test
	public void metricTest() throws InterruptedException{
		
		MetricRegistry registry=new MetricRegistry();
		ConsoleReporter reporter=new ConsoleReporter(registry);
		reporter.start();
		
		Meter meter=registry.meter("meter");
		Timer timer=registry.timber("timer");
		Random random=new Random();
		for(int i=0;i<1000;i++) {
			timer.start();
			Thread.sleep(random.nextInt(100));
			timer.stop();
			meter.mark();
		}
		reporter.close();
	}
	
}
