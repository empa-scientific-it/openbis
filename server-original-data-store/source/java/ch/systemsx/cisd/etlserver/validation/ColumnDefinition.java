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
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;

/**
 * Definition of a column used by {@link DataSetValidatorForTSV}.
 *
 * @author Franz-Josef Elmer
 */
class ColumnDefinition
{
    static final String MANDATORY_KEY = "mandatory";

    static final String ORDER_KEY = "order";

    static final String HEADER_VALIDATOR_KEY = "header-validator";

    static final String HEADER_PATTERN_KEY = "header-pattern";

    static final String VALUE_VALIDATOR_KEY = "value-validator";

    static final String CAN_DEFINE_MULTIPLE_COLUMNS_KEY = "can-define-multiple-columns";

    private final String name;

    private final IColumnHeaderValidator headerValidator;

    private final IValidatorFactory valueValidatorFactory;

    private final boolean mandatory;

    private final Integer orderOrNull;

    private final boolean canDefineMultipleColumns;

    static ColumnDefinition create(String name, Properties properties)
    {
        boolean mandatory = PropertyUtils.getBoolean(properties, MANDATORY_KEY, false);
        Integer order = null;
        boolean canDefineMultipleColumns = false;
        if (properties.getProperty(ORDER_KEY) != null)
        {
            order = PropertyUtils.getInt(properties, ORDER_KEY, 0);
            if (order < 1)
            {
                throw new ConfigurationFailureException("Order value has to be positive: " + order);
            }
            canDefineMultipleColumns = false;
        } else if (mandatory == false)
        {
            canDefineMultipleColumns =
                    PropertyUtils.getBoolean(properties, CAN_DEFINE_MULTIPLE_COLUMNS_KEY, false);
        }
        String headerValidatorName = properties.getProperty(HEADER_VALIDATOR_KEY);
        IColumnHeaderValidator headerValidator;
        if (headerValidatorName == null)
        {
            String headerPattern = properties.getProperty(HEADER_PATTERN_KEY, ".*");
            headerValidator = new RegExBasedValidator(headerPattern);
        } else
        {
            ExtendedProperties headerValidatorProperties =
                    ExtendedProperties.getSubset(properties, HEADER_VALIDATOR_KEY + ".", true);
            headerValidator =
                    ClassUtils.create(IColumnHeaderValidator.class, headerValidatorName,
                            headerValidatorProperties);
        }
        IValidatorFactory factory = createValidatorFactory(properties);
        return new ColumnDefinition(name, headerValidator, factory, mandatory, order,
                canDefineMultipleColumns);
    }

    static IValidatorFactory createValidatorFactory(Properties properties)
    {
        String validatorFactoryName =
                properties.getProperty(VALUE_VALIDATOR_KEY, DefaultValueValidatorFactory.class
                        .getName());
        IValidatorFactory factory =
                ClassUtils.create(IValidatorFactory.class, validatorFactoryName, properties);
        return factory;
    }

    private ColumnDefinition(String name, IColumnHeaderValidator headerValidator,
            IValidatorFactory valueValidatorFactory, boolean mandatory, Integer orderOrNull,
            boolean canDefineMultipleColumns)
    {
        this.name = name;
        this.headerValidator = headerValidator;
        this.valueValidatorFactory = valueValidatorFactory;
        this.mandatory = mandatory;
        this.orderOrNull = orderOrNull;
        this.canDefineMultipleColumns = canDefineMultipleColumns;
    }

    boolean canDefineMultipleColumns()
    {
        return canDefineMultipleColumns;
    }

    boolean isMandatory()
    {
        return mandatory;
    }

    Integer getOrderOrNull()
    {
        return orderOrNull;
    }

    String getName()
    {
        return name;
    }

    void assertValidHeader(String header)
    {
        Result result = validateHeader(header);
        if (result.isValid() == false)
        {
            throw new UserFailureException("According to column definition '" + name
                    + "' the header '" + header + "' is invalid because of the following reason: "
                    + result);
        }
    }

    Result validateHeader(String header)
    {
        return headerValidator.validateHeader(header);
    }

    IValidator createValidator(String header)
    {
        return valueValidatorFactory.createValidator(header);
    }
}
