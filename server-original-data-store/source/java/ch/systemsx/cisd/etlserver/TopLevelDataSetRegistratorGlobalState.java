/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.resource.IReleasable;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.etlserver.registrator.recovery.IDataSetStorageRecoveryManager;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.IDataSourceQueryService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * Global state needed by top level data set registrators.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TopLevelDataSetRegistratorGlobalState implements IReleasable
{
    // can be used from dropboxes
    public static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TopLevelDataSetRegistratorGlobalState.class);

    private final String dssCode;

    private final String shareId;

    private final File storeRootDir;

    private final File dssInternalTempDir;

    private final File dssRegistrationLogDir;

    private final File preStagingDir;

    private final File stagingDir;

    private final File preCommitDir;

    private final File recoveryStateDir;

    private final File recoveryMarkerDir;

    private final IEncapsulatedOpenBISService openBisService;

    private final IMailClient mailClient;

    private final IDataSetValidator dataSetValidator;

    private final IDataSourceQueryService dataSourceQueryService;

    private final boolean notifySuccessfulRegistration;

    private final ThreadParameters threadParameters;

    private final boolean useIsFinishedMarkerFile;

    private final boolean deleteUnidentified;

    private final DynamicTransactionQueryFactory dynamicTransactionQueryFactory;

    private final String preRegistrationScriptOrNull;

    private final String postRegistrationScriptOrNull;

    private final String[] validationScriptsOrNull;

    private final IDataSetStorageRecoveryManager storageRecoveryManager;

    /**
     * Constructor that takes some values from the thread parameters.
     */
    public TopLevelDataSetRegistratorGlobalState(String dssCode, String shareId, File storeRootDir,
            File dssInternalTempDir, File dssRegistrationLogDir, File dssRecoveryStateDir,
            File dssRecoveryMarkerDir,
            IEncapsulatedOpenBISService openBisService, IMailClient mailClient,
            IDataSetValidator dataSetValidator, IDataSourceQueryService dataSourceQueryService,
            DynamicTransactionQueryFactory dynamicTransactionQueryFactory,
            boolean notifySuccessfulRegistration, ThreadParameters threadParameters,
            IDataSetStorageRecoveryManager storageRecoveryManager)
    {
        this(dssCode, shareId, storeRootDir, dssInternalTempDir, dssRegistrationLogDir,
                dssRecoveryStateDir, dssRecoveryMarkerDir, openBisService, mailClient, dataSetValidator,
                dataSourceQueryService, dynamicTransactionQueryFactory,
                notifySuccessfulRegistration, threadParameters, threadParameters
                        .useIsFinishedMarkerFile(), threadParameters.deleteUnidentified(),
                threadParameters.tryGetPreRegistrationScript(), threadParameters
                        .tryGetPostRegistrationScript(),
                threadParameters.tryGetValidationScripts(), storageRecoveryManager);
    }

    public TopLevelDataSetRegistratorGlobalState(String dssCode, String shareId, File storeRootDir,
            File dssInternalTempDir, File dssRegistrationLogDir, File dssRecoveryStateDir,
            File dssRecoveryMarkerDir,
            IEncapsulatedOpenBISService openBisService, IMailClient mailClient,
            IDataSetValidator dataSetValidator, IDataSourceQueryService dataSourceQueryService,
            DynamicTransactionQueryFactory dynamicTransactionQueryFactory,
            boolean notifySuccessfulRegistration, ThreadParameters threadParameters,
            boolean useIsFinishedMarkerFile, boolean deleteUnidentified,
            String preRegistrationScriptOrNull, String postRegistrationScriptOrNull,
            String[] validationScriptsOrNull, IDataSetStorageRecoveryManager storageRecoveryManager)
    {
        this.dssCode = dssCode;
        this.shareId = shareId;
        this.storeRootDir = storeRootDir;
        this.dssInternalTempDir = dssInternalTempDir;
        this.dssRegistrationLogDir = dssRegistrationLogDir;
        this.preStagingDir =
                getPreStagingDir(storeRootDir, shareId, threadParameters.getThreadProperties());
        this.stagingDir =
                getStagingDir(storeRootDir, shareId, threadParameters.getThreadProperties());
        this.preCommitDir =
                getPreCommitDir(storeRootDir, shareId, threadParameters.getThreadProperties());
        this.openBisService = openBisService;
        this.mailClient = mailClient;
        this.dataSetValidator = dataSetValidator;
        this.dataSourceQueryService = dataSourceQueryService;
        this.dynamicTransactionQueryFactory = dynamicTransactionQueryFactory;
        this.notifySuccessfulRegistration = notifySuccessfulRegistration;
        this.threadParameters = threadParameters;
        this.useIsFinishedMarkerFile = useIsFinishedMarkerFile;
        this.deleteUnidentified = deleteUnidentified;
        this.preRegistrationScriptOrNull = preRegistrationScriptOrNull;
        this.postRegistrationScriptOrNull = postRegistrationScriptOrNull;
        this.validationScriptsOrNull = validationScriptsOrNull;

        this.recoveryStateDir = new File(dssRecoveryStateDir, threadParameters.getThreadName());
        this.recoveryMarkerDir = new File(dssRecoveryMarkerDir, threadParameters.getThreadName());

        this.recoveryStateDir.mkdirs();
        this.recoveryMarkerDir.mkdirs();

        this.storageRecoveryManager = storageRecoveryManager;
        this.storageRecoveryManager.setDropboxRecoveryStateDir(this.recoveryStateDir);
        this.storageRecoveryManager.setRecoveryMarkerFilesDir(this.recoveryMarkerDir);
        this.storageRecoveryManager
                .setMaximumRertyCount(threadParameters.getMaximumRecoveryCount());
        this.storageRecoveryManager
                .setRetryPeriodInSeconds(threadParameters
                        .getMinimumRecoveryPeriod());

        // Initialize the DSS Registration Log Directory
        new DssRegistrationLogDirectoryHelper(dssRegistrationLogDir).initializeSubdirectories();
    }

    public String getDssCode()
    {
        return dssCode;
    }

    public String getShareId()
    {
        return shareId;
    }

    public File getStoreRootDir()
    {
        return storeRootDir;
    }

    /**
     * Get a directory that can be used for temporary files, and is local to the server.
     */
    public File getDssInternalTempDir()
    {
        return dssInternalTempDir;
    }

    /**
     * Get the directory that hold the DSS registration logs
     */
    public File getDssRegistrationLogDir()
    {
        return dssRegistrationLogDir;
    }

    /**
     * Get the directory used for pre-staging. This holds a hardlink copy of the incoming data.
     */
    public File getPreStagingDir()
    {
        return preStagingDir;
    }

    /**
     * Get the staging directory used in registration.
     */
    public File getStagingDir()
    {
        return stagingDir;
    }

    /**
     * Get's the precommit directory. It is used to keep "ready to store" files.
     */
    public File getPreCommitDir()
    {
        return preCommitDir;
    }

    public File getRecoveryStateDir()
    {
        return recoveryStateDir;
    }

    public IEncapsulatedOpenBISService getOpenBisService()
    {
        return openBisService;
    }

    public IMailClient getMailClient()
    {
        return mailClient;
    }

    public IDataSetValidator getDataSetValidator()
    {
        return dataSetValidator;
    }

    public IDataSourceQueryService getDataSourceQueryService()
    {
        return dataSourceQueryService;
    }

    public DynamicTransactionQueryFactory getDynamicTransactionQueryFactory()
    {
        return dynamicTransactionQueryFactory;
    }

    public boolean isNotifySuccessfulRegistration()
    {
        return notifySuccessfulRegistration;
    }

    public ThreadParameters getThreadParameters()
    {
        return threadParameters;
    }

    public boolean isUseIsFinishedMarkerFile()
    {
        return useIsFinishedMarkerFile;
    }

    public boolean isDeleteUnidentified()
    {
        return deleteUnidentified;
    }

    public String getPreRegistrationScript()
    {
        return preRegistrationScriptOrNull;
    }

    public String getPostRegistrationScript()
    {
        return postRegistrationScriptOrNull;
    }

    public String[] getValidationScriptsOrNull()
    {
        return validationScriptsOrNull;
    }

    /**
     * Return the email addresses of all administrator users registered on the openBIS AS.
     */
    public List<String> getAdministratorEmails()
    {
        List<String> emails = new ArrayList<String>();

        List<Person> administrators = openBisService.listAdministrators();
        for (Person admin : administrators)
        {
            String email = admin.getEmail();
            if (StringUtils.isNotBlank(email))
            {
                emails.add(email);
            }
        }
        return emails;
    }

    public IDataSetStorageRecoveryManager getStorageRecoveryManager()
    {
        return storageRecoveryManager;
    }

    /*
     * Properties that control the location of directories. Other properties are rather more suitable in ThreadParameters
     */
    public static final String STAGING_DIR = "staging-dir";

    public static final String DEFAULT_STAGING_DIR = "staging";

    public static final String PRE_STAGING_DIR = "pre-staging-dir";

    public static final String DEFAULT_PRE_STAGING_DIR = "pre-staging";

    public static final String PRE_COMMIT_DIR = "pre-commit-dir";

    public static final String DEFAULT_PRE_COMMIT_DIR = "pre-commit";

    public static File getStagingDir(File storeRoot, String shareId, Properties threadProperties)
    {
        return getShareLocalDir(storeRoot, shareId, threadProperties, STAGING_DIR, DEFAULT_STAGING_DIR);
    }

    public static File getPreStagingDir(File storeRoot, String shareId, Properties threadProperties)
    {
        return getShareLocalDir(storeRoot, shareId, threadProperties, PRE_STAGING_DIR,
                DEFAULT_PRE_STAGING_DIR);
    }

    public static File getPreCommitDir(File storeRoot, String shareId, Properties threadProperties)
    {
        return getShareLocalDir(storeRoot, shareId, threadProperties, PRE_COMMIT_DIR, DEFAULT_PRE_COMMIT_DIR);
    }

    /**
     * Get a directory local to the share, respecting the user override, if one is specified, and defaulting to the defaultDirName.
     * 
     * @param storeRoot The root of the DSS store
     * @param shareId The shareId the directory should be local to
     * @param threadProperties The properties where the the override might be specified
     * @param overridePropertyName The name of the property that specifies the override
     * @param defaultDirName The default name of the directory to use if no override has been specified
     * @return The directory to use
     */
    private static File getShareLocalDir(File storeRoot, String shareId,
            Properties threadProperties, String overridePropertyName, String defaultDirName)
    {
        String shareLocalDirPath =
                PropertyUtils.getProperty(threadProperties, overridePropertyName);
        if (null == shareLocalDirPath)
        {
            return getDefaultShareLocalDir(storeRoot, shareId, defaultDirName);
        } else
        {
            File shareLocalDir = new File(shareLocalDirPath);
            shareLocalDir.mkdirs();
            return shareLocalDir;
        }
    }

    /**
     * Get and create a directory that is on the
     */
    private static File getDefaultShareLocalDir(File storeRoot, String shareId, String dirName)
    {
        File shareRoot;
        if (false == StringUtils.isBlank(shareId))
        {
            shareRoot = new File(storeRoot, shareId);
        } else
        {
            shareRoot = storeRoot;
        }

        File stagingDir;
        if (shareRoot.isDirectory())
        {
            stagingDir = new File(shareRoot, dirName);
        } else
        {
            stagingDir = new File(storeRoot, dirName);
        }
        stagingDir.mkdir();
        if (stagingDir.isDirectory())
        {
            return stagingDir;
        }

        return storeRoot;
    }

    @Override
    public void release()
    {
        if (getDataSourceQueryService() instanceof IReleasable)
        {
            ((IReleasable) getDataSourceQueryService()).release();
        }
    }

}
