package com.jhuir.application.configurator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

/**
 * Default implementation of {@link PropertyHandler}
 */
public class DefaultPropertyHandler implements PropertyHandler
{
	private interface ParserAction<T>
	{
		T getValue(String superPath, Configuration config, String name, Class<T> type);
	}

	@SuppressWarnings("rawtypes")
	private final Map<Class, ParserAction> actions = new HashMap<Class, ParserAction>();

	public DefaultPropertyHandler()
	{
		actions.put(Integer.class, new IntegerParser());
		actions.put(int.class, new RequiredValueParserAction<Integer>(new IntegerParser()));
		actions.put(BigInteger.class, new BigIntParser());
		actions.put(BigDecimal.class, new BigDecParser());
		actions.put(Boolean.class, new BooleanParser());
		actions.put(boolean.class, new RequiredValueParserAction<Boolean>(new BooleanParser()));
		actions.put(Byte.class, new ByteParser());
		actions.put(byte.class, new RequiredValueParserAction<Byte>(new ByteParser()));
		actions.put(Double.class, new DoubleParser());
		actions.put(double.class, new RequiredValueParserAction<Double>(new DoubleParser()));
		actions.put(Float.class, new FloatParser());
		actions.put(float.class, new RequiredValueParserAction<Float>(new FloatParser()));
		actions.put(Long.class, new LongParser());
		actions.put(long.class, new RequiredValueParserAction<Long>(new LongParser()));
		actions.put(Short.class, new ShortParser());
		actions.put(short.class, new RequiredValueParserAction<Short>(new ShortParser()));
		actions.put(String.class, new StringParser());
		actions.put(String[].class, new StringArrayParser());
		actions.put(List.class, new ListParser());
	}

	public <T> T getValue(final String superPath, final Configuration config, final String name, final Class<T> type)
	{
		@SuppressWarnings("unchecked")
		ParserAction<T> p = actions.get(type);
		if (p == null)
		{
			throw new ApplicationConfiguratorException(
					MessageFormat.format("No property handler is defined for type {0}",
							type.getName()));
		}
		return p.getValue(superPath, config, name, type);
	}

	private class RequiredValueParserAction<T> implements ParserAction<T>
	{
		private final ParserAction<T> impl;

		public RequiredValueParserAction(final ParserAction<T> impl)
		{
			this.impl = impl;
		}

		public T getValue(final String superPath, final Configuration config, final String name, final Class<T> type)
		{
			T result = impl.getValue(superPath, config, name, type);
			if (result == null)
			{
				String fullPath = superPath == null || superPath.isEmpty()
						? name
						: MessageFormat.format("{0}.{1}", superPath, name);
				throw new ApplicationConfiguratorException(
						MessageFormat.format("Error parsing missing property {0} as {1}",
								fullPath, type.getName()));
			}
			return result;
		}

	}

	private abstract class BaseParserAction<T> implements ParserAction<T>
	{
		public T getValue(final String superPath, final Configuration config, final String name, final Class<T> type)
		{
			try
			{
				return getValueImpl(config, name);
			}
			catch (Exception e)
			{
				String fullPath = superPath == null || superPath.isEmpty()
						? name
						: MessageFormat.format("{0}.{1}", superPath, name);
				throw new ApplicationConfiguratorException(
						MessageFormat.format("Error parsing property {0} as {1}",
								fullPath, type.getName()), e);
			}
		}

		protected abstract T getValueImpl(Configuration config, String name);
	}

	private class IntegerParser extends BaseParserAction<Integer>
	{
		@Override
		protected Integer getValueImpl(final Configuration config, final String name)
		{
			return config.getInteger(name, null);
		}
	}

	private class BooleanParser extends BaseParserAction<Boolean>
	{
		@Override
		protected Boolean getValueImpl(final Configuration config, final String name)
		{
			return config.getBoolean(name, null);
		}
	}

	private class ByteParser extends BaseParserAction<Byte>
	{
		@Override
		protected Byte getValueImpl(final Configuration config, final String name)
		{
			return config.getByte(name, null);
		}
	}

	private class LongParser extends BaseParserAction<Long>
	{
		@Override
		protected Long getValueImpl(final Configuration config, final String name)
		{
			return config.getLong(name, null);
		}
	}

	private class ShortParser extends BaseParserAction<Short>
	{
		@Override
		protected Short getValueImpl(final Configuration config, final String name)
		{
			return config.getShort(name, null);
		}
	}

	private class StringParser extends BaseParserAction<String>
	{
		@Override
		protected String getValueImpl(final Configuration config, final String name)
		{
			return config.getString(name, null);
		}
	}

	private class DoubleParser extends BaseParserAction<Double>
	{
		@Override
		protected Double getValueImpl(final Configuration config, final String name)
		{
			return config.getDouble(name, null);
		}
	}

	private class FloatParser extends BaseParserAction<Float>
	{
		@Override
		protected Float getValueImpl(final Configuration config, final String name)
		{
			return config.getFloat(name, null);
		}
	}

	private class BigIntParser extends BaseParserAction<BigInteger>
	{
		@Override
		protected BigInteger getValueImpl(final Configuration config, final String name)
		{
			return config.getBigInteger(name, null);
		}
	}

	private class BigDecParser extends BaseParserAction<BigDecimal>
	{
		@Override
		protected BigDecimal getValueImpl(final Configuration config, final String name)
		{
			return config.getBigDecimal(name, null);
		}
	}

	private class StringArrayParser extends BaseParserAction<String[]>
	{
		@Override
		protected String[] getValueImpl(final Configuration config, final String name)
		{
			return config.getStringArray(name);
		}
	}

	private class ListParser extends BaseParserAction<List<Object>>
	{
		@Override
		protected List<Object> getValueImpl(final Configuration config, final String name)
		{
			return config.getList(name);
		}
	}
}
