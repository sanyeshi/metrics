package org.metrics.elasticsearch.reporter;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.metric.transport.http.ConsoleSender;
import org.metric.transport.http.HttpSender;
import org.metrics.core.ConsoleReporter;
import org.metrics.core.Meter;
import org.metrics.core.MetricRegistry;
import org.metrics.core.ScheduledReporter;
import org.metrics.core.Timer;
import org.metrics.util.IOUtil;
import org.metrics.util.IPUtil;
import org.metrics.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Snapshot;

public class ElasticsearchReporter extends ScheduledReporter {

	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	public static class Node {
		private final String ip;
		private final int port;

		public Node(String ip, int port) {
			super();
			this.ip = ip;
			this.port = port;
		}

		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}

		@Override
		public String toString() {
			return "Node [ip=" + ip + ", port=" + port + "]";
		}
	}

	/**
	 * A builder for {@link ElasticsearchReporter} instances. Defaults to using the
	 * default locale and time zone, writing to ElasticSearch, converting rates to
	 * events/second, converting durations to milliseconds, and not filtering
	 * metrics.
	 */
	public static class Builder {
		private final MetricRegistry registry;
		private int esMajorVersion;
		private List<Node> esNodes;
		private HttpSender httpSender;
		private String localHost;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.esMajorVersion = 6;
			this.esNodes = new ArrayList<>();
			this.httpSender = new ConsoleSender();
		}

		public Builder esMajorVersion(int esMajorVersion) {
			this.esMajorVersion = esMajorVersion;
			return this;
		}

		public Builder esNode(Node node) {
			this.esNodes.add(node);
			return this;
		}

		public Builder esNode(List<Node> nodes) {
			this.esNodes.addAll(nodes);
			return this;
		}

		public Builder httBuilder(HttpSender httpSender) {
			this.httpSender = httpSender;
			return this;
		}

		public Builder localHost(String localHost) {
			this.localHost = localHost;
			return this;
		}

		/**
		 * Builds a {@link ConsoleReporter} with the given properties.
		 *
		 * @return a {@link ConsoleReporter}
		 */
		public ElasticsearchReporter build() {
			return new ElasticsearchReporter(registry, esMajorVersion, esNodes,
					httpSender, localHost);
		}
	}

	private static final Logger logger = LoggerFactory
			.getLogger(ElasticsearchReporter.class);
	private int esMajorVersion;
	private List<Node> nodes;
	private HttpSender httpSender;
	private Clock clock;
	private final DateTimeFormatter dtf;
	private AtomicInteger next = new AtomicInteger();
	private String localHost;

	private ElasticsearchReporter(MetricRegistry registry, int esMajorVersion,
			List<Node> nodes, HttpSender httpSender, String localHost) {
		super(registry, "es-reporter");
		this.esMajorVersion = esMajorVersion;
		this.nodes = nodes;
		this.httpSender = httpSender;
		this.clock = registry.getClock();
		this.dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		this.localHost = localHost;
		if (StringUtil.isEmpty(this.localHost)) {
			this.localHost = IPUtil.getLocalHost().orElse("127.0.0.1");
		}
		createTemplate();
	}

	private void createTemplate() {

		String template = null;
		if (esMajorVersion >= 7) {
			try (InputStream in = ElasticsearchReporter.class
					.getResourceAsStream("/template/metrics-template-7.json")) {
				template = IOUtil.toString(in);
			} catch (IOException e) {
				logger.error("Read metrics template error.", e);
			}
		} else {
			try (InputStream in = ElasticsearchReporter.class
					.getResourceAsStream("/template/metrics-template.json")) {
				template = IOUtil.toString(in);
			} catch (IOException e) {
				logger.error("Read metrics template error.", e);
			}
		}
		if (StringUtil.isEmpty(template)) {
			logger.warn("Metrics template is empty.");
			return;
		}

		try {
			httpSender.post(templateUrl())
					  .content("application/json", template)
					  .send()
					  .onSuccess(response -> {
						  logger.debug(
								"Create metrics template success,response code {},response body {}",
								response.code(), response.body());
					  })
					  .onError(response -> {
						  logger.error(
								"Create metrics template error,response code {},response body",
								response.code(), response.body());
					});
		} catch (Throwable e) {
			logger.error("Create metrics template error", e);
		}
	}

	@Override
	public void report(Map<String, Meter> meterMap, Map<String, Timer> timerMap) {

		String timestamp = ZonedDateTime
				.ofInstant(Instant.ofEpochMilli(clock.getTime()), ZoneId.systemDefault())
				.format(dtf);

		StringBuilder builder = new StringBuilder();

		if (!meterMap.isEmpty()) {
			builder.append(format(meterMap, timestamp, new MeterFormatter()));
		}

		if (!timerMap.isEmpty()) {
			builder.append(format(timerMap, timestamp, new TimerFormatter()));
		}

		try {
			httpSender.post(bulkUrl())
					  .content("application/json", builder.toString())
					  .send()
					  .onSuccess(response -> {
						  logger.debug(
								"Send to es success,response code {},response body {}",
								response.code(), response.body());
					  })
					  .onError(response -> {
						  logger.error("Send to es error,response code {},response body",
								response.code(), response.body());
					  });
		} catch (Throwable e) {
			logger.error("Send metrics to es error", e);
		}
	}

	private String formatJson(String val) {
		return String.format("\"%s\"", val);
	}
	
	private String formatJson(double val) {
		return String.format("%10.2f", val).trim();
	}

	private <T> String getIndexName(String timestamp, T t) {
		String date = timestamp.substring(0, 10);
		return "metrics-" + t.getClass().getSimpleName().toLowerCase() +"-"+ date;
	}

	private String bulkUrl() {
		Node node = nextNode();
		return "http://" + node.getIp() + ":" + node.getPort() + "/_bulk";
	}

	private String templateUrl() {
		Node node = nextNode();
		return "http://" + node.getIp() + ":" + node.getPort() + "/_template/metrics";
	}

	private Node nextNode() {
		if (nodes.isEmpty()) {
			logger.warn("No es node,use localhost:9200.");
			return new Node("localhost", 9200);
		}
		return nodes.get(next.getAndIncrement() % nodes.size());
	}

	private <T> String format(Map<String, T> map, String timestamp,
			Formatter<T> formatter) {
		
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, T> entry : map.entrySet()) {
			builder.append("{\"index\":")
					.append("{\"_index\":")
					.append(formatJson(getIndexName(timestamp, entry.getValue())));
			if (esMajorVersion < 7) {
				builder.append(",").append("\"_type\":\"_doc\"");
			}
			builder.append("}").append("}\n");

			builder.append("{").append("\"name\":").append(formatJson(entry.getKey()))
				   .append(",").append("\"@timestamp\":").append(formatJson(timestamp))
				   .append(",").append("\"host\":").append(formatJson(localHost));
			
			formatter.format(builder, entry.getValue());
			builder.append("}\n");
		}
		return builder.toString();
	}

	private interface Formatter<T> {
		void format(StringBuilder builder, T t);
	}

	private class MeterFormatter implements Formatter<Meter> {

		@Override
		public void format(StringBuilder builder, Meter meter) {
			builder.append(",\"count\":").append(meter.getCount())
			.append(",\"rate\":").append(meter.getRate())
			.append(",\"meanRate\":").append(formatJson(meter.getMeanRate()))
			.append(",\"m1Rate\":").append(formatJson(meter.getOneMinuteRate()))
			.append(",\"m5Rate\":").append(formatJson(meter.getFiveMinuteRate()))
			.append(",\"m15Rate\":").append(formatJson(meter.getFifteenMinuteRate()));
		}
	}

	private class TimerFormatter implements Formatter<Timer> {

		@Override
		public void format(StringBuilder builder, Timer timer) {
			Snapshot snapshot=timer.getSnapshot();
			builder.append(",\"count\":").append(timer.getCount())
			.append(",\"rate\":").append(timer.getRate())
			.append(",\"meanRate\":").append(formatJson(timer.getMeanRate()))
			.append(",\"m1Rate\":").append(formatJson(timer.getOneMinuteRate()))
			.append(",\"m5Rate\":").append(formatJson(timer.getFiveMinuteRate()))
			.append(",\"m15Rate\":").append(formatJson(timer.getFifteenMinuteRate()))
			.append(",\"max\":").append(timer.getMax())
			.append(",\"min\":").append(timer.getMin())
			.append(",\"avg\":").append(formatJson(timer.getAvg()))
			.append(",\"p75\":").append(formatJson(snapshot.get75thPercentile()))
			.append(",\"p95\":").append(formatJson(snapshot.get95thPercentile()))
			.append(",\"p98\":").append(formatJson(snapshot.get98thPercentile()))
			.append(",\"p99\":").append(formatJson(snapshot.get99thPercentile()))
			.append(",\"p999\":").append(formatJson(snapshot.get999thPercentile()));
		}
	}
}
