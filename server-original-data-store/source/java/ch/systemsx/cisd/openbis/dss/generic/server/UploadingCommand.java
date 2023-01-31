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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.cifex.rpc.client.FileWithOverrideName;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.security.TokenGenerator;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;

/**
 * A command which zips the given data sets and uploads the ZIP file to CIFEX.
 * 
 * @author Franz-Josef Elmer
 */
class UploadingCommand implements IDataSetCommand
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            UploadingCommand.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            UploadingCommand.class);

    private final class ProgressListener implements IProgressListener
    {
        private final File zipFile;

        private ProgressListener(File zipFile)
        {
            this.zipFile = zipFile;
        }

        @Override
        public void start(File file, String operationName, long fileSize, Long fileIdOrNull)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Start uploading of zip file " + file);
            }
        }

        @Override
        public void reportProgress(int percentage, long numberOfBytes)
        {
        }

        @Override
        public void finished(boolean successful)
        {
            if (successful)
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Zip file " + zipFile + " has been successfully uploaded.");
                }
            } else
            {
                operationLog.warn("Uploading of zip file " + zipFile
                        + " has been aborted or failed.");
                sendEMail("Uploading of zip file " + zipFile.getName()
                        + " with requested data sets failed.");
            }
        }

        @Override
        public void exceptionOccured(Throwable throwable)
        {
            notificationLog.error("An error occured during uploading of zip file " + zipFile + ".",
                    throwable);
        }

        @Override
        public void warningOccured(String warningMessage)
        {
            operationLog.warn(warningMessage);
        }

    }

    private final ICIFEXRPCServiceFactory cifexServiceFactory;

    private final List<AbstractExternalData> dataSets;

    private final String fileName;

    private final String comment;

    private final String userID;

    private final String password;

    private final String userEMail;

    private final boolean userAuthenticated;

    private final String cifexAdminUserOrNull;

    private final String cifexAdminPasswordOrNull;

    private final MailClientParameters mailClientParameters;

    private final TokenGenerator tokenGenerator;

    @Private
    boolean deleteAfterUploading = true;

    @Private
    transient IHierarchicalContentProvider hierarchicalContentProvider;

    UploadingCommand(ICIFEXRPCServiceFactory cifexServiceFactory,
            MailClientParameters mailClientParameters, List<AbstractExternalData> dataSets,
            DataSetUploadContext context, String cifexAdminUserOrNull,
            String cifexAdminPasswordOrNull)
    {
        this.cifexServiceFactory = cifexServiceFactory;
        this.mailClientParameters = mailClientParameters;
        this.dataSets = dataSets;
        this.userID = context.getUserID();
        this.password = context.getPassword();
        this.userAuthenticated = context.isUserAuthenticated();
        this.cifexAdminUserOrNull = cifexAdminUserOrNull;
        this.cifexAdminPasswordOrNull = cifexAdminPasswordOrNull;
        fileName = context.getFileName();
        userEMail = context.getUserEMail();
        this.comment = context.getComment();
        tokenGenerator = new TokenGenerator();
    }

    @Override
    public List<String> getDataSetCodes()
    {
        List<String> result = new ArrayList<String>();
        for (AbstractExternalData dataSet : dataSets)
        {
            result.add(dataSet.getCode());
        }
        return result;
    }

    @Override
    public void execute(IHierarchicalContentProvider contentProvider,
            IDataSetDirectoryProvider dataSetDirectoryProvider)
    {
        File root = dataSetDirectoryProvider.getStoreRoot();
        File tempFolder = new File(root, "tmp");
        tempFolder.mkdirs();
        final File zipFile = new File(tempFolder, createFileName());
        boolean successful = fillZipFile(dataSetDirectoryProvider, zipFile);
        if (successful)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Zip file " + zipFile + " with " + dataSets.size()
                        + " data sets has been successfully created.");
            }
            ICIFEXComponent cifex = cifexServiceFactory.createCIFEXComponent();
            String sessionToken = getCIFEXSession(cifex);
            ICIFEXUploader uploader = cifex.createUploader(sessionToken);
            uploader.addProgressListener(new ProgressListener(zipFile));
            uploader.upload(Arrays.asList(new FileWithOverrideName(zipFile, null)),
                    Constants.USER_ID_PREFIX + userID, comment);
        } else
        {
            sendEMail("Couldn't create zip file " + zipFile.getName() + " with requested data sets");
        }
        if (deleteAfterUploading)
        {
            zipFile.delete();
        }
    }

    private String getCIFEXSession(ICIFEXComponent cifex)
    {
        return getCIFEXSession(cifex, userAuthenticated, userID, password, cifexAdminUserOrNull,
                cifexAdminPasswordOrNull);
    }

    private static String getCIFEXSession(ICIFEXComponent cifex, boolean userAuthenticated,
            String userID, String password, String cifexAdminUserOrNull,
            String cifexAdminPasswordOrNull)
    {
        if (userAuthenticated && StringUtils.isBlank(password)
                && StringUtils.isNotBlank(cifexAdminUserOrNull)
                && StringUtils.isNotBlank(cifexAdminPasswordOrNull))
        {
            final String token = cifex.login(cifexAdminUserOrNull, cifexAdminPasswordOrNull);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Calling setSessionUser() on CIFEX session to userID=%s", userID));
            }
            cifex.setSessionUser(token, userID);
            return token;
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Directly logging into CIFEX as userID=%s "
                        + "(user authenticated=%s, password provided=%s", userID,
                        userAuthenticated, StringUtils.isNotBlank(password)));
            }
            return cifex.login(userID, password);
        }
    }

    static boolean canLoginToCIFEX(ICIFEXComponent cifex, boolean userAuthenticated, String userID,
            String password, String cifexAdminUserOrNull, String cifexAdminPasswordOrNull)
    {
        final String tokenOrNull =
                getCIFEXSession(cifex, userAuthenticated, userID, password, cifexAdminUserOrNull,
                        cifexAdminPasswordOrNull);
        if (tokenOrNull != null)
        {
            cifex.logout(tokenOrNull);
            return true;
        } else
        {
            return false;
        }
    }

    private String createFileName()
    {
        if (StringUtils.isBlank(fileName))
        {
            return tokenGenerator.getNewToken(System.currentTimeMillis()) + ".zip";
        }
        return fileName.toLowerCase().endsWith(".zip") ? fileName : fileName + ".zip";
    }

    private boolean fillZipFile(IDataSetDirectoryProvider dataSetDirectoryProvider, File zipFile)
    {
        AbstractDataSetPackager packager = null;
        DataSetExistenceChecker dataSetExistenceChecker =
                new DataSetExistenceChecker(dataSetDirectoryProvider,
                        TimingParameters.create(new Properties()));
        try
        {
            packager = new ZipDataSetPackager(zipFile, true,
                    getHierarchicalContentProvider(), dataSetExistenceChecker);
            for (AbstractExternalData externalData : dataSets)
            {
                String newRootPath = createRootPath(externalData) + "/";
                try
                {

                    packager.addDataSetTo(newRootPath, externalData);
                } catch (RuntimeException ex)
                {
                    notificationLog.error(ex.getMessage(), ex);
                    return false;
                }
            }
            return true;
        } catch (Exception ex)
        {
            notificationLog.error("Couldn't create zip file for uploading", ex);
            return false;
        } finally
        {
            if (packager != null)
            {
                try
                {
                    packager.close();
                } catch (Exception ex)
                {
                    notificationLog.error("Couldn't close package", ex);
                }
            }
        }
    }

    private IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (hierarchicalContentProvider == null)
        {
            hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return hierarchicalContentProvider;
    }

    private String createRootPath(AbstractExternalData dataSet)
    {
        Sample sample = dataSet.getSample();
        Experiment experiment = dataSet.getExperiment();
        if (experiment != null)
        {
            Project project = experiment.getProject();
            return project.getSpace().getCode() + "/" + project.getCode() + "/" + experiment.getCode()
                    + "/" + (sample == null ? "" : sample.getCode() + "/") + dataSet.getCode();
        } else
        {
            return sample.getSpace().getCode() + "/" + sample.getCode() + "/" + dataSet.getCode();
        }
    }

    private void sendEMail(String message)
    {
        final IMailClient mailClient = new MailClient(mailClientParameters);
        mailClient.sendMessage("[Data Set Server] Uploading failed", message, null, null, userEMail);
    }

    @Override
    public String getType()
    {
        return "CIFEX Uploading";
    }

    @Override
    public String getDescription()
    {
        final StringBuilder b = new StringBuilder();
        b.append("Upload data sets to CIFEX: ");
        for (AbstractExternalData dataset : dataSets)
        {
            b.append(dataset.getCode());
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

}
