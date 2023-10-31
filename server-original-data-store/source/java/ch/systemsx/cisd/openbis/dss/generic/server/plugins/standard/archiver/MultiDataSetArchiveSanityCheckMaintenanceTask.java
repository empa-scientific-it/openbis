/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import net.lemnik.eodsql.QueryTool;

public class MultiDataSetArchiveSanityCheckMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MultiDataSetArchiveSanityCheckMaintenanceTask.class);

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    private static final String CHECK_FROM_DATE_KEY = "check-from-date";

    private static final String CHECK_TO_DATE_KEY = "check-to-date";

    private static final String CHECK_IN_RANDOM_ORDER_KEY = "check-in-random-order";

    private static final boolean CHECK_IN_RANDOM_ORDER_DEFAULT = false;

    private static final String RUN_PROBABILITY_KEY = "run-probability";

    private static final double RUN_PROBABILITY_DEFAULT = 1.0;

    private static final String RUN_MAX_RANDOM_DELAY_KEY = "run-max-random-delay";

    private static final long RUN_MAX_RANDOM_DELAY_DEFAULT = 0L;

    private static final String RUN_SIZE = "run-size";

    private static final int RUN_SIZE_DEFAULT = -1;

    private static final String NOTIFY_EMAILS_KEY = "notify-emails";

    private static final String STATUS_FILE_KEY = "status-file";

    private Date checkFromDate;

    private Date checkToDate;

    private boolean checkInRandomOrder;

    private boolean verifyChecksums;

    private double runProbability;

    private long runMaxRandomDelay;

    private int runSize;

    private List<String> notifyEmails;

    private String statusFile;

    private IMultiDataSetArchiverReadonlyQueryDAO multiDataSetDAO;

    private IPathsInfoDAO pathInfoDAO;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public void setUp(final String pluginName, final Properties properties)
    {
        checkFromDate = getDateProperty(properties, CHECK_FROM_DATE_KEY);
        checkToDate = getDateProperty(properties, CHECK_TO_DATE_KEY);
        checkInRandomOrder = PropertyUtils.getBoolean(properties, CHECK_IN_RANDOM_ORDER_KEY, CHECK_IN_RANDOM_ORDER_DEFAULT);

        Properties archiverProperties = ServiceProvider.getDataStoreService().getArchiverProperties();
        verifyChecksums = PropertyUtils.getBoolean(archiverProperties, MultiDataSetArchiver.SANITY_CHECK_VERIFY_CHECKSUMS_KEY,
                MultiDataSetArchiver.DEFAULT_SANITY_CHECK_VERIFY_CHECKSUMS);

        runProbability = PropertyUtils.getDouble(properties, RUN_PROBABILITY_KEY, RUN_PROBABILITY_DEFAULT);
        if (runProbability <= 0 || runProbability > 1.0)
        {
            throw new IllegalArgumentException(
                    "'" + RUN_PROBABILITY_KEY + "' must be greater than 0 and less than or equal 1 (i.e. must be in range (0,1]).");
        }
        runMaxRandomDelay = DateTimeUtils.getDurationInMillis(properties, RUN_MAX_RANDOM_DELAY_KEY, RUN_MAX_RANDOM_DELAY_DEFAULT);
        runSize = PropertyUtils.getInt(properties, RUN_SIZE, RUN_SIZE_DEFAULT);
        if (runSize <= 0 && runSize != -1)
        {
            throw new IllegalArgumentException("'" + RUN_SIZE + "' must be greater than 0 or equal -1 (i.e. check all).");
        }
        notifyEmails = PropertyUtils.getMandatoryList(properties, NOTIFY_EMAILS_KEY);
        statusFile = PropertyUtils.getMandatoryProperty(properties, STATUS_FILE_KEY);

        multiDataSetDAO = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        pathInfoDAO = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }

    @Override public void execute()
    {
        operationLog.info("Starting consistency check task");

        if (runProbability < 1.0)
        {
            boolean execute = Math.random() < runProbability;
            operationLog.info("The check is configured to run with " + runProbability + " probability. " + (execute ?
                    "This time the run will be executed." : "This time the run will be skipped."));
            if (!execute)
            {
                return;
            }
        }

        if (runMaxRandomDelay > 0)
        {
            long runRandomDelay = (long) (Math.random() * runMaxRandomDelay);
            operationLog.info("The check is configured to be run with a maximum random delay of " + DateTimeUtils.renderDuration(runMaxRandomDelay)
                    + ". Delay used for this run: " + DateTimeUtils.renderDuration(runRandomDelay));
            ConcurrencyUtilities.sleep(runRandomDelay);
            operationLog.info("The random delay of " + DateTimeUtils.renderDuration(runRandomDelay) + " used for this run is over.");
        }

        List<MultiDataSetArchiverContainerDTO> containers = null;

        if (checkInRandomOrder)
        {
            operationLog.info("Loading containers to check in a random order.");
            containers = multiDataSetDAO.listContainersInRandomOrder();
        } else
        {
            operationLog.info("Loading containers to check in a chronological order.");
            containers = multiDataSetDAO.listContainersInChronologicalOrder();
        }

        CheckStatuses statuses = loadCheckStatuses();
        int counter = 0;

        for (MultiDataSetArchiverContainerDTO container : containers)
        {
            if (runSize != -1 && counter >= runSize)
            {
                operationLog.info("Reached the configured run size - already checked " + counter + " container(s). Finishing the run.");
                break;
            }

            CheckStatus status = statuses.getStatus(container.getPath());

            if (status == null)
            {
                try
                {
                    operationLog.info("Found container to check '" + container.getPath() + "'");

                    if (shouldCheckConsistency(container))
                    {
                        operationLog.info("Starting consistency check of container '" + container.getPath() + "'");
                        checkConsistency(container);
                        operationLog.info("Finished consistency check of container '" + container.getPath() + "'.");
                        status = CheckStatus.OK;
                        counter++;
                    } else
                    {
                        operationLog.info("Skipping consistency check of container '" + container.getPath() + "'");
                        status = CheckStatus.SKIPPED;
                    }
                } catch (Exception e)
                {
                    operationLog.error("Consistency check of container '" + container.getPath() + "' failed.", e);
                    status = new CheckStatus(true, null, e);
                    counter++;
                }

                if (status.isError())
                {
                    sendEmail(container, status);
                }

                statuses.setStatus(container.getPath(), status);
                saveCheckStatuses(statuses);
            }
        }

        operationLog.info("Finished consistency check task. Checked " + counter + " container(s).");
    }

    private boolean shouldCheckConsistency(final MultiDataSetArchiverContainerDTO container)
    {
        File tar = new File(getOperationsManager().getOriginalArchiveFilePath(container.getPath()));

        if (tar.exists())
        {
            if (checkFromDate != null || checkToDate != null)
            {
                BasicFileAttributes attributes = null;

                try
                {
                    attributes = Files.readAttributes(tar.toPath(), BasicFileAttributes.class);
                } catch (IOException e)
                {
                    throw new RuntimeException("Could not read attributes of tar file '" + tar.getAbsolutePath() + "'", e);
                }

                Date creationDate = new Date(attributes.creationTime().toMillis());

                if (creationDate.getTime() > 0)
                {
                    if ((checkFromDate != null && creationDate.before(checkFromDate)) || (checkToDate != null && creationDate.after(checkToDate)))
                    {
                        return false;
                    }
                } else
                {
                    throw new RuntimeException(
                            "Cannot check if tar file '" + tar.getAbsolutePath() + "' should be verified, because its creation date is 0.", null);
                }
            }

            List<MultiDataSetArchiverDataSetDTO> containerDataSets = multiDataSetDAO.listDataSetsForContainerId(container.getId());

            for (MultiDataSetArchiverDataSetDTO containerDataSet : containerDataSets)
            {
                Long pathInfoDataSetId = pathInfoDAO.tryGetDataSetId(containerDataSet.getCode());

                if (pathInfoDataSetId == null)
                {
                    throw new RuntimeException("Path info database does not have information about data set '" + containerDataSet.getCode()
                            + "' which is part of '" + tar.getAbsolutePath() + "' tar file. Consistency cannot be checked.", null);
                }
            }

            return true;
        } else
        {
            throw new RuntimeException(
                    "Tar path '" + tar.getAbsolutePath() + "' stored in the multi-dataset archiver database points to a file that does not exist.",
                    null);
        }
    }

    private void checkConsistency(final MultiDataSetArchiverContainerDTO container)
    {
        ArchiverTaskContext archiverContext = new ArchiverTaskContext(
                ServiceProvider.getDataStoreService().getDataSetDirectoryProvider(),
                ServiceProvider.getHierarchicalContentProvider());

        MultiDataSetFileOperationsManager operationsManager = getOperationsManager();

        List<MultiDataSetArchiverDataSetDTO> containerDataSets = multiDataSetDAO.listDataSetsForContainerId(container.getId());
        List<DatasetDescription> containerDataSetDescriptions = convertToDescriptions(containerDataSets);

        IHierarchicalContent mainContent = null;

        try
        {
            mainContent = operationsManager.getContainerAsHierarchicalContent(container.getPath(), containerDataSetDescriptions);
            MultiDataSetArchivingUtils.sanityCheck(mainContent, containerDataSetDescriptions, verifyChecksums, archiverContext,
                    new Log4jSimpleLogger(operationLog));
        } catch (Exception e)
        {
            throw new RuntimeException("Sanity check of the main copy of failed", e);
        } finally
        {
            if (mainContent != null)
            {
                mainContent.close();
            }
        }

        IHierarchicalContent replicaContent = null;
        try
        {
            replicaContent =
                    operationsManager.getReplicaAsHierarchicalContent(container.getPath(), containerDataSetDescriptions);
            MultiDataSetArchivingUtils.sanityCheck(replicaContent, containerDataSetDescriptions, verifyChecksums, archiverContext,
                    new Log4jSimpleLogger(operationLog));
        } catch (Exception e)
        {
            throw new RuntimeException("Sanity check of the replica copy failed", e);
        } finally
        {
            if (replicaContent != null)
            {
                replicaContent.close();
            }
        }
    }

    private MultiDataSetFileOperationsManager getOperationsManager()
    {
        Properties archiverProperties = ServiceProvider.getDataStoreService().getArchiverProperties();

        return new MultiDataSetFileOperationsManager(
                archiverProperties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                new SimpleFreeSpaceProvider(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    private List<DatasetDescription> convertToDescriptions(List<MultiDataSetArchiverDataSetDTO> dataSets)
    {
        List<DatasetDescription> list = new ArrayList<>();
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            AbstractExternalData externalData = ServiceProvider.getOpenBISService().tryGetDataSet(dataSet.getCode());
            if (externalData == null)
            {
                throw new RuntimeException("Data set '" + dataSet.getCode() + "' was not found in the main openBIS database.");
            }
            DatasetDescription datasetDescription = DataSetTranslator.translateToDescription(externalData);
            list.add(datasetDescription);
        }
        return list;
    }

    private static Date getDateProperty(Properties properties, String propertyKey)
    {
        String value = PropertyUtils.getProperty(properties, propertyKey);

        if (value != null)
        {
            try
            {
                return DATE_FORMAT.parse(value);
            } catch (Exception e)
            {
                throw new ConfigurationFailureException(
                        "Could not parse property '" + propertyKey + "' to date. Property value '" + value + "'. Expected date format '"
                                + DATE_FORMAT_PATTERN
                                + "'");
            }
        } else
        {
            return null;
        }
    }

    private CheckStatuses loadCheckStatuses()
    {
        File file = new File(statusFile);

        if (file.exists())
        {
            try
            {
                CheckStatuses statuses = mapper.readValue(file, MultiDataSetArchiveSanityCheckMaintenanceTask.CheckStatuses.class);
                operationLog.info("Check statuses successfully loaded. File path: " + file.getAbsolutePath());
                return statuses;
            } catch (Exception e)
            {
                operationLog.error("Loading of check statuses failed. File path: " + file.getAbsolutePath(), e);
            }
        }

        return new CheckStatuses();
    }

    private void saveCheckStatuses(CheckStatuses statuses)
    {
        File file = new File(statusFile);

        try
        {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, statuses);
        } catch (Exception e)
        {
            operationLog.error("Saving of check statuses failed. File path: " + file.getAbsolutePath(), e);
        }
    }

    private void sendEmail(final MultiDataSetArchiverContainerDTO container, final CheckStatus status)
    {
        if (notifyEmails.isEmpty())
        {
            operationLog.info("List of emails to notify is empty. Skipping the email sending.");
            return;
        }

        IMailClient mailClient = ServiceProvider.getDataStoreService().createEMailClient();

        StringBuilder content = new StringBuilder("Consistency check for container '" + container.getPath() + "' failed with error:");

        if (status.getMessage() != null)
        {
            content.append("\n").append(status.getMessage());
        }
        if (status.getStackTrace() != null)
        {
            content.append("\n").append(status.getStackTrace());
        }

        List<EMailAddress> emails = notifyEmails.stream().map(EMailAddress::new).collect(Collectors.toList());

        try
        {
            mailClient.sendEmailMessage("Multi dataset archive consistency check failed", content.toString(), null, null,
                    emails.toArray(new EMailAddress[] {}));
        } catch (Exception e)
        {
            operationLog.warn("Sending of email to: " + emails + " failed.", e);
        }
    }

    @JsonObject("CheckStatuses")
    private static class CheckStatuses
    {

        @JsonProperty
        private final Map<String, CheckStatus> statuses = new LinkedHashMap<>();

        public CheckStatus getStatus(final String containerPath)
        {
            return statuses.get(containerPath);
        }

        public void setStatus(final String containerPath, CheckStatus status)
        {
            statuses.put(containerPath, status);
        }

        public Map<String, CheckStatus> getStatuses()
        {
            return statuses;
        }
    }

    @JsonObject("CheckStatus")
    private static class CheckStatus
    {
        public static final CheckStatus OK = new CheckStatus(false, "OK", null);

        public static final CheckStatus SKIPPED = new CheckStatus(false, "SKIPPED", null);

        @JsonProperty
        private boolean error;

        @JsonProperty
        private String message;

        @JsonProperty
        private String stackTrace;

        private CheckStatus()
        {
        }

        public CheckStatus(boolean error, String message, Exception exception)
        {
            this.error = error;
            this.message = message;
            this.stackTrace = exception != null ? ExceptionUtils.getStackTrace(exception) : null;
        }

        public boolean isError()
        {
            return error;
        }

        public String getMessage()
        {
            return message;
        }

        public String getStackTrace()
        {
            return stackTrace;
        }

    }

}
