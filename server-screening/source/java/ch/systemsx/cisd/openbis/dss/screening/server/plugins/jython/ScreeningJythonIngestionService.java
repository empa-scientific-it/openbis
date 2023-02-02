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

package ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractOmniscientTopLevelDataSetRegistrator.NoOpDelegate;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v2.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.IImagingDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.openbis.dss.etl.jython.v2.ImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.jython.v2.ImagingDataSetRegistrationTransactionV2Delegate;
import ch.systemsx.cisd.openbis.dss.etl.jython.v2.JythonPlateDataSetHandlerUtils;
import ch.systemsx.cisd.openbis.dss.etl.jython.v2.JythonPlateDatasetFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.IPluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.JythonIngestionService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A version of the {@link JythonIngestionService} with extra support for screening.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ScreeningJythonIngestionService extends JythonIngestionService
{

    private static final long serialVersionUID = 1L;

    /**
     * Public constructor.
     */
    public ScreeningJythonIngestionService(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new ScreeningPluginScriptRunnerFactory(
                getScriptPathProperty(properties)));
    }

    public ScreeningJythonIngestionService(Properties properties, File storeRoot,
            IPluginScriptRunnerFactory scriptRunnerFactory)
    {
        super(properties, storeRoot, scriptRunnerFactory);
    }

    @Override
    protected DataSetRegistrationService<DataSetInformation> createRegistrationService(
            DataSetFile incoming, IDelegatedActionWithResult<Boolean> cleanUpAction,
            NoOpDelegate delegate)
    {
        IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory =
                new JythonPlateDatasetFactory(getRegistratorState(), null);

        DataSetRegistrationService<DataSetInformation> service =
                new DataSetRegistrationService<DataSetInformation>(this, incoming,
                        registrationDetailsFactory, cleanUpAction, delegate)
                    {
                        @SuppressWarnings("unchecked")
                        @Override
                        protected DataSetRegistrationTransaction<DataSetInformation> createTransaction(
                                File rollBackStackParentFolder, File workingDirectory,
                                File stagingDirectory,
                                IDataSetRegistrationDetailsFactory<DataSetInformation> factory)
                        {
                            return new ImagingDataSetRegistrationTransaction(
                                    rollBackStackParentFolder, workingDirectory, stagingDirectory,
                                    this, factory, JythonPlateDataSetHandlerUtils.parseOriginalDir(properties),
                                    userSessionToken);
                        }
                    };
        return service;
    }

    @Override
    protected IImagingDataSetRegistrationTransactionV2 createTransaction(
            DataSetRegistrationService<DataSetInformation> service)
    {
        return new ImagingDataSetRegistrationTransactionV2Delegate(
                (ImagingDataSetRegistrationTransaction) service.transaction());
    }
}
