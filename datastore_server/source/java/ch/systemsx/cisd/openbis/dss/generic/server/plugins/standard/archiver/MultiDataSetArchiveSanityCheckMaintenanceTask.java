package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.path.IPathsInfoDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import net.lemnik.eodsql.QueryTool;

public class MultiDataSetArchiveSanityCheckMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MultiDataSetArchiveSanityCheckMaintenanceTask.class);

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    private static final String ARCHIVE_FOLDER_KEY = "archive-folder";

    private static final String DOWNLOAD_FOLDER_KEY = "download-folder";

    private static final String CHECK_FROM_DATE_KEY = "check-from-date";

    private static final String CHECK_TO_DATE_KEY = "check-to-date";

    private static final String NOTIFY_EMAILS_KEY = "notify-emails";

    private static final String STATUS_FILE_KEY = "status-file";

    private static final String STATUS_FILE_DEFAULT = "TODO";

    private String archiveFolder;

    private String downloadFolder;

    private List<String> notifyEmails;

    private Date checkFromDate;

    private Date checkToDate;

    private String statusFile;

    private IMultiDataSetArchiverReadonlyQueryDAO multiDataSetDAO;

    private IPathsInfoDAO pathInfoDAO;

    @Override public void setUp(final String pluginName, final Properties properties)
    {
        archiveFolder = PropertyUtils.getMandatoryProperty(properties, ARCHIVE_FOLDER_KEY);
        downloadFolder = PropertyUtils.getMandatoryProperty(properties, DOWNLOAD_FOLDER_KEY);
        checkFromDate = getMandatoryDateProperty(properties, CHECK_FROM_DATE_KEY);
        checkToDate = getMandatoryDateProperty(properties, CHECK_TO_DATE_KEY);
        notifyEmails = PropertyUtils.getMandatoryList(properties, NOTIFY_EMAILS_KEY);
        statusFile = PropertyUtils.getProperty(properties, STATUS_FILE_KEY, STATUS_FILE_DEFAULT);

        multiDataSetDAO = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        pathInfoDAO = QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }

    @Override public void execute()
    {
        CheckStatuses statuses = loadCheckStatuses();

        for (MultiDataSetArchiverContainerDTO container : multiDataSetDAO.listContainers())
        {
            CheckStatus status = statuses.getStatus(container);

            if (status == null)
            {
                if (shouldCheckConsistency(status, container))
                {
                    checkConsistency(status, container);
                }
            }
        }
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
                status.logError("Could not read attributes of tar file '" + tar.getAbsolutePath() + "'");
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

                        if (pathInfoDataSetId != null)
                        {
                            // TODO
                        } else
                        {
                            status.logError("Path info database does not have information about data set '" + containerDataSet.getCode()
                                    + "' which is part of '" + tar.getAbsolutePath() + "' tar file. Consistency cannot be checked.");
                            return false;
                        }
                    }

                    status.logInfo("Tar file '" + tar.getAbsolutePath() + "' created on '" + DATE_FORMAT.format(creationDate)
                            + "' qualified for the check. Path info database contains information about all of its data sets.");
                    return true;
                }
            } else
            {
                status.logError("Cannot check if tar file '" + tar.getAbsolutePath() + "' should be verified, because its creation date is 0.");
                return false;
            }
        } else
        {
            status.logError("Tar path '" + tar.getAbsolutePath()
                    + "' stored in the multi-dataset archiver database points to a file that does not exist.");
            return false;
        }
    }

    private void checkConsistency(final CheckStatuses statuses, final MultiDataSetArchiverContainerDTO container)
    {
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
        return null;
    }

    private void saveCheckStatuses()
    {

    }

    private static class CheckStatuses
    {

        public CheckStatus getStatus(final MultiDataSetArchiverContainerDTO container)
        {
            return null;
        }
    }

    private static class CheckStatus
    {

        public void logError(String message)
        {

        }

        public void logWarn(String message)
        {

        }

        public void logInfo(String message)
        {

        }

    }

}
