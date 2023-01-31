/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.etlserver.validation;

import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Default factory for {@link IValidator} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class DefaultValueValidatorFactory implements IValidatorFactory
{
    private enum ValueType
    {
        UNIQUE_GROUPS
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return new UniqueGroupValidatorFactory(properties);
            }
        },
        UNIQUE
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return new UniqueValidatorFactory(properties);
            }
        },
        ANY
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return AnyValidatorFactory.INSTANCE;
            }
        },
        STRING
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return new StringValidatorFactory(properties);
            }
        },
        NUMERIC
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return new NumericValidatorFactory(properties);
            }
        };

        abstract IValidatorFactory createFactory(Properties properties);

        static ValueType getValueType(String name)
        {
            ValueType[] values = ValueType.values();
            for (ValueType valueType : values)
            {
                if (valueType.toString().equalsIgnoreCase(name))
                {
                    return valueType;
                }
            }
            throw new ConfigurationFailureException("Invalid " + VALUE_TYPE_KEY + ": " + name);
        }
    }

    static final String VALUE_TYPE_KEY = "value-type";

    private final IValidatorFactory factory;

    public DefaultValueValidatorFactory(Properties properties)
    {
        String property = properties.getProperty(VALUE_TYPE_KEY, ValueType.ANY.toString());
        ValueType valueType = ValueType.getValueType(property);
        factory = valueType.createFactory(properties);
    }

    @Override
    public IValidator createValidator(String header)
    {
        return factory.createValidator(header);
    }

}
