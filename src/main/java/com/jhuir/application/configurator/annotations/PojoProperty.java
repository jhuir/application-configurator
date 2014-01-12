package com.jhuir.application.configurator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the field or method value should be loaded from the
 * configuration as complex object, using subset of configuration based on the
 * specified name
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD })
public @interface PojoProperty
{
	/**
	 * Optional name of the configuration key that will be used. If not
	 * specified the name of the field/method will be used
	 */
	String name() default "";

	/**
	 * Optional implementation class that will be instantiated.
	 */
	Class<?> implementation() default Void.class;
}
