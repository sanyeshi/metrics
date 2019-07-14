package org.metrics.annotation.aspectj;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.metrics.annotation.Metered;
import org.metrics.annotation.Timered;
import org.metrics.core.Meter;
import org.metrics.core.MetricRegistry;
import org.metrics.core.Timer;


@Aspect
public class MetricAspect extends AbstractAspectSupport{
	
	public MetricAspect(MetricRegistry registry) {
		super(registry);
	}

    @Around("@annotation(org.metrics.annotation.Metered)")
    public Object invokeWithMetered(ProceedingJoinPoint pjp) throws Throwable {
        Method originMethod = resolveMethod(pjp);

        Metered annotation = originMethod.getAnnotation(Metered.class);
        if (annotation == null) {
            throw new IllegalStateException("Wrong state for Metered annotation");
        }
        try {
            return pjp.proceed();
        }catch (Throwable ex) {
            throw ex;
        } finally {
        	 String metricName = annotation.name();
        	 Meter meter=registry.meter(metricName);
        	 meter.mark();
        }
    }
    
    @Around("@annotation(org.metrics.annotation.Timered)")
    public Object invokeWithTimered(ProceedingJoinPoint pjp) throws Throwable {
        Method originMethod = resolveMethod(pjp);

        Timered annotation = originMethod.getAnnotation(Timered.class);
        if (annotation == null) {
            throw new IllegalStateException("Wrong state for Metered annotation");
        }
        String metricName = annotation.name();
   	    Timer timer=registry.timber(metricName);
   	    timer.start();
        try {
             return pjp.proceed();
        }catch (Throwable ex) {
            throw ex;
        } finally {
        	 timer.stop();
        }
    }
}
