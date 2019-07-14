package org.metrics.core;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class ScheduledReporter implements Reporter {

    private static final Logger LOG = LoggerFactory.getLogger(ScheduledReporter.class);
    private static final AtomicInteger FACTORY_ID = new AtomicInteger();
    
    private final MetricRegistry registry;
    private final ScheduledExecutorService executor;
  
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(0);
        private final String namePrefix;

        private NamedThreadFactory(String name) {
            this.namePrefix = "metrics-" + name + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    protected ScheduledReporter(MetricRegistry registry,String name) {
		this(registry,
            Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name + '-' + FACTORY_ID.incrementAndGet())));
    }
	
    protected ScheduledReporter(MetricRegistry registry,
                                ScheduledExecutorService executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @Override
    public void start() {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    report();
                } catch (RuntimeException ex) {
                    LOG.error("RuntimeException thrown from {}#report. Exception was suppressed.", ScheduledReporter.this.getClass().getSimpleName(), ex);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown(); 
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow(); 
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                	LOG.error("{}:ScheduledExecutorService did not terminate.",getClass().getSimpleName());
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

  
    @Override
    public void close() {
        stop();
    }

    public void report() {
        synchronized (this) {
        	Map<String, Meter> meterMap=registry.meterMap();
        	Map<String, Timer> timerMap=registry.timerMap();
        	
        	for( Meter meter:meterMap.values()) {
        		meter.sample();
        	}
        	for( Timer timer:timerMap.values()) {
        		timer.sample();
        	}
            report(meterMap,timerMap);
        }
    }
   
    public abstract void report(Map<String,Meter> meterMap,Map<String,Timer> timerMap);

}