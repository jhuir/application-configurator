package com.jhuir.application.configurator;

/**
 * Exception thrown when there is error while processing Configuration
 */
public class ApplicationConfiguratorException extends RuntimeException
{
	private static final long serialVersionUID = 8249821844317756770L;

	public ApplicationConfiguratorException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ApplicationConfiguratorException(final String message)
	{
		super(message);
	}

	public ApplicationConfiguratorException(final Throwable cause)
	{
		super(cause);
	}

}
