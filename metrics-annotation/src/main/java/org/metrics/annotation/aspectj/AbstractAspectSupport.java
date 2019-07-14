package org.metrics.annotation.aspectj;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.metrics.core.MetricRegistry;

public class AbstractAspectSupport {

	protected  MetricRegistry registry;
	
	protected AbstractAspectSupport(MetricRegistry registry) {
		if(registry==null) {
			throw new IllegalArgumentException("MetricRegistry can not be null.");
		}
		this.registry=registry;
	}
	
	/**
	 * 
	 * @param joinPoint
	 * @return
	 */
	protected Method resolveMethod(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Class<?> targetClass = joinPoint.getTarget().getClass();

		Method method = getDeclaredMethodFor(targetClass, signature.getName(),
				signature.getMethod().getParameterTypes());
		if (method == null) {
			throw new IllegalStateException(
					"Cannot resolve target method: " + signature.getMethod().getName());
		}
		return method;
	}
	

	/**
	 * Get declared method with provided name and parameterTypes in given class and
	 * its super classes. All parameters should be valid.
	 *
	 * @param clazz
	 *            class where the method is located
	 * @param name
	 *            method name
	 * @param parameterTypes
	 *            method parameter type list
	 * @return resolved method, null if not found
	 */
	private Method getDeclaredMethodFor(Class<?> clazz, String name,
			Class<?>... parameterTypes) {
		try {
			return clazz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null) {
				return getDeclaredMethodFor(superClass, name, parameterTypes);
			}
		}
		return null;
	}
	
	
}
