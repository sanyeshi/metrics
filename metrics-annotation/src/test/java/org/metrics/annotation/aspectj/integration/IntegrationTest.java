package org.metrics.annotation.aspectj.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = { IntegrationConfig.class })
public class IntegrationTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private FooService fooService;
	
	
	@Test
	public void fooTest() throws InterruptedException {
		for(int i=0;i<1000;i++) {
			fooService.meterFoo(i);
			fooService.timerFoo(i);
		}
		Thread.sleep(3000);
	}
	
}
