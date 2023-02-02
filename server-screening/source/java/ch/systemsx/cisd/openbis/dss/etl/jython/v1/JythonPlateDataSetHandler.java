package ch.systemsx.cisd.openbis.dss.etl.jython.v1;

import java.io.File;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Jython dropbox for HCS and Microscopy image datasets.
 * 
 * @author Tomasz Pylak
 */
public class JythonPlateDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
{
    private final String originalDirName;

    public JythonPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
        originalDirName =
                JythonPlateDataSetHandlerUtils.parseOriginalDir(globalState.getThreadParameters()
                        .getThreadProperties());
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    protected IDataSetRegistrationDetailsFactory<DataSetInformation> createObjectFactory(
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return new JythonPlateDatasetFactory(getRegistratorState(),
                userProvidedDataSetInformationOrNull);
    }

    @Override
    protected DataSetRegistrationService<DataSetInformation> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new JythonDataSetRegistrationService<DataSetInformation>(this, incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                jythonInterpreterFactory.createInterpreter(), getGlobalState())
            {
                @SuppressWarnings("unchecked")
                @Override
                protected DataSetRegistrationTransaction<DataSetInformation> createTransaction(
                        File rollBackStackParentFolder,
                        File workingDirectory,
                        File stagingDirectory,
                        IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory)
                {
                    return new ImagingDataSetRegistrationTransaction(rollBackStackParentFolder,
                            workingDirectory, stagingDirectory, this, registrationDetailsFactory,
                            originalDirName);
                }
            };
    }
}
