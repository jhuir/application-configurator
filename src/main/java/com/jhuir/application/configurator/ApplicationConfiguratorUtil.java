package com.jhuir.application.configurator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import com.jhuir.application.configurator.annotations.PojoProperty;
import com.jhuir.application.configurator.annotations.Property;

/**
 * Utility class used to instantiate annotated POJO object with the values from
 * the specified Configuration ({@link #instantiatePojo(Configuration, Class)}).
 * <p>
 * The fields that should be loaded from the configuration must be annotated
 * with one of the annotations defined in
 * {@link com.jhuir.application.configurator.annotations}
 * </p>
 * <p>
 * Annotations can also be applied to field setters. Every method with exactly
 * one argument can be annotated, and will be called with the defined value from
 * the configuration
 * </p>
 */
public class ApplicationConfiguratorUtil
{
	private Map<Class<? extends PropertyHandler>, PropertyHandler> propertyHandlers = new HashMap<Class<? extends PropertyHandler>, PropertyHandler>();

	private ApplicationConfiguratorUtil()
	{
	}

	/**
	 * Instantiates a POJO object using the values from the specified
	 * configuration
	 * 
	 * @param configuration
	 *            configuration used to instantiate POJO fields
	 * @param targetPojoType
	 *            the type of the POJO that will be instantiated
	 * @return The newly created POJO instance
	 * @throws ApplicationConfiguratorException
	 *             in case something goes wrong
	 */
	public static <T> T instantiatePojo(final Configuration configuration, final Class<T> targetPojoType)
	{
		ApplicationConfiguratorUtil parser = new ApplicationConfiguratorUtil();
		return parser.instantiateImpl(new ConfigurationWithPath(null, configuration), targetPojoType);
	}

	private <T> T instantiateImpl(final ConfigurationWithPath cfg, final Class<T> pojoType)
	{
		if (cfg.getConfiguration().isEmpty())
		{
			return null;
		}

		T result;
		try
		{
			result = pojoType.newInstance();
		}
		catch (Exception e)
		{
			throw new ApplicationConfiguratorException(MessageFormat.format("Error creating instance of {0}",
					pojoType.getCanonicalName()), e);
		}

		try
		{
			handleSimples(cfg, pojoType, result);
			handlePojos(cfg, pojoType, result);
		}
		catch (Exception e)
		{
			throw new ApplicationConfiguratorException(MessageFormat.format("Error setting properties of {0}",
					pojoType.getCanonicalName()), e);
		}

		return result;
	}

	private void handleSimples(
			final ConfigurationWithPath cfg,
			final Class<?> pojoType,
			final Object instance) throws Exception
	{
		List<AnnotatedItem> items = new ArrayList<AnnotatedItem>();
		loadAnnotatedMethods(pojoType, items, Property.class);
		loadAnnotatedFields(pojoType, items, Property.class);

		for (AnnotatedItem i : items)
		{
			setSimple(cfg, i, instance);
		}
	}

	private void handlePojos(
			final ConfigurationWithPath cfg,
			final Class<?> pojoType,
			final Object instance) throws Exception
	{
		List<AnnotatedItem> items = new ArrayList<AnnotatedItem>();
		loadAnnotatedMethods(pojoType, items, PojoProperty.class);
		loadAnnotatedFields(pojoType, items, PojoProperty.class);

		for (AnnotatedItem i : items)
		{
			setPojo(cfg, i, instance);
		}
	}

	private void setSimple(
			final ConfigurationWithPath cfg,
			final AnnotatedItem item,
			final Object instance)
			throws Exception
	{
		Property ann = item.getAnnotation(Property.class);
		String name = ann.name().isEmpty() ? item.getName() : ann.name();
		Class<? extends PropertyHandler> handlerType = ann.handler();
		PropertyHandler handler = getPropertyHandler(handlerType);
		Object value = handler.getValue(cfg.getPath(), cfg.getConfiguration(), name, item.getType());
		if (value == null)
		{
			return;
		}
		item.setValue(instance, value);
	}

	private void setPojo(
			final ConfigurationWithPath cfg,
			final AnnotatedItem item,
			final Object instance)
			throws Exception
	{
		PojoProperty ann = item.getAnnotation(PojoProperty.class);
		String name = ann.name().isEmpty() ? item.getName() : ann.name();
		Class<?> impl = ann.implementation();
		Class<?> type = item.getType();
		if (impl.equals(Void.class))
		{
			impl = type;
		}
		else if (!type.isAssignableFrom(impl))
		{
			throw new ApplicationConfiguratorException(MessageFormat.format(
					"Specified {0} can not be assigned from {1}. Location: {2}",
					type, impl, item));
		}
		ConfigurationWithPath subConf = cfg.subset(name);
		Object value = instantiateImpl(subConf, impl);
		item.setValue(instance, value);
	}

