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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.DataSetRegistrationAlgorithmState;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

public abstract class DataSetRegistrationHelper implements
        DataSetRegistrationAlgorithm.IRollbackDelegate
{
    @Private
    public static final String EMAIL_SUBJECT_TEMPLATE =
            DataSetRegistrationAlgorithm.EMAIL_SUBJECT_TEMPLATE;

    @Private
    static final String DATA_SET_REGISTRATION_FAILURE_TEMPLATE =
            DataSetRegistrationAlgorithm.DATA_SET_REGISTRATION_FAILURE_TEMPLATE;

    @Private
    static final String DATA_SET_STORAGE_FAILURE_TEMPLATE =
            DataSetRegistrationAlgorithm.DATA_SET_STORAGE_FAILURE_TEMPLATE;

    @Private
    static final String SUCCESSFULLY_REGISTERED =
            DataSetRegistrationAlgorithm.SUCCESSFULLY_REGISTERED;

    protected final File incomingDataSetFile;

    protected final DataSetRegistrationAlgorithm registrationAlgorithm;

    public DataSetRegistrationHelper(File incomingDataSetFile, String shareId,
            IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
            IPreRegistrationAction preRegistrationAction,
            IPostRegistrationAction postRegistrationAction)
    {
        DataSetInformation dataSetInformation = extractDataSetInformation(incomingDataSetFile);
        dataSetInformation.setShareId(shareId);
        IDataStoreStrategy dataStoreStrategy =
                getDataStrategyStore()
                        .getDataStoreStrategy(dataSetInformation, incomingDataSetFile);
        DataSetRegistrationAlgorithmState algorithmState =
                new DataSetRegistrationAlgorithmState(incomingDataSetFile, getOpenBisService(),
                        cleanAftrewardsAction, preRegistrationAction, postRegistrationAction,
                        dataSetInformation, dataStoreStrategy, getTypeExtractor(),
                        getStorageProcessor(), getFileOperations(), getDataSetValidator(),
                        getMailClient(), shouldDeleteUnidentified(), getRegistrationLock(),
                        getDataStoreCode(), shouldNotifySuccessfulRegistration());
        registrationAlgorithm = new DataSetRegistrationAlgorithm(algorithmState, this);
        this.incomingDataSetFile = algorithmState.getIncomingDataSetFile();
    }

    public DataSetRegistrationHelper(File incomingDataSetFile, String shareId,
            IDelegatedActionWithResult<Boolean> cleanAftrewardsAction,
            IPreRegistrationAction preRegistrationAction,
            IPostRegistrationAction postRegistrationAction,
            IDataSetInApplicationServerRegistrator appServerRegistrator)
    {
        DataSetInformation dataSetInformation = extractDataSetInformation(incomingDataSetFile);
        dataSetInformation.setShareId(shareId);
        IDataStoreStrategy dataStoreStrategy =
                getDataStrategyStore()
                        .getDataStoreStrategy(dataSetInformation, incomingDataSetFile);
        DataSetRegistrationAlgorithmState algorithmState =
                new DataSetRegistrationAlgorithmState(incomingDataSetFile, getOpenBisService(),
                        cleanAftrewardsAction, preRegistrationAction, postRegistrationAction,
                        dataSetInformation, dataStoreStrategy, getTypeExtractor(),
                        getStorageProcessor(), getFileOperations(), getDataSetValidator(),
                        getMailClient(), shouldDeleteUnidentified(), getRegistrationLock(),
                        getDataStoreCode(), shouldNotifySuccessfulRegistration());
        registrationAlgorithm =
                new DataSetRegistrationAlgorithm(algorithmState, this, appServerRegistrator);
        this.incomingDataSetFile = algorithmState.getIncomingDataSetFile();
    }

    public DataSetRegistrationAlgorithm getRegistrationAlgorithm()
    {
        return registrationAlgorithm;
    }

    /**
     * Return the data set information.
     */
    public DataSetInformation getDataSetInformation()
    {
        return registrationAlgorithm.getDataSetInformation();
    }

    /**
     * Prepare registration of a data set.
     */
    public final void prepare()
    {
        registrationAlgorithm.prepare();
    }

    public final boolean hasDataSetBeenIdentified()
    {
        return registrationAlgorithm.hasDataSetBeenIdentified();
    }

    /**
     * Register the data set. This method is only ever called for identified data sets.
     */
    public final List<DataSetInformation> registerDataSet()
    {
        return registrationAlgorithm.registerDataSet();
    }

    /**
     * This method is only ever called for unidentified data sets.
     */
    public final void dealWithUnidentifiedDataSet()
    {
        registrationAlgorithm.dealWithUnidentifiedDataSet();
    }

    /**
     * From given <var>incomingDataSetPath</var> extracts a <code>DataSetInformation</code>.
     * 
     * @return never <code>null</code> but prefers to throw an exception.
     */
    protected DataSetInformation extractDataSetInformation(final File incomingDataSetPath)
    {
        String errorMessage =
                "Error when trying to identify data set '" + incomingDataSetPath.getAbsolutePath()
                        + "'.";
        DataSetInformation dataSetInfo = null;
        try
        {
            dataSetInfo =
                    getDataSetInfoExtractor().getDataSetInformation(incomingDataSetPath,
                            getOpenBisService());
            dataSetInfo.setInstanceUUID(getHomeDatabaseInstance().getUuid());
            if (getOperationLog().isDebugEnabled())
            {
                getOperationLog().debug(
                        String.format("Extracting data set information '%s' from incoming "
                                + "data set path '%s'", dataSetInfo, incomingDataSetPath));
            }
            return dataSetInfo;
        } catch (final HighLevelException e)
        {
            if (dataSetInfo != null)
            {
                String email = dataSetInfo.tryGetUploadingUserEmail();
                if (StringUtils.isBlank(email) == false)
                {
                    getMailClient().sendMessage(errorMessage, e.getMessage(), null, null, email);
                }
            }
            throw e;
        } catch (final RuntimeException ex)
        {
            throw new EnvironmentFailureException(errorMessage, ex);
        }
    }

    @Override
    public void rollback(DataSetRegistrationAlgorithm algo, Throwable ex)
    {
        rollback(ex);
    }

    // subclass responsibility
    protected abstract void rollback(Throwable ex);

    // state accessors
    protected abstract IEncapsulatedOpenBISService getOpenBisService();

    protected abstract ITypeExtractor getTypeExtractor();

    protected abstract IStorageProcessorTransactional getStorageProcessor();

    protected abstract IDataSetInfoExtractor getDataSetInfoExtractor();

    protected abstract DatabaseInstance getHomeDatabaseInstance();

    protected abstract Logger getOperationLog();

    protected abstract Logger getNotificationLog();

    protected abstract IMailClient getMailClient();

    protected abstract IFileOperations getFileOperations();

    protected abstract String getDataStoreCode();

    protected abstract IDataStrategyStore getDataStrategyStore();

    protected abstract IDataSetValidator getDataSetValidator();

    protected abstract Lock getRegistrationLock();

    protected abstract boolean shouldDeleteUnidentified();

    protected abstract boolean shouldNotifySuccessfulRegistration();

}