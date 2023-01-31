/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * {@link IDataSetInfoExtractor} which delegates all the tasks to the extractor specified in configuration with
 * {@link #DELEGATE_EXTRACTOR_CLASS_PROPERTY} property.
 * <p>
 * This class is supposed to be extended to add specific functionality which has to be performed besides the basic operations.
 * </p>
 * 
 * @author Piotr Buczek
 */
abstract public class AbstractDelegatingDataSetInfoExtractor implements IDataSetInfoExtractor
{
    /**
     * Property name which is used to specify the class of the default storage processor, to which all calls are delegated.
     */
    protected final static String DELEGATE_EXTRACTOR_CLASS_PROPERTY = "extractor";

    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION,
                    AbstractDelegatingDataSetInfoExtractor.class);

    private final IDataSetInfoExtractor delegate;

    protected AbstractDelegatingDataSetInfoExtractor(Properties properties)
    {
        this(createDelegate(properties));
    }

    protected AbstractDelegatingDataSetInfoExtractor(IDataSetInfoExtractor delegate)
    {
        this.delegate = delegate;
    }

    @Private
    static IDataSetInfoExtractor createDelegate(Properties properties)
    {
        String delegateClass =
                PropertyUtils.getMandatoryProperty(properties, DELEGATE_EXTRACTOR_CLASS_PROPERTY);
        Properties p =
                ExtendedProperties.getSubset(properties, DELEGATE_EXTRACTOR_CLASS_PROPERTY + ".",
                        true);
        return createClass(IDataSetInfoExtractor.class, delegateClass, p);
    }

    private final static <T> T createClass(final Class<T> superClazz, String className,
            Object... argumentsOrNull)
    {
        try
        {
            return ClassUtils.create(superClazz, className, argumentsOrNull);
        } catch (IllegalArgumentException ex)
        {
            throw new ConfigurationFailureException(ex.getMessage());
        }
    }

    //
    // delegation
    //

    @Override
    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        return delegate.getDataSetInformation(incomingDataSetPath, openbisService);
    }

}
