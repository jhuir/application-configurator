application-configurator
========================

##Description

Application configurator is used for easy mapping from commons-configuration to Java POJO that can be used to configure your Java applications.

##Code Examples

Code example that initializes configuration POJO from properties file.

<code>
  **ExampleMain.java**

	package com.example.application.configurator;

	import org.apache.commons.configuration.Configuration;
	import org.apache.commons.configuration.PropertiesConfiguration;

	import com.jhuir.application.configurator.ApplicationConfiguratorUtil;

	public class ExampleMain
	{
		public static void main(final String[] args) throws Exception
		{
			Configuration configuration = new PropertiesConfiguration("configuration.properties");

			AppConfiguration config = ApplicationConfiguratorUtil.instantiatePojo(configuration, AppConfiguration.class);

			System.out.println(config);
		}
	}
</code>

<code>
  **configuration.properties**

	stringProperty=string value
	intProperty=100
	pojoProperty.longProperty=90000
	pojoProperty.stringArray=s1, s2, s3
</code>

<code>
**AppConfiguration.java**
  
	package com.example.application.configurator;

	import com.jhuir.application.configurator.annotations.PojoProperty;
	import com.jhuir.application.configurator.annotations.Property;

	public class AppConfiguration
	{
		@Property
		private String stringProperty;

		@Property(name = "intProperty")
		private int intProp;

		@PojoProperty
		private AppConfigPojo pojoProperty;

		public String getStringProperty()
		{
			return stringProperty;
		}

		public int getIntProp()
		{
			return intProp;
		}

		public AppConfigPojo getPojoProperty()
		{
			return pojoProperty;
		}

		@Override
		public String toString()
		{
			return "AppConfiguration [stringProperty=" + stringProperty + ", intProp=" + intProp + ", pojoProperty="
					+ pojoProperty + "]";
		}

	}
</code>


<code>
  **AppConfigPojo.java**
  
	package com.example.application.configurator;
	
	import java.util.Arrays;
	
	import com.jhuir.application.configurator.annotations.Property;
	
	public class AppConfigPojo
	{
		@Property
		private long longProperty;
	
		@Property
		private String[] stringArray;
	
		public long getLongProperty()
		{
			return longProperty;
		}
	
		public String[] getStringArray()
		{
			return stringArray;
		}
	
		@Override
		public String toString()
		{
			return "AppConfigPojo [longProperty=" + longProperty + ", stringArray=" + Arrays.toString(stringArray) + "]";
		}

	}
</code>

The output of the example shows the loaded values:
<br/>
*AppConfiguration [stringProperty=string value, intProp=100, pojoProperty=AppConfigPojo [longProperty=90000, stringArray=[s1, s2, s3]]]*
