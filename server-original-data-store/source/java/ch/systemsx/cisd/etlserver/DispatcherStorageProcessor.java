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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Storage processor which dispatches the tasks between defined dispatchable storage processors specified in configuration with
 * {@link #DISPATCHER_PROCESSORS_LIST_PROPERTY} property. A dataset storage processor is dispatchable only if it implements
 * {@link IDispatchableStorageProcessor}!
 * <p>
 * If more than one storage processor accepts a dataset, the first one which is defined in the configuration will be used.
 * 
 * @author Tomasz Pylak
 */
public class DispatcherStorageProcessor extends AbstractStorageProcessor
{
    /**
     * A storage processor can be used by the {@link DispatcherStorageProcessor} only if extends this interface.
     */
    public static interface IDispatchableStorageProcessor extends IStorageProcessorTransactional
    {
        /** @return true if the dataset should be processed by this storage processor. */
        boolean accepts(DataSetInformation dataSetInformation, File incomingDataSet);
    }

    /**
     * Property name which is used to specify list of storage processors names. All of them should implement {@link IDispatchableStorageProcessor}.
     */
    protected final static String DISPATCHER_PROCESSORS_LIST_PROPERTY = "processors";

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DispatcherStorageProcessor.class);

    private final List<IDispatchableStorageProcessor> delegates;

    public DispatcherStorageProcessor(Properties properties)
    {
        this(createDispatcherStorageProcessors(properties), properties);
    }

    @Private
    protected DispatcherStorageProcessor(List<IDispatchableStorageProcessor> delegates,
            Properties properties)
    {
        super(properties);
        this.delegates = delegates;
    }

    private IStorageProcessorTransactional chooseStorageProcessor(
            DataSetInformation dataSetInformation,
            File incomingDataSet)
    {
        for (IDispatchableStorageProcessor storageProcessor : delegates)
        {
            if (storageProcessor.accepts(dataSetInformation, incomingDataSet))
            {
                return storageProcessor;
            }
        }
        throw new ConfigurationFailureException(
                "No storage processor appropriate for the dataset has been found: "
                        + dataSetInformation);
    }

    @Private
    static List<IDispatchableStorageProcessor> createDispatcherStorageProcessors(
            Properties properties)
    {
        List<IDispatchableStorageProcessor> delegates =
                new ArrayList<IDispatchableStorageProcessor>();
        SectionProperties[] allSectionsProperties =
                PropertyParametersUtil.extractSectionProperties(properties,
                        DISPATCHER_PROCESSORS_LIST_PROPERTY, true);
        for (SectionProperties section : allSectionsProperties)
        {
            Properties sectionProperties = section.getProperties();
            String delegateClass = PropertyUtils.getMandatoryProperty(properties, section.getKey());
            IDispatchableStorageProcessor storageProcessor =
                    ClassUtils.create(IDispatchableStorageProcessor.class, delegateClass,
                            sectionProperties);
            delegates.add(storageProcessor);
        }
        return delegates;
    }

    // --- dispatcher implementation

    @Override
    public IStorageProcessorTransaction createTransaction(final StorageProcessorTransactionParameters transactionParameters)
    {
        final IStorageProcessorTransactional storageProcessor =
                chooseStorageProcessor(transactionParameters.getDataSetInformation(), transactionParameters.getIncomingDataSetDirectory());
        return storageProcessor.createTransaction(transactionParameters);
    }

}
