package org.metrics.elasticsearch.reporter;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metric.transport.http.HttpClientSender;
import org.metrics.core.MetricRegistry;
import org.metrics.core.Reporter;
import org.metrics.core.Timer;
import org.metrics.elasticsearch.reporter.ElasticsearchReporter.Node;


public class ElasticsearchReporterTest {

	@Before
	public void init() {
	}

	@Test
	public void report130Test() throws InterruptedException {
		
		MetricRegistry registry = new MetricRegistry();
		Reporter reporter=ElasticsearchReporter
			.forRegistry(registry)
			.httBuilder(new HttpClientSender())
			.esNode(new Node("192.168.63.139", 9200))
			.localHost("192.168.63.130")
			.build();
		reporter.start();
		
		Random random=new Random();
		//Meter meter = registry.meter("meter");
		Timer timer=registry.timber("timber");
		for (int i = 0; i < 100000; i++) {
			timer.start();
			Thread.sleep(random.nextInt(5));
			timer.stop();
			//meter.mark();
		}
		reporter.stop();
	}
	
	@Test
	public void report131Test() throws InterruptedException {
		
		MetricRegistry registry = new MetricRegistry();
		Reporter reporter=ElasticsearchReporter
			.forRegistry(registry)
			.httBuilder(new HttpClientSender())
			.esNode(new Node("192.168.63.139", 9200))
			.localHost("192.168.63.131")
			.build();
		reporter.start();
		
		Random random=new Random();
		//Meter meter = registry.meter("meter");
		Timer timer=registry.timber("timber");
		for (int i = 0; i < 100000; i++) {
			timer.start();
			Thread.sleep(random.nextInt(3));
			timer.stop();
			//meter.mark();
		}
		reporter.stop();
	}
	
	@After
	public void destroy() {
		
	}

}
