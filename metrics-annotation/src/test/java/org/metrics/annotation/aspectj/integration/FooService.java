package org.metrics.annotation.aspectj.integration;

import org.metrics.annotation.Metered;
import org.metrics.annotation.Timered;
import org.springframework.stereotype.Service;



@Service
public class FooService {
	
	@Metered(name = "meter")
	public void meterFoo(int ms) {
		sleep(ms);
	}
	
	@Timered(name = "timer")
	public void timerFoo(int ms) {
		sleep(ms);
	}
	
	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			
		}
	}
	
}
