/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.etlserver.IArchiveCandidateDiscoverer;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;

/**
 * {@link IMaintenanceTask} performing automatic archiving of data sets.
 * <p>
 * TODO KE: implement as asynchronous maintenance task
 * 
 * @author Piotr Buczek
 */
public class AutoArchiverTask implements IMaintenanceTask
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AutoArchiverTask.class);

    private static final String POLICY_SECTION_NAME = "policy";

    private static final String DISCOVERY_SECTION_NAME = "archive-candidate-discoverer";

    private static final String CLASS_PROPERTY_NAME = "class";

    private static final String DATA_SET_TYPE_PROPERTY_NAME = "data-set-type";

    private static final String OLDER_THAN_PROPERTY_NAME = "older-than";

    private static final String REMOVE_DATASETS_FROM_STORE = "remove-datasets-from-store";

    private static final int DEFAULT_OLDER_THAN = 30;

    private IEncapsulatedOpenBISService openBISService;

    private IAutoArchiverPolicy policy;

    private IArchiveCandidateDiscoverer archiveCandidateDiscoverer;

    private ArchiverDataSetCriteria criteria;

    private boolean removeFromDataStore;

    @Override
    public void execute()
    {
        List<AbstractExternalData> candidates = archiveCandidateDiscoverer.findDatasetsForArchiving(openBISService, criteria);
        if (candidates.isEmpty())
        {
            operationLog.info("nothing to archive");
            return;
        }
        operationLog.info("apply policy to " + candidates.size() + " candidates for archiving.");
        List<AbstractExternalData> dataSets = policy.filter(candidates);
        if (dataSets.isEmpty())
        {
            operationLog.info("nothing to archive");
        } else
        {
            operationLog.info("archiving: "
                    + CollectionUtils.abbreviate(Code.extractCodes(dataSets), 10));
            openBISService.archiveDataSets(Code.extractCodes(dataSets), removeFromDataStore, new HashMap<>());
        }
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        openBISService = ServiceProvider.getOpenBISService();
        criteria = createCriteria(properties);
        SectionProperties policySectionProperties =
                PropertyParametersUtil.extractSingleSectionProperties(properties,
                        POLICY_SECTION_NAME, false);
        policy = createPolicyInstance(policySectionProperties);

        SectionProperties discoverySectionProperties =
                PropertyParametersUtil.extractSingleSectionProperties(properties,
                        DISCOVERY_SECTION_NAME, false);
        archiveCandidateDiscoverer = createArchiveDatasetDiscoverer(discoverySectionProperties);

        removeFromDataStore =
                PropertyUtils.getBoolean(properties, REMOVE_DATASETS_FROM_STORE, false);

        operationLog.info("Plugin " + pluginName + " initialized");
    }

    private ArchiverDataSetCriteria createCriteria(Properties properties)
    {
        String dataSetTypeCodeOrNull = properties.getProperty(DATA_SET_TYPE_PROPERTY_NAME);
        int olderThan =
                PropertyUtils.getInt(properties, OLDER_THAN_PROPERTY_NAME, DEFAULT_OLDER_THAN);
        return new ArchiverDataSetCriteria(olderThan, dataSetTypeCodeOrNull, false);
    }

    private IArchiveCandidateDiscoverer createArchiveDatasetDiscoverer(SectionProperties discoverySectionProperties)
    {
        String className = discoverySectionProperties.getProperties().getProperty(CLASS_PROPERTY_NAME);
        if (className == null)
        {
            return new AgeArchiveCandidateDiscoverer(discoverySectionProperties.getProperties());
        }

        return createInstance(discoverySectionProperties, className, IArchiveCandidateDiscoverer.class);
    }

    private IAutoArchiverPolicy createPolicyInstance(SectionProperties policySectionProperties)
    {
        String className = policySectionProperties.getProperties().getProperty(CLASS_PROPERTY_NAME);
        if (className == null)
        {
            return DummyAutoArchiverPolicy.INSTANCE;
        }

        return createInstance(policySectionProperties, className, IAutoArchiverPolicy.class);
    }

    private static <T> T createInstance(SectionProperties constructorArguments, String className,
            Class<T> interfaceToCreate)
    {
        try
        {
            return ClassUtils.create(interfaceToCreate, className, constructorArguments
                    .getProperties());
        } catch (ConfigurationFailureException ex)
        {
            throw ex; // rethrow the exception without changing the message
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

}