	private String getSimpleName(final Method method)
	{
		String name = method.getName();
		if (name.startsWith("set") && name.length() > 3)
		{
			name = name.substring(3, 4).toLowerCase() + name.substring(4);
		}
		return name;
	}

	private Class<?> getParameter(final Method method)
	{
		Class<?>[] params = method.getParameterTypes();
		if (params == null || params.length != 1)
		{
			throw new ApplicationConfiguratorException(
					MessageFormat.format(
							"Method {0} in class {1} is not a valid setter (must have exactly 1 argument)",
							method.getName(), method.getDeclaringClass().getCanonicalName()));
		}
		return params[0];
	}

	private void loadAnnotatedFields(
			final Class<?> clazz,
			final List<AnnotatedItem> annotatedProperties,
			final Class<? extends Annotation> annotationType)
	{
		Field[] fields = clazz.getDeclaredFields();

		for (Field f : fields)
		{
			if (f.isAnnotationPresent(annotationType))
			{
				annotatedProperties.add(new FieldItem(f));
			}
		}

		Class<?> parent = clazz.getSuperclass();
		if (parent != null && !parent.equals(Object.class))
		{
			loadAnnotatedFields(parent, annotatedProperties, annotationType);
		}
	}

	private void loadAnnotatedMethods(
			final Class<?> clazz,
			final List<AnnotatedItem> annotatedMethods,
			final Class<? extends Annotation> annotationType)
	{
		Method[] methods = clazz.getDeclaredMethods();

		for (Method m : methods)
		{
			if (m.isAnnotationPresent(annotationType))
			{
				annotatedMethods.add(new MethodItem(m));
			}
		}

		Class<?> parent = clazz.getSuperclass();
		if (parent != null && !parent.equals(Object.class))
		{
			loadAnnotatedMethods(parent, annotatedMethods, annotationType);
		}
	}

	private PropertyHandler getPropertyHandler(final Class<? extends PropertyHandler> type) throws Exception
	{
		PropertyHandler handler = propertyHandlers.get(type);
		if (handler == null)
		{
			handler = type.newInstance();
			propertyHandlers.put(type, handler);
		}
		return handler;
	}

	private static class ConfigurationWithPath
	{
		private final Configuration configuration;
		private final String path;

		public ConfigurationWithPath(final String path, final Configuration configuration)
		{
			this.configuration = configuration;
			this.path = path;
		}

		public Configuration getConfiguration()
		{
			return configuration;
		}

		public String getPath()
		{
			return path;
		}

		public ConfigurationWithPath subset(final String key)
		{
			String subPath = MessageFormat.format("{0}.{1}", path, key);
			Configuration subConf = configuration.subset(key);
			return new ConfigurationWithPath(subPath, subConf);
		}
	}

	private interface AnnotatedItem
	{

		<T extends Annotation> T getAnnotation(Class<T> annotation);

		void setValue(Object instance, Object value) throws Exception;

		Class<?> getType();

		String getName();

	}

	private class MethodItem implements AnnotatedItem
	{
		private Method method;

		public MethodItem(final Method m)
		{
			method = m;
		}

		public <T extends Annotation> T getAnnotation(final Class<T> annotation)
		{
			return method.getAnnotation(annotation);
		}

		public void setValue(final Object instance, final Object value) throws Exception
		{
			method.setAccessible(true);
			method.invoke(instance, value);
		}

		public Class<?> getType()
		{
			return getParameter(method);
		}

		public String getName()
		{
			return getSimpleName(method);
		}

		@Override
		public String toString()
		{
			return MessageFormat.format("Object: {0}, Method: {1}",
					method.getDeclaringClass().getCanonicalName(),
					method.getName());
		}
	}

	private class FieldItem implements AnnotatedItem
	{
		private final Field field;

		public FieldItem(final Field f)
		{
			field = f;
		}

		public <T extends Annotation> T getAnnotation(final Class<T> annotation)
		{
			return field.getAnnotation(annotation);
		}

		public void setValue(final Object instance, final Object value) throws Exception
		{
			field.setAccessible(true);
			field.set(instance, value);
		}

		public Class<?> getType()
		{
			return field.getType();
		}

		public String getName()
		{
			return field.getName();
		}

		@Override
		public String toString()
		{
			return MessageFormat.format("Object: {0}, Field: {1}",
					field.getDeclaringClass().getCanonicalName(),
					field.getName());
		}
	}
}
