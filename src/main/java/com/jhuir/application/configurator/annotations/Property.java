package com.jhuir.application.configurator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.jhuir.application.configurator.DefaultPropertyHandler;
import com.jhuir.application.configurator.PropertyHandler;

/**
 * Indicates that the field or method value should be loaded from the
 * configuration as simple object
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.FIELD, ElementType.METHOD })
public @interface Property
{
	/**
	 * Optional name of the configuration key that will be used. If not
	 * specified the name of the field/method will be used
	 */
	String name() default "";

	/**
	 * Optional {@link PropertyHandler} that will be used to load and convert
	 * the value from the Configuration
	 */
	Class<? extends PropertyHandler> handler() default DefaultPropertyHandler.class;
}
