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

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Default implementation of a data set validator.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetValidator implements IDataSetValidator
{
    static final String DATA_SET_VALIDATORS_KEY = "data-set-validators";

    static final String DATA_SET_TYPE_KEY = "data-set-type";

    static final String VALIDATOR_KEY = "validator";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetValidator.class);

    private final Map<String, IDataSetValidator> validators;

    public DataSetValidator(Properties properties)
    {
        validators = new HashMap<String, IDataSetValidator>();
        SectionProperties[] props =
                PropertyParametersUtil.extractSectionProperties(properties,
                        DATA_SET_VALIDATORS_KEY, false);
        for (SectionProperties sectionProperties : props)
        {
            Properties validatorProperties = sectionProperties.getProperties();
            String dataSetType = validatorProperties.getProperty(DATA_SET_TYPE_KEY);
            if (dataSetType == null)
            {
                throw new ConfigurationFailureException("Missing mandatory property: "
                        + sectionProperties.getKey() + "." + DATA_SET_TYPE_KEY);
            }
            try
            {
                IDataSetValidator validator = createValidator(validatorProperties);
                IDataSetValidator previous = validators.put(dataSetType, validator);
                if (previous == null)
                {
                    new ConfigurationFailureException(
                            "There is already a validator for data set type '" + dataSetType + "'.");
                }
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Validator for data set type '" + dataSetType + "' defined.");
                }
            } catch (CheckedExceptionTunnel ex)
            {
                throw new ConfigurationFailureException(
                        "Error occured while creating data set validator '"
                                + sectionProperties.getKey() + "': " + ex.toString(),
                        ex.getCause());
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException(
                        "Error occured while creating data set validator '"
                                + sectionProperties.getKey() + "': " + ex.toString(), ex);
            }
        }
    }

    private IDataSetValidator createValidator(Properties validatorProperties)
    {
        String validatorClass = validatorProperties.getProperty(VALIDATOR_KEY);
        if (validatorClass == null)
        {
            return new DataSetValidatorForTSV(validatorProperties);
        }
        return ClassUtils.create(IDataSetValidator.class, validatorClass, validatorProperties);
    }

    @Override
    public void assertValidDataSet(DataSetType dataSetType, File incomingDataSetFileOrFolder)
    {
        IDataSetValidator validator = validators.get(dataSetType.getCode());
        if (validator != null)
        {
            try
            {
                validator.assertValidDataSet(dataSetType, incomingDataSetFileOrFolder);
            } catch (Exception ex)
            {
                throw new UserFailureException("Data set of type '" + dataSetType.getCode()
                        + "' is invalid: " + ex.getMessage(), ex);
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Data set [" + incomingDataSetFileOrFolder + "] of type '"
                        + dataSetType.getCode() + "' successfully validated.");
            }
        }
    }

    @Override
    public void assertValidDataSet(DataSetType dataSetType, Reader reader, String dataSourceName)
    {
        IDataSetValidator validator = validators.get(dataSetType.getCode());
        if (validator != null)
        {
            try
            {
                validator.assertValidDataSet(dataSetType, reader, dataSourceName);
            } catch (Exception ex)
            {
                throw new UserFailureException("Data set of type '" + dataSetType.getCode()
                        + "' is invalid: " + ex.getMessage(), ex);
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Data set [" + dataSourceName + "] of type '"
                        + dataSetType.getCode() + "' successfully validated.");
            }
        }
    }
}
