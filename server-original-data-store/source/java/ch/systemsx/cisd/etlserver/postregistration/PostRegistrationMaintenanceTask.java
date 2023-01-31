/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Maintenance task performing {@link IPostRegistrationTask}s.
 * 
 * @author Franz-Josef Elmer
 */
public class PostRegistrationMaintenanceTask implements IDataStoreLockingMaintenanceTask
{
    @Private
    static final String POST_REGISTRATION_TASKS_PROPERTY = "post-registration-tasks";

    @Private
    static final String IGNORE_DATA_SETS = "ignore-data-sets-before-date";

    @Private
    static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";

    private static final String DEFAULT_LAST_SEEN_DATA_SET_FILE = "last-seen-data-set.txt";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PostRegistrationMaintenanceTask.class);

    private IEncapsulatedOpenBISService service;

    private boolean needsLockOnDataStore;

    private Set<Entry<String, IPostRegistrationTask>> tasks;

    private File lastSeenDataSetFile;

    private File newLastSeenDataSetFile;

    private Date ignoreBeforeDate;

    private TaskExecutor executor;

    @Override
    public boolean requiresDataStoreLock()
    {
        return needsLockOnDataStore;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        //
        // NB Changes to this method need to be reflected in setUpEmpty as well!
        //
        service = ServiceProvider.getOpenBISService();
        // Linked hash map because the task should be executed in the order of definition.
        Map<String, IPostRegistrationTask> map = new LinkedHashMap<String, IPostRegistrationTask>();
        SectionProperties[] sectionProperties =
                PropertyParametersUtil.extractSectionProperties(properties,
                        POST_REGISTRATION_TASKS_PROPERTY, false);
        for (SectionProperties sectionProperty : sectionProperties)
        {
            Properties taskProperties = sectionProperty.getProperties();
            String className = PropertyUtils.getMandatoryProperty(taskProperties, "class");
            IPostRegistrationTask task =
                    ClassUtils.create(IPostRegistrationTask.class, className, taskProperties,
                            service);
            if (task.requiresDataStoreLock())
            {
                needsLockOnDataStore = true;
            }
            map.put(sectionProperty.getKey(), task);
        }
        tasks = map.entrySet();
        executor = new TaskExecutor(properties, operationLog);
        String fileName =
                properties.getProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY,
                        DEFAULT_LAST_SEEN_DATA_SET_FILE);
        lastSeenDataSetFile = new File(fileName);
        newLastSeenDataSetFile = new File(fileName + ".new");
        String property = properties.getProperty(IGNORE_DATA_SETS);
        if (property == null)
        {
            ignoreBeforeDate = new Date(0);
        } else
        {
            try
            {
                ignoreBeforeDate = DateUtils.parseDate(property, new String[]
                { "yyyy-MM-dd" });
            } catch (ParseException ex)
            {
                throw new ConfigurationFailureException("Invalid value of property '"
                        + IGNORE_DATA_SETS + "': " + property);
            }
        }
    }

    /**
     * The PostRegistrationMaintenanceTask is a bit exceptional since it needs to be run even if it is not configured by the user. This method is used
     * to set it up in that case.
     */
    public void setUpEmpty()
    {
        service = ServiceProvider.getOpenBISService();
        // Linked hash map because the task should be executed in the order of definition.
        Map<String, IPostRegistrationTask> map = new LinkedHashMap<String, IPostRegistrationTask>();
        needsLockOnDataStore = false;
        tasks = map.entrySet();
        executor = new TaskExecutor(new Properties(), operationLog);
        String fileName = DEFAULT_LAST_SEEN_DATA_SET_FILE;
        lastSeenDataSetFile = new File(fileName);
        newLastSeenDataSetFile = new File(fileName + ".new");
        ignoreBeforeDate = new Date(0);
    }

    @Override
    public void execute()
    {
        executor.cleanup();

        // check if there is any dataset for wich post-registration task has been executed, but has
        // not been marked as such in the service.
        // If there is one we send information to the service and delete the marker file.
        if (lastSeenDataSetFile.exists())
        {
            String lastRegisteredCode =
                    FileUtilities.loadToString(lastSeenDataSetFile).trim();
            service.markSuccessfulPostRegistration(lastRegisteredCode);
            deleteLastSeenDataSetId();
        }

        List<AbstractExternalData> dataSets = getDataSetsForPostRegistration();

        for (int i = 0; i < dataSets.size(); i++)
        {
            AbstractExternalData dataSet = dataSets.get(i);
            String code = dataSet.getCode();
            operationLog.info("Post registration of " + (i + 1) + ". of " + dataSets.size()
                    + " data sets: " + code);
            try
            {
                for (Entry<String, IPostRegistrationTask> entry : tasks)
                {
                    IPostRegistrationTask task = entry.getValue();
                    task.clearCache();
                    String taskName = entry.getKey();
                    executor.execute(task, taskName, code, dataSet.isContainer());
                }

                // After succesful registration the information is send to the service.
                // To ensure we won't process the same dataset twice we create a marker file with
                // the dataset ID, and delete it only after the service call succeeded.
                // We won't start processing new datasets if the marker file is not deleted.
                saveLastSeenDataSetId(code);
                service.markSuccessfulPostRegistration(code);
                deleteLastSeenDataSetId();

            } catch (Throwable ex)
            {
                operationLog.error("Post registration failed.", ex);
                logPostponingMessage(dataSets, i);
                break;
            }
        }
    }

    /**
     * @return List of datasets to process in post registration. Sorted by Id, incrementally.
     */
    private List<AbstractExternalData> getDataSetsForPostRegistration()
    {
        List<AbstractExternalData> dataSets = service.listDataSetsForPostRegistration();
        List<AbstractExternalData> filteredList = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData dataSet : dataSets)
        {
            if (dataSet.getRegistrationDate().getTime() > ignoreBeforeDate.getTime())
            {
                filteredList.add(dataSet);
            }
        }
        Collections.sort(filteredList, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData o1, AbstractExternalData o2)
                {
                    return (int) (o1.getId() - o2.getId());
                }
            });
        return filteredList;
    }

    /**
     * Store locally the ID of the dataset, wich has just been processed.
     */
    private void saveLastSeenDataSetId(String lastRegisteredDataSetCode)
    {
        FileUtilities.writeToFile(newLastSeenDataSetFile, lastRegisteredDataSetCode);
        newLastSeenDataSetFile.renameTo(lastSeenDataSetFile);
    }

    /**
     * Delete the information about recently processed dataset.
     */
    private void deleteLastSeenDataSetId()
    {
        lastSeenDataSetFile.delete();
    }

    private void logPostponingMessage(List<AbstractExternalData> dataSets, int i)
    {
        int numberOfDataSets = dataSets.size();
        if (i < numberOfDataSets - 1)
        {
            List<String> codes = new ArrayList<String>();
            for (int j = i + 1; j < numberOfDataSets; j++)
            {
                codes.add(dataSets.get(j).getCode());
            }

            operationLog.error("Because post registration task failed for data set "
                    + dataSets.get(i).getCode() + " post registration tasks are postponed for "
                    + "the following data sets: " + CollectionUtils.abbreviate(codes, 30));
        }
    }

}
