package com.jhuir.application.configurator;

import org.apache.commons.configuration.Configuration;

/**
 * Handler that is used to load and convert values from the configuration to
 * expected target types.
 */
public interface PropertyHandler
{
	/**
	 * Gets value from the configuration and converts it the expected target
	 * type
	 * 
	 * @param superPath
	 *            current path in case of subset configuration. Useful for
	 *            logging purposes
	 * @param config
	 *            current configuration that is used to load the value
	 * @param name
	 *            the key that will be used to load the value from the
	 *            configuration
	 * @param type
	 *            the expected target type of the value
	 * @return the value loaded from the configuration
	 */
	<T> T getValue(
			final String superPath,
			final Configuration config,
			final String name,
			final Class<T> type);
}
