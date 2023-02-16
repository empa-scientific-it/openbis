/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.testng.util.Strings;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.jython.IJythonInterpreter;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetFile;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v2.AbstractProgrammableTopLevelDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author jakubs
 */
public class JythonDataSetRegistrationServiceV2<T extends DataSetInformation> extends
        JythonTopLevelDataSetHandlerV2.JythonDataSetRegistrationService<T>
{
    static private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JythonDataSetRegistrationServiceV2.class);

    public static final String MAIL_CONTACT_ADDRESSES_KEY = "mail.addresses.dropbox-errors";

    public JythonDataSetRegistrationServiceV2(
            AbstractProgrammableTopLevelDataSetHandler<T> registrator,
            DataSetFile incomingDataSetFile,
            DataSetInformation userProvidedDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> globalCleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate, IJythonInterpreter interpreter,
            TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(registrator, incomingDataSetFile, userProvidedDataSetInformationOrNull,
                globalCleanAfterwardsAction, delegate, interpreter, globalState);
    }

    public DataSetRegistrationTransaction<T> getTransaction()
    {
        return transaction;
    }

    /**
     * rolls back the existing transaction
     */
    public void rollbackAndForgetTransaction()
    {
        if (transaction != null)
        {
            transaction.rollback();
            transaction = null;
        }
    }

    /**
     * Commit any scheduled changes.
     */
    @Override
    public void commit()
    {
        transaction.commit();

        logDssRegistrationResult();

        try
        {
            // Execute the clean afterwards action as successful only if no errors occurred and we
            // registered data sets
            executeGlobalCleanAfterwardsAction(false == (didErrorsArise() || transaction.isRolledback()));
        } catch (final IOExceptionUnchecked e)
        {
            final File file = transaction.getIncomingDataSetFile().getRealIncomingFile();
            final TopLevelDataSetRegistratorGlobalState globalState = getRegistratorContext().getGlobalState();

            globalState.getMailClient().sendEmailMessage("Dataset Registration Error",
                    String.format("IO Exception processing incoming folder %s: \n%s", file.getAbsolutePath(),
                            e.getMessage()),
                    null, null, getEmailAddresses());

            throw e;
        }
    }

    private String getRegistratorsEmail()
    {
        final String folderName = transaction.getIncoming().getName();
        if (!folderName.startsWith("."))
        {
            final String[] datasetInfo = folderName.split("\\+");

            if (datasetInfo.length >= 1)
            {
                final String entityKind = datasetInfo[0];

                if (entityKind.equals("O"))
                {
                    final IApplicationServerApi v3 = ServiceProvider.getV3ApplicationService();
                    final String sessionToken = transaction.getOpenBisServiceSessionToken();
                    final boolean projectSamplesEnabled = v3.getServerInformation(sessionToken)
                            .get("project-samples-enabled").equals("true");

                    if (datasetInfo.length >= 4 && projectSamplesEnabled)
                    {
                        final String sampleSpace = datasetInfo[1];
                        final String projectCode = datasetInfo[2];
                        final String sampleCode = datasetInfo[3];

                        return getSampleRegistratorsEmail(sampleSpace, projectCode, sampleCode);
                    } else if (datasetInfo.length >= 3)
                    {
                        final String sampleSpace = datasetInfo[1];
                        final String sampleCode = datasetInfo[2];

                        return getSampleRegistratorsEmail(sampleSpace, null, sampleCode);
                    } else
                    {
                        return null;
                    }
                } else if (entityKind.equals("E"))
                {
                    final String experimentSpace = datasetInfo[1];
                    final String projectCode = datasetInfo[2];
                    final String experimentCode = datasetInfo[3];

                    return getExperimentRegistratorsEmail(experimentSpace, projectCode, experimentCode);
                } else
                {
                    return null;
                }
            } else
            {
                return null;
            }
        } else
        {
            return null;
        }
    }

    private String getSampleRegistratorsEmail(final String spaceCode, final String projectCode, final String sampleCode)
    {
        final IApplicationServerApi v3 = ServiceProvider.getV3ApplicationService();
        final SampleIdentifier sampleIdentifier = new SampleIdentifier(spaceCode, projectCode, null, sampleCode);
        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withRegistrator();
        final Sample foundSample = v3.getSamples(transaction.getOpenBisServiceSessionToken(), List.of(sampleIdentifier),
                fetchOptions).get(sampleIdentifier);
        return foundSample != null ? foundSample.getRegistrator().getEmail() : null;
    }

    private String getExperimentRegistratorsEmail(final String spaceCode, final String projectCode, final String experimentCode)
    {
        final IApplicationServerApi v3 = ServiceProvider.getV3ApplicationService();
        final ExperimentIdentifier experimentIdentifier = new ExperimentIdentifier(spaceCode, projectCode,
                experimentCode);
        final ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withRegistrator();
        final Experiment foundExperiment = v3.getExperiments(transaction.getOpenBisServiceSessionToken(),
                List.of(experimentIdentifier), fetchOptions).get(experimentIdentifier);
        return foundExperiment != null ? foundExperiment.getRegistrator().getEmail() : null;
    }

    private EMailAddress[] getEmailAddresses()
    {
        final TopLevelDataSetRegistratorGlobalState globalState = getRegistratorContext().getGlobalState();
        final String registratorsEmail = getRegistratorsEmail();
        final Stream<String> registratorsEmailStream = Strings.isNullOrEmpty(registratorsEmail) ? Stream.empty()
                : Stream.of(registratorsEmail);
        final Stream<String> contactAddressEmailStream = Arrays.stream(
                globalState.getThreadParameters().getThreadProperties().getProperty(MAIL_CONTACT_ADDRESSES_KEY)
                        .split("[,;]"));
        return Stream.concat(registratorsEmailStream, contactAddressEmailStream).map(EMailAddress::new)
                .toArray(EMailAddress[]::new);
    }


    @Override
    protected void logDssRegistrationResult()
    {
        // If the transaction is not in recovery pending state, do the normal logging
        if (false == transaction.isRecoveryPending())
        {
            super.logDssRegistrationResult();
            return;
        }

        // Log that we are in recovery pending state
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Encountered errors, trying to recover.\n");
        for (Throwable error : getEncounteredErrors())
        {
            logMessage.append("\t");
            logMessage.append(error.toString());
        }
        dssRegistrationLog.info(operationLog, logMessage.toString());
    }
}
