/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.io.File;

import ch.ethz.cisd.hotdeploy.PluginContainer;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractProgrammableTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetRegistrationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Pawel Glyzewski
 */
public class JavaTopLevelDataSetHandlerV2<T extends DataSetInformation> extends
        AbstractProgrammableTopLevelDataSetHandler<T>
{
    // The key for the script in the properties file
    public static final String PROGRAM_CLASS_KEY = "program-class";

    private Class<? extends IJavaDataSetRegistrationDropboxV2> programClass;

    private String className;

    private String dropboxName;

    /**
     * @param globalState
     */
    @SuppressWarnings("unchecked")
    public JavaTopLevelDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        className =
                PropertyUtils.getMandatoryProperty(globalState.getThreadParameters()
                        .getThreadProperties(), PROGRAM_CLASS_KEY);

        dropboxName = globalState.getThreadParameters().getThreadName();

        PluginContainer container =
                PluginContainer.tryGetInstance(dropboxName);

        if (container != null)
        {
            programClass = (Class<? extends IJavaDataSetRegistrationDropboxV2>) container
                    .tryGetPluginClassByClassname(className);
        }

        if (programClass == null)
        {
            try
            {
                programClass =
                        (Class<? extends IJavaDataSetRegistrationDropboxV2>) Class
                                .forName(className);
            } catch (ClassNotFoundException ex)
            {
                throw ConfigurationFailureException.fromTemplate("Class '%s' does not exist!",
                        className);
            }
        }

        DssRegistrationHealthMonitor.getInstance(globalState.getOpenBisService(),
                globalState.getRecoveryStateDir());
    }

    /**
     * V2 registration framework -- do not put files that are scheduled for recovery into the faulty paths.
     */
    @Override
    public boolean shouldNotAddToFaultyPathsOrNull(File file)
    {
        // If there is a recovery marker file, do not add the file to faulty paths.
        return hasRecoveryMarkerFile(file);
    }

    @Override
    protected void handleDataSet(DataSetFile dataSetFile, DataSetRegistrationService<T> service)
            throws Throwable
    {
        waitUntilApplicationIsReady(service, dataSetFile);

        IJavaDataSetRegistrationDropboxV2 v2Programm = getV2DropboxProgram(service);

        if (v2Programm.isRetryFunctionDefined())
        {
            executeProcessFunctionWithRetries(v2Programm,
                    (JythonDataSetRegistrationServiceV2<T>) service, dataSetFile);
        } else
        {
            // in case when there is no retry function defined we just call the process and don't
            // try to catch any kind of exceptions
            service.getDssRegistrationLog().info(operationLog, "Start processing");
            v2Programm.process(wrapTransaction(service.transaction()));
        }
    }

    @Override
    protected RecoveryHookAdaptor getRecoveryHookAdaptor(File incoming)
    {
        return new RecoveryHookAdaptor(incoming)
            {
                IJavaDataSetRegistrationDropboxV2 v2ProgramInternal;

                @Override
                protected IJavaDataSetRegistrationDropboxV2 getV2DropboxProgramInternal()
                {
                    if (v2ProgramInternal == null)
                    {
                        v2ProgramInternal = getV2DropboxProgram(null);
                    }

                    return v2ProgramInternal;
                }
            };

    }

    /**
     * Create a V2 registration service.
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new DataSetRegistrationServiceV2<T>(this, incomingDataSetFile,
                userProvidedDataSetInformationOrNull, cleanAfterwardsAction, delegate);
    }

    @Override
    protected boolean hasRecoveryMarkerFile(File incoming)
    {
        return getGlobalState().getStorageRecoveryManager().getProcessingMarkerFile(incoming)
                .exists();
    }

    @Override
    protected IJavaDataSetRegistrationDropboxV2 getV2DropboxProgram(
            DataSetRegistrationService<T> service)
    {
        try
        {

            PluginContainer container =
                    PluginContainer.tryGetInstance(dropboxName);

            if (container != null)
            {
                @SuppressWarnings("unchecked")
                Class<? extends IJavaDataSetRegistrationDropboxV2> clazz =
                        (Class<? extends IJavaDataSetRegistrationDropboxV2>) container
                                .tryGetPluginClassByClassname(className);
                if (clazz != null)
                {
                    return clazz.newInstance();
                }
            }

            return programClass.newInstance();
        } catch (InstantiationException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (IllegalAccessException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
