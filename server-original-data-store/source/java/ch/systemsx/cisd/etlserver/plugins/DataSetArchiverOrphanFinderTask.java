/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.MultiDataSetArchiver;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

public class DataSetArchiverOrphanFinderTask implements IMaintenanceTask
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DataSetArchiverOrphanFinderTask.class);

    public static final String EMAIL_ADDRESSES_KEY = "email-addresses";

    private static final String SEPARATOR = ",";

    private List<EMailAddress> emailAddresses;

    private IMailClient mailClient;

    private boolean isMultiDatasetArchiver;

    public DataSetArchiverOrphanFinderTask()
    {
        this(ServiceProvider.getDataStoreService().createEMailClient(),
                ServiceProvider.getDataStoreService().getArchiverPlugin() instanceof MultiDataSetArchiver);
    }

    DataSetArchiverOrphanFinderTask(IMailClient mailClient, boolean isMultiDatasetArchiver)
    {
        this.mailClient = mailClient;
        this.isMultiDatasetArchiver = isMultiDatasetArchiver;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Task " + pluginName + " initialized.");
        emailAddresses = getEMailAddresses(properties);
    }

    @Transactional
    @Override
    public void execute()
    {
        operationLog.info(DataSetArchiverOrphanFinderTask.class.getSimpleName() + " Started");

        operationLog.info("1. Directories, obtain archiver directory.");

        String destination = null;
        if (isMultiDatasetArchiver)
        {
            destination = DataStoreServer.getConfigParameter("archiver.final-destination", null);
        } else
        {
            destination = DataStoreServer.getConfigParameter("archiver.destination", null);
        }

        if (destination == null)
        {
            operationLog.info("No destination directory found, this task can't be executed.");
            return;
        }
        File destinationFolder = new File(destination);
        if (destinationFolder.isDirectory() == false)
        {
            operationLog.warn("The destination directory does not exist or isn't a folder: " + destinationFolder.getAbsolutePath());
            return;
        }

        operationLog.info("2.1 Database, obtain a list of the multi dataset containers on the database.");
        List<String> multiDataSetContrainers = null;
        if (isMultiDatasetArchiver)
        {
            multiDataSetContrainers = MultiDataSetArchiverDataSourceUtil.getContainerList();
        }

        Set<String> multiDatasetsContainersOnDB = new HashSet<String>();
        if (multiDataSetContrainers != null)
        {
            for (String container : multiDataSetContrainers)
            {
                multiDatasetsContainersOnDB.add(container.toLowerCase());
            }
        }

        operationLog.info("2.2 Database, obtain a list of the archived datasets on the database.");
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        List<SimpleDataSetInformationDTO> presentDTOs = service.listPhysicalDataSetsByArchivingStatus(null, Boolean.TRUE);
        Set<String> presentInArchiveOnDB = new HashSet<String>();
        if (presentDTOs != null)
        {
            for (SimpleDataSetInformationDTO presentDTO : presentDTOs)
            {
                presentInArchiveOnDB.add(presentDTO.getDataSetCode().toLowerCase());
            }
        }

        List<File> filesOnDisk = null;

        try
        {
            filesOnDisk = Files.find(destinationFolder.toPath(), Integer.MAX_VALUE, (path, basicFileAttributes) -> !Files.isDirectory(path))
                    .map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e)
        {
            throw new RuntimeException("Couldn't list files in folder: " + destinationFolder.getAbsolutePath(), e);
        }

        operationLog.info("3. Verify if the " + filesOnDisk.size()
                + " files on destination are on multi dataset archiver containers or a normal archived dataset.");

        Set<String> presentInArchiveFS = new HashSet<String>();
        List<File> onFSandNotDB = new ArrayList<File>();
        for (File file : filesOnDisk)
        {
            String fileName = destinationFolder.toPath().relativize(file.toPath()).toString().toLowerCase();
            presentInArchiveFS.add(fileName); // To be used in step 4
            if (multiDatasetsContainersOnDB.contains(fileName))
            {
                operationLog.debug("Found multi dataset archiver container: " + file.getName());
            } else
            {
                String dataSetCode = file.getName().substring(0, file.getName().length() - 4);
                if ((fileName.endsWith(".tar") || fileName.endsWith(".zip"))
                        && presentInArchiveOnDB.contains(dataSetCode))
                {
                    operationLog.debug("Found archived dataset: " + file.getName());
                } else
                {
                    onFSandNotDB.add(file);
                    operationLog.debug("Not found on DB for FS: " + file.getName());
                }
            }
        }

        operationLog.info("4. Verify if the " + multiDatasetsContainersOnDB.size() 
            + " containers of the multi data set mapping database are on the file system.");
        List<String> multiOnDBandNotFS = new ArrayList<String>();
        for (String multiDatasetsContainerOnDB : multiDatasetsContainersOnDB)
        {
            if (presentInArchiveFS.contains(multiDatasetsContainerOnDB) == false)
            {
                operationLog.debug("Multi - Not found in FS for DB: " + multiDatasetsContainerOnDB);
                multiOnDBandNotFS.add(multiDatasetsContainerOnDB);
            }
        }

        List<String> singleOnDBandNotFS = new ArrayList<String>();
        for (String presentOnDB : presentInArchiveOnDB)
        {
            String fileNameTar = presentOnDB + ".tar";
            String fileNameZip = presentOnDB + ".zip";

            if (presentInArchiveFS.contains(fileNameTar) == false &&
                    presentInArchiveFS.contains(fileNameZip) == false &&
                    (isMultiDatasetArchiver == false ||
                            (isMultiDatasetArchiver &&
                                    MultiDataSetArchiverDataSourceUtil.isDataSetInContainer(presentOnDB.toUpperCase()) == false)))
            {
                operationLog.debug("Single - Not found in FS for DB: " + presentOnDB);
                singleOnDBandNotFS.add(presentOnDB);
            }
        }

        if (onFSandNotDB.isEmpty() == false || multiOnDBandNotFS.isEmpty() == false || singleOnDBandNotFS.isEmpty() == false)
        {
            operationLog.info("5. Send email with not found files.");
            String subject = "openBIS Data Set Archive Orphan Finder report";
            String content = "";
            Collections.sort(onFSandNotDB);
            for (File notFound : onFSandNotDB)
            {
                content += "Found in the archive but not in the database: " + destinationFolder.toPath().relativize(notFound.toPath()) + "\n";
            }
            Collections.sort(multiOnDBandNotFS);
            for (String notFound : multiOnDBandNotFS)
            {
                content += "Found in the database but not in the multi data set archive: " + notFound + "\n";
            }
            Collections.sort(singleOnDBandNotFS);
            for (String notFound : singleOnDBandNotFS)
            {
                content += "Found in the database but not in the archive: " + notFound + "\n";
            }
            for (EMailAddress recipient : emailAddresses)
            {
                mailClient.sendEmailMessage(subject, content, null, null, recipient);
                operationLog.info("Mail sent to " + recipient.tryGetEmailAddress());
            }
        }

        operationLog.info(DataSetArchiverOrphanFinderTask.class.getSimpleName() + " Finished");
    }

    private List<EMailAddress> getEMailAddresses(Properties properties)
    {
        String[] tokens =
                PropertyUtils.getMandatoryProperty(properties, EMAIL_ADDRESSES_KEY)
                        .split(SEPARATOR);
        List<EMailAddress> addresses = new ArrayList<EMailAddress>();
        for (String token : tokens)
        {
            addresses.add(new EMailAddress(token.trim()));
        }
        return addresses;
    }

}
