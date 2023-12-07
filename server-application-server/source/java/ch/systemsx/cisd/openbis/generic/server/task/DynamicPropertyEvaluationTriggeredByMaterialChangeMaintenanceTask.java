/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationSchedulerWithQueue;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.SupportedDateTimePattern;

/**
 * Maintenance task for re-evaluation of dynamic properties of entities which have material properties which have changed since the last run of the
 * task. This is also true if the material properties of the material properties have changed.
 * <p>
 * Currently only samples are supported.
 * 
 * @author Franz-Josef Elmer
 */
public class DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask.class);

    static final String TIMESTAMP_FILE_KEY = "timestamp-file";

    static final String DEFAULT_TIMESTAMP_FILE = "../../../data/"
            + DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask.class.getSimpleName() + "-timestamp.txt";

    static final String INITIAL_TIMESTAMP_KEY = "initial-timestamp";

    private final ICommonServerForInternalUse server;

    private final IDynamicPropertyEvaluationScheduler scheduler;

    private final ITimeProvider timeProvider;

    private File timestampFile;

    private String initialTimestamp;

    public DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask()
    {
        this(CommonServiceProvider.getCommonServer(), CommonServiceProvider.getDAOFactory().getPersistencyResources()
                .getDynamicPropertyEvaluationScheduler(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    DynamicPropertyEvaluationTriggeredByMaterialChangeMaintenanceTask(ICommonServerForInternalUse server,
            IDynamicPropertyEvaluationScheduler scheduler, ITimeProvider timeProvider)
    {
        this.server = server;
        this.scheduler = scheduler;
        this.timeProvider = timeProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        timestampFile = new File(properties.getProperty(TIMESTAMP_FILE_KEY, DEFAULT_TIMESTAMP_FILE));
        initialTimestamp = PropertyUtils.getMandatoryProperty(properties, INITIAL_TIMESTAMP_KEY);
    }

    @Override
    public void execute()
    {
        String timestamp = getTimestamp();
        SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
        if (contextOrNull == null)
        {
            operationLog.warn("Couldn't authenticate as system.");
            return;
        }
        String sessionToken = contextOrNull.getSessionToken();
        try
        {
            String newTimestamp = createTimestamp();
            Collection<TechId> allMaterialIds = getAllChangedMaterials(sessionToken, timestamp);
            if (allMaterialIds.isEmpty() == false)
            {
                List<Sample> samples = server.listSamplesByMaterialProperties(sessionToken, allMaterialIds);
                operationLog.info(samples.size() + " samples found for changed materials.");
                if (samples.isEmpty() == false)
                {
                    List<Long> ids = TechId.asLongs(TechId.createList(samples));
                    DynamicPropertyEvaluationOperation operation = DynamicPropertyEvaluationOperation.evaluate(SamplePE.class, ids);
                    scheduler.scheduleUpdate(operation);
                    if (scheduler instanceof IDynamicPropertyEvaluationSchedulerWithQueue)
                    {
                        ((IDynamicPropertyEvaluationSchedulerWithQueue) scheduler).synchronizeThreadQueue();
                    }
                }
            }
            saveTimestamp(newTimestamp);
        } finally
        {
            server.logout(sessionToken);
        }
    }

    private Collection<TechId> getAllChangedMaterials(String sessionToken, String timestamp)
    {
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        DetailedSearchCriterion criterion =
                new DetailedSearchCriterion(
                        DetailedSearchField
                                .createAttributeField(MaterialAttributeSearchFieldKind.MODIFICATION_DATE),
                        CompareType.MORE_THAN_OR_EQUAL, timestamp);
        criteria.setCriteria(Arrays.asList(criterion));
        criteria.setConnection(SearchCriteriaConnection.MATCH_ALL);
        List<Material> materials = server.searchForMaterials(sessionToken, criteria);
        int numberOfChangedMaterials = materials.size();
        operationLog.info(numberOfChangedMaterials + " materials changed since [" + timestamp + "].");
        Collection<TechId> allMaterialIds = new HashSet<TechId>();
        Collection<TechId> materialIds = TechId.createList(materials);
        while (materialIds.isEmpty() == false)
        {
            materialIds.removeAll(allMaterialIds);
            allMaterialIds.addAll(materialIds);
            materialIds = server.listMaterialIdsByMaterialProperties(sessionToken, materialIds);
        }
        if (numberOfChangedMaterials != allMaterialIds.size())
        {
            operationLog.info(allMaterialIds.size() + " materials in total changed.");
        }
        return allMaterialIds;
    }

    private String getTimestamp()
    {
        String timestamp = null;
        if (timestampFile.isFile())
        {
            timestamp = FileUtilities.loadToString(timestampFile).trim();
            try
            {
                DateUtils.parseDate(timestamp, new String[] { SupportedDateTimePattern.CANONICAL_DATE_PATTERN.getPattern() });
            } catch (ParseException ex)
            {
                operationLog.warn("Invalid timestamp in file '" + timestampFile + "': " + timestamp);
                timestamp = null;
            }
        }
        return timestamp != null ? timestamp : initialTimestamp;
    }

    private String createTimestamp()
    {
        return DateFormatUtils.format(new Date(timeProvider.getTimeInMilliseconds()),
                SupportedDateTimePattern.CANONICAL_DATE_PATTERN.getPattern());
    }

    private void saveTimestamp(String newTimestamp)
    {
        timestampFile.getParentFile().mkdirs();
        FileUtilities.writeToFile(timestampFile, newTimestamp);
        operationLog.info("Timestamp [" + newTimestamp + "] saved in '" + timestampFile + "'.");
    }

}
