/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.api.v1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.etlserver.DataStrategyStore;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.validation.DataSetValidator;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.common.io.ByteArrayBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPutDataSetService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptReader;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SessionWorkspaceUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * Helper class that maintains the state for handling put requests. The requests themselves are serviced by the {@link PutDataSetExecutor}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class PutDataSetService implements IPutDataSetService
{

    private static final String MULTIPLE_FILES_UPLOAD_DIR = "upload";

    private final IEncapsulatedOpenBISService openBisService;

    private final Logger operationLog;

    private final Lock registrationLock;

    // These are all initialized only once, but it is not possible to initialize them at
    // construction time, since this causes a dependency loop that causes problems in Spring.
    private DataSetTypeToRegistratorMapper registratorMap;

    private DataStrategyStore dataStrategyStore;

    private File storeDirectory;

    private String dataStoreCode;

    private boolean isInitialized = false;

    private IMailClient mailClient;

    private IDataSetValidator dataSetValidator;

    private DatabaseInstance homeDatabaseInstance;

    private File sessionWorkspace = SessionWorkspaceUtil.getSessionWorkspace(
            DssPropertyParametersUtil.loadServiceProperties());

    /**
     * The designated constructor.
     * 
     * @param openBisService
     * @param operationLog
     */
    public PutDataSetService(IEncapsulatedOpenBISService openBisService, Logger operationLog)
    {
        this.openBisService = openBisService;
        this.operationLog = operationLog;

        this.registrationLock = new ReentrantLock();
    }

    /**
     * A constructor for testing purposes. Not useful outside of testing.
     * 
     * @param openBisService
     * @param operationLog
     * @param store
     * @param map
     * @param mail
     * @param dsCode
     */
    public PutDataSetService(IEncapsulatedOpenBISService openBisService, Logger operationLog,
            File store, DataSetTypeToRegistratorMapper map, IMailClient mail, String dsCode,
            IDataSetValidator validator)
    {
        this(openBisService, operationLog);

        registratorMap = map;
        storeDirectory = store;
        registratorMap.initializeStoreRootDirectory(storeDirectory);

        mailClient = mail;
        dataStrategyStore = new DataStrategyStore(openBisService, mailClient);

        this.dataStoreCode = dsCode;

        homeDatabaseInstance = openBisService.getHomeDatabaseInstance();

        dataSetValidator = validator;

        isInitialized = true;
    }

    public String putDataSet(String sessionToken, String dropboxName,
            CustomImportFile customImportFile)
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        try
        {
            ITopLevelDataSetRegistrator registrator =
                    registratorMap.getRegistratorForDropbox(dropboxName);

            final List<DataSetInformation> infos;
            // Branch -- use the old logic for the ETLServerPlugins
            if (registrator instanceof PutDataSetServerPluginHolder)
            {
                infos =
                        new PutDataSetExecutor(this,
                                ((PutDataSetServerPluginHolder) registrator).getPlugin(),
                                sessionToken, createNewDataSetDTO(customImportFile),
                                getAsInputStream(customImportFile)).execute(true);
            } else
            {
                infos =
                        new PutDataSetTopLevelDataSetHandler(this, registrator, sessionToken,
                                createNewDataSetDTO(customImportFile),
                                getAsInputStream(customImportFile)).execute(true);
            }
            StringBuilder sb = new StringBuilder();
            for (DataSetInformation info : infos)
            {
                sb.append(info.getDataSetCode());
                sb.append(",");
            }

            // Remove the trailing comma
            if (sb.length() > 0)
            {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } catch (UserFailureException e)
        {
            throw new IllegalArgumentException(e);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        }
    }

    private InputStream getAsInputStream(CustomImportFile customImportFile)
    {
        return new ConcatenatedContentInputStream(false, new ByteArrayBasedContentNode(
                customImportFile.getBytes(), customImportFile.getFileName()));
    }

    private NewDataSetDTO createNewDataSetDTO(CustomImportFile customImportFile)
    {
        return new NewDataSetDTO(null, null, Collections.singletonList(new FileInfoDssDTO(
                customImportFile.getFileName(), customImportFile.getFileName(), false,
                customImportFile.getBytes().length)));
    }

    public String putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        try
        {
            String dataSetTypeOrNull = newDataSet.tryDataSetType();
            ITopLevelDataSetRegistrator registrator =
                    registratorMap.getRegistratorForType(dataSetTypeOrNull);

            final List<DataSetInformation> infos;
            // Branch -- use the old logic for the ETLServerPlugins
            if (registrator instanceof PutDataSetServerPluginHolder)
            {
                infos =
                        new PutDataSetExecutor(this,
                                ((PutDataSetServerPluginHolder) registrator).getPlugin(),
                                sessionToken, newDataSet, inputStream).execute(false);
            } else
            {
                infos =
                        new PutDataSetTopLevelDataSetHandler(this, registrator, sessionToken,
                                newDataSet, inputStream).execute(false);
            }
            StringBuilder sb = new StringBuilder();
            for (DataSetInformation info : infos)
            {
                sb.append(info.getDataSetCode());
                sb.append(",");
            }

            // Remove the trailing comma
            if (sb.length() > 0)
            {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } catch (UserFailureException e)
        {
            throw new IllegalArgumentException(e);
        } catch (IOException e)
        {
            throw new IOExceptionUnchecked(e);
        } finally
        {
            // Close the input stream now that we are done with it
            try
            {
                inputStream.close();
            } catch (IOException ex)
            {

            }
        }
    }

    public List<DataSetInformation> putDataSet(String sessionToken, NewDataSetDTO newDataSet, String uploadId)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        if (newDataSet == null)
        {
            throw new UserFailureException("New data set cannot be null");
        }

        if (false == isInitialized)
        {
            doInitialization();
        }

        validateSessionToken(sessionToken);
        validateUploadId(uploadId);

        ServiceProvider.getOpenBISService().checkSession(sessionToken);

        File sessionWorkspaceUploadDir = new File(new File(sessionWorkspace, sessionToken), uploadId);

        List<DataSetInformation> result = null;
        if (sessionWorkspaceUploadDir.exists())
        {
            result = putDataSetFromSessionWorkspaceFileUpload(sessionToken, newDataSet, uploadId);
        } else {
            result = putDataSetFromStoreShareFileUpload(sessionToken, newDataSet, uploadId);
        }
        return result;
    }

    private List<DataSetInformation> putDataSetFromSessionWorkspaceFileUpload(String sessionToken, NewDataSetDTO newDataSet, String uploadId)
            throws IOExceptionUnchecked, IllegalArgumentException {
        String dataSetType = newDataSet.tryDataSetType();
        ITopLevelDataSetRegistrator registrator = registratorMap.getRegistratorForType(dataSetType);

        File tempIncomingDir = new File(new File(sessionWorkspace, sessionToken), uploadId);

        File[] filesInIncomingDir = tempIncomingDir.listFiles();
        if (filesInIncomingDir.length != 1) {
            throw new UserFailureException("Unexpected state, incoming directory should contain just one file or folder but found: " + filesInIncomingDir.length);
        }

        if (registrator instanceof PutDataSetServerPluginHolder)
        {
            return new PutDataSetExecutor(this, ((PutDataSetServerPluginHolder) registrator).getPlugin(), sessionToken, newDataSet, tempIncomingDir,
                    filesInIncomingDir[0]).executeWithoutWriting();
        } else
        {
            return new PutDataSetTopLevelDataSetHandler(this, registrator, sessionToken, newDataSet, tempIncomingDir, filesInIncomingDir[0]).executeWithoutWriting();
        }
    }

    private List<DataSetInformation> putDataSetFromStoreShareFileUpload(String sessionToken, NewDataSetDTO newDataSet, String uploadId)
            throws IOExceptionUnchecked, IllegalArgumentException {

        String dataSetType = newDataSet.tryDataSetType();
        ITopLevelDataSetRegistrator registrator = registratorMap.getRegistratorForType(dataSetType);

        File sessionUploadDir = new File(getTemporaryIncomingRoot(dataSetType), sessionToken);
        File uploadIdDir = new File(sessionUploadDir, uploadId);
        File multipleFilesUploadDir = new File(uploadIdDir, MULTIPLE_FILES_UPLOAD_DIR);

        File[] uploadedFiles = null;
        File dataSet = null;

        if (multipleFilesUploadDir.exists() && multipleFilesUploadDir.isDirectory())
        {
            uploadedFiles = multipleFilesUploadDir.listFiles();
        }

        if (uploadedFiles == null || uploadedFiles.length == 0)
        {
            throw new UserFailureException("No uploaded files found for upload id '" + uploadId + "'");
        } else if (uploadedFiles.length == 1)
        {
            dataSet = uploadedFiles[0];
        } else
        {
            dataSet = multipleFilesUploadDir;
        }

        if (registrator instanceof PutDataSetServerPluginHolder)
        {
            return new PutDataSetExecutor(this, ((PutDataSetServerPluginHolder) registrator).getPlugin(), sessionToken, newDataSet, uploadIdDir,
                    dataSet).executeWithoutWriting();
        } else
        {
            return new PutDataSetTopLevelDataSetHandler(this, registrator, sessionToken, newDataSet, uploadIdDir, dataSet).executeWithoutWriting();
        }
    }

    public void putFileToStoreShare(String sessionToken, String folderPathOrNull, String filePath, String dataSetType, String uploadId,
            InputStream inputStream)
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        File file = null;
        OutputStream outputStream = null;

        try
        {
            validateSessionToken(sessionToken);
            validateUploadId(uploadId);
            if (StringUtils.isBlank(filePath))
            {
                throw new UserFailureException("File path cannot be null or empty");
            }
            if (false == StringUtils.isBlank(folderPathOrNull) && folderPathOrNull.contains("../"))
            {
                throw new UserFailureException("Folder path must not contain '../'");
            }
            if (filePath.contains("../"))
            {
                throw new UserFailureException("File path must not contain '../'");
            }
            if (StringUtils.isBlank(dataSetType))
            {
                throw new UserFailureException("Data set type cannot be null or empty");
            }
            if (inputStream == null)
            {
                throw new UserFailureException("Input stream cannot be null");
            }

            ServiceProvider.getOpenBISService().checkSession(sessionToken);

            File sessionUploadDir = new File(getTemporaryIncomingRoot(dataSetType), sessionToken);
            File uploadIdDir = new File(sessionUploadDir, uploadId);
            File multipleFilesUploadDir = new File(uploadIdDir, MULTIPLE_FILES_UPLOAD_DIR);

            File filePathDir = null;
            if (StringUtils.isBlank(folderPathOrNull))
            {
                filePathDir = new File(multipleFilesUploadDir, FilenameUtils.getPath(filePath));
            } else
            {
                filePathDir = new File(multipleFilesUploadDir, FilenameUtils.getPath(folderPathOrNull + "/" + filePath));
            }

            if (false == filePathDir.exists())
            {
                filePathDir.mkdirs();
            }

            file = new File(filePathDir, FilenameUtils.getName(filePath));
            outputStream = new FileOutputStream(file);
            IOUtils.copyLarge(inputStream, outputStream);

            operationLog.info("File '" + filePath + "' with upload id: '" + uploadId + "' has been stored as '" + file.getCanonicalPath() + "'");

        } catch (IOException ioe)
        {
            IOUtils.closeQuietly(outputStream);

            try
            {
                file.delete();
            } catch (Exception e)
            {
            }

            throw CheckedExceptionTunnel.wrapIfNecessary(ioe);

        } finally
        {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Return the validation script for the new data set, or null if none if applicable.
     */
    public String getValidationScript(String dataSetTypeOrNull)
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        ITopLevelDataSetRegistrator registrator =
                registratorMap.getRegistratorForType(dataSetTypeOrNull);
        TopLevelDataSetRegistratorGlobalState globalState = registrator.getGlobalState();
        String[] scriptPaths = globalState.getValidationScriptsOrNull();
        if (scriptPaths == null)
        {
            return null;
        }

        return ValidationScriptReader.tryReadValidationScript(scriptPaths);
    }

    private void doInitialization()
    {
        PutDataSetServiceInitializer initializer = new PutDataSetServiceInitializer();

        mailClient = new MailClient(initializer.getMailProperties());
        dataStrategyStore = new DataStrategyStore(openBisService, mailClient);

        this.dataStoreCode = initializer.getDataStoreCode();

        homeDatabaseInstance = openBisService.getHomeDatabaseInstance();

        dataSetValidator = initializer.getDataSetValidator();

        File[] shares = SegmentedStoreUtils.getShares(storeDirectory);
        if (shares.length == 0)
        {
            if (new File(storeDirectory, Constants.DEFAULT_SHARE_ID).mkdirs() == false)
            {
                throw new ConfigurationFailureException("Can not create default share in store: "
                        + storeDirectory);
            }
        }

        registratorMap = initializer.getRegistratorMap(openBisService, mailClient);
        registratorMap.initializeStoreRootDirectory(storeDirectory);

        isInitialized = true;
    }

    IEncapsulatedOpenBISService getOpenBisService()
    {
        return openBisService;
    }

    IMailClient getMailClient()
    {
        return mailClient;
    }

    public String getShareId(String dataSetTypeOrNull)
    {
        return getThreadGlobalState(dataSetTypeOrNull).getShareId();
    }

    File getIncomingDir(String dataSetTypeOrNull)
    {
        return getThreadGlobalState(dataSetTypeOrNull).getThreadParameters()
                .getIncomingDataDirectory();
    }

    private TopLevelDataSetRegistratorGlobalState getThreadGlobalState(String dataSetTypeOrNull)
    {
        return registratorMap.getRegistratorForType(dataSetTypeOrNull).getGlobalState();
    }

    private Collection<TopLevelDataSetRegistratorGlobalState> getThreadGlobalStates()
    {
        Collection<TopLevelDataSetRegistratorGlobalState> states = new ArrayList<TopLevelDataSetRegistratorGlobalState>();
        Collection<ITopLevelDataSetRegistrator> registrators = registratorMap.getRegistrators();

        for (ITopLevelDataSetRegistrator registrator : registrators)
        {
            if (registrator != null && registrator.getGlobalState() != null)
            {
                states.add(registrator.getGlobalState());
            }
        }

        return states;
    }

    Logger getOperationLog()
    {
        return operationLog;
    }

    Lock getRegistrationLock()
    {
        return registrationLock;
    }

    DataStrategyStore getDataStrategyStore()
    {
        return dataStrategyStore;
    }

    String getDataStoreCode()
    {
        return dataStoreCode;
    }

    IDataSetValidator getDataSetValidator()
    {
        return dataSetValidator;
    }

    DatabaseInstance getHomeDatabaseInstance()
    {
        return homeDatabaseInstance;
    }

    public File getStoreRootDirectory()
    {
        return storeDirectory;
    }

    public void setStoreDirectory(File storeDirectory)
    {
        this.storeDirectory = storeDirectory;
    }

    public File createTemporaryIncomingDir(String dataSetTypeCodeOrNull)
    {
        String uniqueFolderName = openBisService.createPermId();
        File temporaryIncomingDir =
                new File(getTemporaryIncomingRoot(dataSetTypeCodeOrNull), uniqueFolderName);
        temporaryIncomingDir.mkdir();
        return temporaryIncomingDir;
    }

    public File getTemporaryIncomingRoot(String dataSetTypeCodeOrNull)
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        TopLevelDataSetRegistratorGlobalState globalState =
                getThreadGlobalState(dataSetTypeCodeOrNull);
        return getTemporaryIncomingRoot(globalState);
    }

    private File getTemporaryIncomingRoot(TopLevelDataSetRegistratorGlobalState globalState)
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        File storeRoot = globalState.getStoreRootDir();
        if (false == StringUtils.isBlank(globalState.getShareId()))
        {
            File shareRoot = new File(storeRoot, globalState.getShareId());
            if (shareRoot.isDirectory())
            {
                File incomingDir = new File(shareRoot, "rpc-incoming");
                incomingDir.mkdir();
                if (incomingDir.isDirectory())
                {
                    return incomingDir;
                }
            }
        }
        return storeRoot;
    }

    public void cleanupSession(String sessionToken)
    {
        if (false == isInitialized)
        {
            doInitialization();
        }

        validateSessionToken(sessionToken);

        Collection<TopLevelDataSetRegistratorGlobalState> states = getThreadGlobalStates();

        for (TopLevelDataSetRegistratorGlobalState state : states)
        {
            File sessionUploadDir = null;

            try
            {
                sessionUploadDir = new File(getTemporaryIncomingRoot(state), sessionToken);

                if (sessionUploadDir.exists())
                {
                    operationLog.info("Cleaning up a user session upload folder '" + sessionUploadDir.getAbsolutePath() + "'");
                    QueueingPathRemoverService.removeRecursively(sessionUploadDir);
                }
            } catch (Exception e)
            {
                operationLog.warn(
                        "Could not clean up a user session upload folder '" + sessionUploadDir.getAbsolutePath() + "' together with the user session",
                        e);
            }
        }
    }

    private void validateSessionToken(String sessionToken)
    {
        validate(sessionToken, "Session token");
    }

    private void validateUploadId(String uploadId)
    {
        validate(uploadId, "Upload id");
    }

    private void validate(String uploadId, String name)
    {
        if (StringUtils.isBlank(uploadId))
        {
            throw new UserFailureException(name + " cannot be null or empty");
        }
        if (uploadId.contains("/"))
        {
            throw new UserFailureException(name + " must not contain '/'");
        }
    }

}

/**
 * Helper class to simplify initializing the final fields of the {@link PutDataSetService}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetServiceInitializer
{
    private final Parameters params;

    PutDataSetServiceInitializer()
    {
        params = Parameters.createParametersForApiUse();
    }

    public DataSetTypeToRegistratorMapper getRegistratorMap(
            IEncapsulatedOpenBISService openBisService, IMailClient mailClient)
    {
        return new DataSetTypeToRegistratorMapper(params, openBisService, mailClient,
                getDataSetValidator());
    }

    Properties getMailProperties()
    {
        return Parameters.createMailProperties(params.getProperties());
    }

    String getDataStoreCode()
    {
        return DssPropertyParametersUtil.getDataStoreCode(params.getProperties());
    }

    DataSetValidator getDataSetValidator()
    {
        return new DataSetValidator(params.getProperties());
    }
}
