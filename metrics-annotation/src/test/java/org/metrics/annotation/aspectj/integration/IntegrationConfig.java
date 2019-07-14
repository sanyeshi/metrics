package org.metrics.annotation.aspectj.integration;

import org.metrics.annotation.aspectj.MetricAspect;
import org.metrics.core.ConsoleReporter;
import org.metrics.core.MetricRegistry;
import org.metrics.core.Reporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan("org.metrics.annotation.aspectj.integration")
public class IntegrationConfig {

	@Bean
	public MetricRegistry metricRegistry() {
		return new MetricRegistry();
	}

	@Bean
	public Reporter consoleReporter() {
		ConsoleReporter reporter = new ConsoleReporter(metricRegistry());
		reporter.start();
		return reporter;
	}

	@Bean
	public MetricAspect metricAspect() {
		return new MetricAspect(metricRegistry());
	}

}
