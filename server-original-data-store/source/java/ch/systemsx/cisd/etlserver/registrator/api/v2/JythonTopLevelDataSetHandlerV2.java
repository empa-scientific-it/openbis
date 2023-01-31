/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.jython.IJythonFunction;
import ch.systemsx.cisd.common.jython.IJythonInterpreter;
import ch.systemsx.cisd.common.jython.IJythonInterpreterFactory;
import ch.systemsx.cisd.common.jython.v25.Jython25InterpreterFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.DssRegistrationLogger;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.monitor.DssRegistrationHealthMonitor;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractProgrammableTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.v2.DataSetRegistrationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Jython27FactoriesProvider;

/**
 * A top-level data set handler that runs a python (jython) script to register data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JythonTopLevelDataSetHandlerV2<T extends DataSetInformation> extends
        AbstractProgrammableTopLevelDataSetHandler<T>
{
    protected final IJythonInterpreterFactory jythonInterpreterFactory;

    // version of jython. Available options are "2.7" and "2.5". Default is jython 2.7
    public static final String JYTHON_VERSION = "plugin-jython-version";

    /**
     * Constructor.
     * 
     * @param globalState
     */
    public JythonTopLevelDataSetHandlerV2(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);

        String path =
                PropertyUtils.getMandatoryProperty(globalState.getThreadParameters()
                        .getThreadProperties(), SCRIPT_PATH_KEY);
        scriptFile = new File(path);
        if (scriptFile.isFile() == false)
        {
            throw ConfigurationFailureException.fromTemplate("Script file '%s' does not exist!",
                    path);
        }

        String jythonVersion = PropertyUtils.getProperty(globalState.getThreadParameters()
                .getThreadProperties(), JYTHON_VERSION);

        if ("2.5".equals(jythonVersion))
        {
            jythonInterpreterFactory = new Jython25InterpreterFactory();
        } else
        {
            jythonInterpreterFactory = Jython27FactoriesProvider.getInterpreterFactory();
        }

        DssRegistrationHealthMonitor.getInstance(globalState.getOpenBisService(),
                globalState.getRecoveryStateDir());

    }

    /**
     * Create a registration service that includes a python interpreter (we need the interpreter in the service so we can use it in error handling).
     */
    @Override
    protected DataSetRegistrationService<T> createDataSetRegistrationService(
            DataSetFile incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return createJythonDataSetRegistrationServiceV2(incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                jythonInterpreterFactory.createInterpreter(), getGlobalState());
    }

    /**
     * Create a Jython registration service that includes access to the interpreter.
     * 
     * @param pythonInterpreter
     */
    protected DataSetRegistrationService<T> createJythonDataSetRegistrationServiceV2(
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate, IJythonInterpreter pythonInterpreter,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        return new JythonDataSetRegistrationServiceV2<T>(this, incomingDataSetFile,
                userProvidedDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                pythonInterpreter, globalState);
    }

    private void configureEvaluator(File dataSetFile, JythonDataSetRegistrationService<T> service,
            IJythonInterpreter interpreter)
    {
        interpreter.set(INCOMING_DATA_SET_VARIABLE_NAME, dataSetFile);
        interpreter.set(STATE_VARIABLE_NAME, getGlobalState());

        if (service != null)
        {
            interpreter.set(FACTORY_VARIABLE_NAME, service.getDataSetRegistrationDetailsFactory());
        }
    }

    protected void executeJythonScript(DataSetFile dataSetFile, String scriptString,
            JythonDataSetRegistrationService<T> service)
    {
        DssRegistrationLogger logger = service.getDssRegistrationLog();

        // Configure the evaluator
        IJythonInterpreter interpreter = service.getInterpreter();

        IJavaDataSetRegistrationDropboxV2 v2Programm =
                new JythonAsJavaDataSetRegistrationDropboxV2Wrapper(interpreter);

        logger.info(operationLog, "Compile python script");

        // Invoke the evaluator
        interpreter.exec(scriptString, scriptFile.getPath());

        verifyEvaluatorHookFunctions(interpreter);

        if (false == v2Programm.isRetryFunctionDefined())
        {
            // in case when there is no retry function defined we just call the process and don't
            // try to catch any kind of exceptions
            logger.info(operationLog, "Start processing");
            v2Programm.process(wrapTransaction(service.transaction()));
        } else
        {
            executeProcessFunctionWithRetries(v2Programm,
                    (JythonDataSetRegistrationServiceV2<T>) service, dataSetFile);
        }
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
    protected boolean hasRecoveryMarkerFile(File incoming)
    {
        return getGlobalState().getStorageRecoveryManager().getProcessingMarkerFile(incoming)
                .exists();
    }

    @Override
    protected IJavaDataSetRegistrationDropboxV2 getV2DropboxProgram(
            DataSetRegistrationService<T> service)
    {
        return new JythonAsJavaDataSetRegistrationDropboxV2Wrapper(
                getInterpreterFromService(service));
    }

    @Override
    protected AbstractProgrammableTopLevelDataSetHandler<T>.RecoveryHookAdaptor getRecoveryHookAdaptor(
            File incoming)
    {
        return new RecoveryHookAdaptor(incoming)
            {
                IJavaDataSetRegistrationDropboxV2 v2ProgramInternal;

                @Override
                protected IJavaDataSetRegistrationDropboxV2 getV2DropboxProgramInternal()
                {
                    if (v2ProgramInternal == null)
                    {
                        IJythonInterpreter internalInterpreter =
                                jythonInterpreterFactory.createInterpreter();
                        // interpreter.execute script

                        configureEvaluator(incoming, null, internalInterpreter);

                        // Load the script
                        String scriptString = FileUtilities.loadToString(scriptFile);

                        // Invoke the evaluator
                        internalInterpreter.exec(scriptString, scriptFile.getPath());

                        verifyEvaluatorHookFunctions(internalInterpreter);

                        v2ProgramInternal =
                                new JythonAsJavaDataSetRegistrationDropboxV2Wrapper(
                                        internalInterpreter);
                    }
                    return v2ProgramInternal;
                }
            };
    }

    public enum JythonHookFunction
    {
        /**
         * The name of the v2 process function, that is executed during the registration. A replacement for a top-level file body code in v1.
         */
        PROCESS_FUNCTION("process", 1),

        /**
         * The name of the function to define to hook into the service rollback mechanism. V1 only.
         */
        ROLLBACK_SERVICE_FUNCTION_NAME("rollback_service", 2),

        /**
         * The name of the function to define to hook into the transaction rollback mechanism. V1 only.
         */
        ROLLBACK_TRANSACTION_FUNCTION_NAME("rollback_transaction", 4),

        /**
         * The name of the function called after successful transaction commit. V1 only.
         */
        COMMIT_TRANSACTION_FUNCTION_NAME("commit_transaction", 2),

        /**
         * The name of the function called after successful transaction commit.
         */
        POST_STORAGE_FUNCTION_NAME("post_storage", 1),

        /**
         * The name of the function called just before registration of datasets in application server.
         */
        PRE_REGISTRATION_FUNCTION_NAME("pre_metadata_registration", 1),

        /**
         * The name of the function called just after successful registration of datasets in application server.
         */
        POST_REGISTRATION_FUNCTION_NAME("post_metadata_registration", 1),

        /**
         * The name of the function to define to hook into the transaction rollback mechanism.
         */
        ROLLBACK_PRE_REGISTRATION_FUNCTION_NAME("rollback_pre_registration", 2),

        /**
         * The name of the function to define to hook into the transaction rollback mechanism.
         */
        SHOULD_RETRY_PROCESS_FUNCTION_NAME("should_retry_processing", 2),

        /**
         * The name of the function called when secondary transactions, DynamicTransactionQuery objects, fail.
         */
        DID_ENCOUNTER_SECONDARY_TRANSACTION_ERRORS_FUNCTION_NAME(
                "did_encounter_secondary_transaction_errors", 3);

        public final String name;

        int argCount;

        private JythonHookFunction(String name, int argCount)
        {
            this.name = name;
            this.argCount = argCount;

        }
    }

    protected static final String FACTORY_VARIABLE_NAME = "factory";

    /**
     * The name of the local variable under which the service is made available to the script.
     */
    protected static final String SERVICE_VARIABLE_NAME = "service";

    /**
     * The name of the local variable under which the global state
     */
    protected static final String STATE_VARIABLE_NAME = "state";

    /**
     * The name of the local variable under which the incoming directory is made available to the script.
     */
    protected static final String INCOMING_DATA_SET_VARIABLE_NAME = "incoming";

    /**
     * The name of the local variable under which the transaction is made available to the script.
     */
    protected static final String TRANSACTION_VARIABLE_NAME = "transaction";

    // The key for the script in the properties file
    public static final String SCRIPT_PATH_KEY = "script-path";

    protected final File scriptFile;

    @Override
    public void handleDataSet(DataSetFile dataSetFile, DataSetRegistrationService<T> genericService)
            throws Throwable
    {
        // Load the script
        String scriptString = FileUtilities.loadToString(scriptFile);

        JythonDataSetRegistrationService<T> service =
                (JythonDataSetRegistrationService<T>) genericService;

        waitUntilApplicationIsReady(genericService, dataSetFile);

        executeJythonScript(dataSetFile, scriptString, service);
    }

    public static IJythonFunction tryJythonFunction(IJythonInterpreter interpreter,
            JythonHookFunction functionDefinition)
    {
        return interpreter.tryJythonFunction(functionDefinition.name);
    }

    protected void verifyEvaluatorHookFunctions(IJythonInterpreter interpreter)
    {
        for (JythonHookFunction function : JythonHookFunction.values())
        {
            IJythonFunction py = tryJythonFunction(interpreter, function);
            if (py != null)
            {
                int argCount = py.getArgumentCount();
                if (argCount >= 0)
                {
                    if (argCount != function.argCount)
                    {
                        throw new IllegalArgumentException(
                                String.format(
                                        "The function %s in %s has wrong number of arguments(%s instead of %s).",
                                        function.name, scriptFile.getName(), argCount,
                                        function.argCount));
                    }
                } else
                {
                    System.err
                            .println("Possibly incorrect python code. Can't verify script correctness.");
                }
            }
        }
    }

    public abstract static class ProgrammableDropboxObjectFactory<T extends DataSetInformation>
            extends AbstractDataSetRegistrationDetailsFactory<T>
    {
        public ProgrammableDropboxObjectFactory(
                OmniscientTopLevelDataSetRegistratorState registratorState,
                DataSetInformation userProvidedDataSetInformationOrNull)
        {
            super(registratorState, userProvidedDataSetInformationOrNull);
        }

        /**
         * Factory method that creates a new registration details object.
         */
        public DataSetRegistrationDetails<T> createRegistrationDetails()
        {
            return createDataSetRegistrationDetails();
        }

        /**
         * Returns the Java class for the given class name.
         */
        public Class<?> getClass(String className)
        {
            try
            {
                return Class.forName(className);
            } catch (ClassNotFoundException ex)
            {
                return null;
            }
        }
    }

    public static class JythonDataSetRegistrationService<T extends DataSetInformation> extends
            DataSetRegistrationService<T>
    {
        private IJythonInterpreter interpreter;

        public JythonDataSetRegistrationService(
                AbstractProgrammableTopLevelDataSetHandler<T> registrator,
                DataSetFile incomingDataSetFile,
                DataSetInformation userProvidedDataSetInformationOrNull,
                IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
                ITopLevelDataSetRegistratorDelegate delegate,
                IJythonInterpreter interpreter,
                TopLevelDataSetRegistratorGlobalState globalState)
        {
            super(registrator, incomingDataSetFile, registrator
                    .createObjectFactory(userProvidedDataSetInformationOrNull),
                    globalCleanAfterwardsAction, delegate);
            interpreter.set(STATE_VARIABLE_NAME, globalState);
            this.interpreter = interpreter;
        }

        public IJythonInterpreter getInterpreter()
        {
            return interpreter;
        }

        @Override
        public void cleanAfterRegistrationIfNecessary()
        {
            super.cleanAfterRegistrationIfNecessary();

            if (interpreter != null)
            {
                interpreter.releaseResources();
            }
        }
    }

    protected IJythonInterpreter getInterpreterFromService(DataSetRegistrationService<T> service)
    {
        IJythonInterpreter interpreter = ((JythonDataSetRegistrationService<T>) service).interpreter;
        return interpreter;
    }

}
