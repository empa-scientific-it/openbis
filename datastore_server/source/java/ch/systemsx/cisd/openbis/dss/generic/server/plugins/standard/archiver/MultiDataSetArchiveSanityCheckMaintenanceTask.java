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

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import net.lemnik.eodsql.QueryTool;

public class MultiDataSetArchiveSanityCheckMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MultiDataSetArchiveSanityCheckMaintenanceTask.class);

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    private static final String CHECK_FROM_DATE_KEY = "check-from-date";

    private static final String CHECK_TO_DATE_KEY = "check-to-date";

    private static final String NOTIFY_EMAILS_KEY = "notify-emails";

    private static final String STATUS_FILE_KEY = "status-file";

    private Date checkFromDate;

    private Date checkToDate;

    private List<String> notifyEmails;

    private String statusFile;

    private IMultiDataSetArchiverReadonlyQueryDAO multiDataSetDAO;

    private IPathsInfoDAO pathInfoDAO;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public void setUp(final String pluginName, final Properties properties)
    {
        checkFromDate = getMandatoryDateProperty(properties, CHECK_FROM_DATE_KEY);
        checkToDate = getMandatoryDateProperty(properties, CHECK_TO_DATE_KEY);
        notifyEmails = PropertyUtils.getMandatoryList(properties, NOTIFY_EMAILS_KEY);
        statusFile = PropertyUtils.getMandatoryProperty(properties, STATUS_FILE_KEY);

        multiDataSetDAO = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        pathInfoDAO = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }

    @Override public void execute()
    {
        CheckStatuses existingStatuses = loadCheckStatuses();
        CheckStatuses newStatuses = new CheckStatuses();

        for (MultiDataSetArchiverContainerDTO container : multiDataSetDAO.listContainers())
        {
            CheckStatus status = existingStatuses.getStatus(container.getPath());

            if (status == null)
            {
                CheckStatus newStatus = new CheckStatus();

                try
                {
                    if (shouldCheckConsistency(newStatus, container))
                    {
                        checkConsistency(newStatus, container);
                    }
                } catch (Exception e)
                {
                    operationLog.error("Consistency check task failed unexpectedly", e);
                    newStatus.addError("Consistency check task failed unexpectedly", e);
                } finally
                {
                    newStatuses.setStatus(container.getPath(), newStatus);
                }
            }
        }

        sendEmail(newStatuses);

        existingStatuses.addStatuses(newStatuses);
        saveCheckStatuses(existingStatuses);
    }

    private boolean shouldCheckConsistency(final CheckStatus status, final MultiDataSetArchiverContainerDTO container)
    {
        File tar = new File(container.getPath());

        if (tar.exists())
        {
            BasicFileAttributes attributes = null;

            try
            {
                attributes = Files.readAttributes(tar.toPath(), BasicFileAttributes.class);
            } catch (IOException e)
            {
                status.addError("Could not read attributes of tar file '" + tar.getAbsolutePath() + "'", e);
                return false;
            }

            Date creationDate = new Date(attributes.creationTime().toMillis());

            if (creationDate.getTime() > 0)
            {
                if (checkFromDate.before(creationDate) && creationDate.before(checkToDate))
                {
                    List<MultiDataSetArchiverDataSetDTO> containerDataSets = multiDataSetDAO.listDataSetsForContainerId(container.getId());

                    for (MultiDataSetArchiverDataSetDTO containerDataSet : containerDataSets)
                    {
                        Long pathInfoDataSetId = pathInfoDAO.tryGetDataSetId(containerDataSet.getCode());

                        if (pathInfoDataSetId == null)
                        {
                            status.addError("Path info database does not have information about data set '" + containerDataSet.getCode()
                                    + "' which is part of '" + tar.getAbsolutePath() + "' tar file. Consistency cannot be checked.", null);
                            return false;
                        }
                    }

                    status.addInfo("Tar file '" + tar.getAbsolutePath() + "' created on '" + DATE_FORMAT.format(creationDate)
                            + "' qualified for the check. Path info database contains information about all of its data sets.");
                    return true;
                } else
                {
                    return false;
                }
            } else
            {
                status.addError("Cannot check if tar file '" + tar.getAbsolutePath() + "' should be verified, because its creation date is 0.", null);
                return false;
            }
        } else
        {
            status.addError("Tar path '" + tar.getAbsolutePath()
                    + "' stored in the multi-dataset archiver database points to a file that does not exist.", null);
            return false;
        }
    }

    private void checkConsistency(final CheckStatus status, final MultiDataSetArchiverContainerDTO container)
    {
        ArchiverTaskContext archiverContext = new ArchiverTaskContext(
                ServiceProvider.getDataStoreService().getDataSetDirectoryProvider(),
                ServiceProvider.getHierarchicalContentProvider());

        Properties archiverProperties = ServiceProvider.getDataStoreService().getArchiverProperties();

        MultiDataSetFileOperationsManager operationsManager = new MultiDataSetFileOperationsManager(
                archiverProperties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory(),
                new SimpleFreeSpaceProvider(), SystemTimeProvider.SYSTEM_TIME_PROVIDER);

        List<MultiDataSetArchiverDataSetDTO> containerDataSets = multiDataSetDAO.listDataSetsForContainerId(container.getId());
        List<DatasetDescription> containerDataSetDescriptions = convertToDescriptions(containerDataSets);

        IHierarchicalContent mainContent = null;

        try
        {
            mainContent = operationsManager.getContainerAsHierarchicalContent(container.getPath(), containerDataSetDescriptions);
            MultiDataSetArchivingUtils.sanityCheck(mainContent, containerDataSetDescriptions, archiverContext, new Log4jSimpleLogger(operationLog));
        } catch (Exception e)
        {
            status.addError("Sanity check of the main copy failed", e);
            return;
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
            MultiDataSetArchivingUtils.sanityCheck(replicaContent, containerDataSetDescriptions, archiverContext,
                    new Log4jSimpleLogger(operationLog));
        } catch (Exception e)
        {
            status.addError("Sanity check of the replica copy failed", e);
            return;
        } finally
        {
            if (replicaContent != null)
            {
                replicaContent.close();
            }
        }

        status.addInfo("Consistency check passed for both the main copy and the replica copies.");
    }

    private List<DatasetDescription> convertToDescriptions(List<MultiDataSetArchiverDataSetDTO> dataSets)
    {
        List<DatasetDescription> list = new ArrayList<>();
        for (MultiDataSetArchiverDataSetDTO dataSet : dataSets)
        {
            DatasetDescription description = new DatasetDescription();
            description.setDataSetCode(dataSet.getCode());
            list.add(description);
        }
        return list;
    }

    private static Date getMandatoryDateProperty(Properties properties, String propertyKey)
    {
        String value = PropertyUtils.getMandatoryProperty(properties, propertyKey);
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

    private void sendEmail(CheckStatuses statuses)
    {
        if (notifyEmails.isEmpty())
        {
            operationLog.info("List of emails to notify is empty. Skipping the email sending.");
            return;
        }

        IMailClient mailClient = ServiceProvider.getDataStoreService().createEMailClient();

        StringBuilder content = new StringBuilder("The following archive containers have been checked:\n");

        for (Map.Entry<String, CheckStatus> entry : statuses.getStatuses().entrySet())
        {
            String path = entry.getKey();
            CheckStatus status = entry.getValue();

            content.append("- ").append(path).append(" :\n");

            if (status.getErrors().size() > 0)
            {
                content.append("    errors:\n");
                for (String error : status.getErrors())
                {
                    content.append("        - ").append(error);
                }
            }

            if (status.getInfos().size() > 0)
            {
                content.append("    info:\n");
                for (String info : status.getInfos())
                {
                    content.append("        - ").append(info);
                }
            }
        }

        List<EMailAddress> emails = notifyEmails.stream().map(EMailAddress::new).collect(Collectors.toList());

        try
        {
            mailClient.sendEmailMessage("Multi-dataset archiver sanity check report", content.toString(), null, null,
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

        public void addStatuses(final CheckStatuses statuses)
        {
            this.statuses.putAll(statuses.statuses);
        }

        public Map<String, CheckStatus> getStatuses()
        {
            return statuses;
        }
    }

    @JsonObject("CheckStatus")
    private static class CheckStatus
    {

        @JsonProperty
        private final List<String> infos = new ArrayList<>();

        @JsonProperty
        private final List<String> errors = new ArrayList<>();

        public void addInfo(String message)
        {
            infos.add(message);
        }

        public void addError(String message, Exception e)
        {
            errors.add(message);
        }

        public List<String> getInfos()
        {
            return infos;
        }

        public List<String> getErrors()
        {
            return errors;
        }
    }

}
