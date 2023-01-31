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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.DataSetRegistrationAlgorithm;
import ch.systemsx.cisd.etlserver.IExtensibleDataSetHandler;
import ch.systemsx.cisd.etlserver.entityregistration.SampleAndDataSetControlFileProcessor.ControlFileRegistrationProperties;
import ch.systemsx.cisd.etlserver.entityregistration.SampleDataSetPair.SampleDataSetPairProcessing;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Utitlity class for registering one sample/dataset combination.
 * <p>
 * The processingApplied state of the sampleDataSetPair is updated upon completion of this object's register method.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetRegistrator extends AbstractSampleAndDataSetProcessor implements
        DataSetRegistrationAlgorithm.IDataSetInApplicationServerRegistrator
{

    private final ControlFileRegistrationProperties properties;

    private final SampleDataSetPair sampleDataSetPair;

    // State that is updated during the registration

    // The file/folder to register
    private File dataSetFile;

    // The sample, if the sample exists, null otherwise.
    private Sample sampleOrNull;

    private boolean didSucceed = false;

    private IRegistrationStatus failureException = null;

    SampleAndDataSetRegistrator(File folder, ControlFileRegistrationProperties properties,
            SampleDataSetPair sampleDataSetPair)
    {
        super(properties.getGlobalProperties(), folder);
        this.properties = properties;
        this.sampleDataSetPair = sampleDataSetPair;
    }

    /**
     * Commit this line. If there was a failure return the exception, otherwise return null.
     * 
     * @return An exception if one was encountered, otherwise null.
     */
    public IRegistrationStatus register()
    {
        dataSetFile = new File(folder, sampleDataSetPair.getFolderName());
        try
        {
            checkDataSetFileNotEmpty();
            injectExperiment();
            retrieveSampleOrNull();
        } catch (UserFailureException ex)
        {
            sampleDataSetPair.setProcessingApplied(SampleDataSetPairProcessing.FAILED);
            return new RegistrationError(ex);
        }

        try
        {
            checkConformanceToMode();
        } catch (UserFailureException ex)
        {
            sampleDataSetPair.setProcessingApplied(SampleDataSetPairProcessing.SKIPPED);
            return new RegistrationError(ex);
        }

        if (globalState.getDelegator() instanceof IExtensibleDataSetHandler)
        {
            IExtensibleDataSetHandler handler =
                    (IExtensibleDataSetHandler) globalState.getDelegator();
            handler.handleDataSet(dataSetFile, sampleDataSetPair.getDataSetInformation(), this);
            if (didSucceed)
            {
                logDataRegistered();

                if (isSampleKnown())
                {
                    sampleDataSetPair
                            .setProcessingApplied(SampleDataSetPairProcessing.UPDATED_SAMPLE_REGISTERED_DATA_SET);
                } else
                {
                    sampleDataSetPair
                            .setProcessingApplied(SampleDataSetPairProcessing.REGISTERED_SAMPLE_AND_DATA_SET);
                }
            }
        }
        if (null == failureException)
        {
            return new RegistrationSuccess(sampleDataSetPair.getTokens());
        } else
        {
            sampleDataSetPair.setProcessingApplied(SampleDataSetPairProcessing.FAILED);
            return failureException;
        }
    }

    /**
     * Return the file that we treat as a data set. This is only valid *after* register has been called.
     */
    public File getDataSetFile()
    {
        return dataSetFile;
    }

    /**
     * If we are in REJECT_EXISTING, the sample should not exist. If we are in REJECT_NONEXISTING, the sample should exist.
     */
    private void checkConformanceToMode()
    {
        switch (globalState.getSampleRegistrationMode())
        {
            case IGNORE_EXISTING:
                if (isSampleKnown())
                {
                    throw new UserFailureException("This sample has already been registered.");
                }
                break;
            case REJECT_NONEXISTING:
                if (false == isSampleKnown())
                {
                    throw new UserFailureException(
                            "This drop box expects existing samples. This sample does not exist.");
                }
                break;
            case ACCEPT_ALL:
                break;
        }
    }

    private void checkDataSetFileNotEmpty()
    {
        if (0 == dataSetFile.list().length)
        {
            throw new UserFailureException("The data set folder cannot be empty");
        }
    }

    private void retrieveSampleOrNull()
    {
        String sampleIdString = sampleDataSetPair.getNewSample().getIdentifier();
        if (null == sampleIdString)
        {
            throw new UserFailureException("A sample identifier must be specified");
        }

        SampleIdentifier sampleIdentifier =
                new SampleIdentifierFactory(sampleIdString).createIdentifier();
        if (null == sampleIdentifier)
        {
            throw new UserFailureException(
                    "The sample identifier does not conform to the sample identifier format");
        }

        sampleOrNull = globalState.getOpenbisService().tryGetSampleWithExperiment(sampleIdentifier);
    }

    private void injectExperiment()
    {
        ExperimentIdentifier experimentId =
                sampleDataSetPair.getDataSetInformation().getExperimentIdentifier();
        if (null == experimentId)
        {
            return;
        }

        Experiment experiment = globalState.getOpenbisService().tryGetExperiment(experimentId);
        if (null == experiment)
        {
            throw new UserFailureException("The experiment with identifier " + experimentId
                    + " does not exist");
        }
        sampleDataSetPair.getDataSetInformation().setExperiment(experiment);
    }

    private void logDataRegistered()
    {
        String messageFormat =
                (isSampleKnown()) ? "Updated sample, registered data set %s"
                        : "Registered sample/data set pair %s";
        String message = String.format(messageFormat, sampleDataSetPair);
        logInfo(message);
    }

    @Override
    public void registerDataSetInApplicationServer(NewExternalData data) throws Throwable
    {
        syncDataToDataSetProperties(data);
        try
        {
            Sample sample;
            if (isSampleKnown())
            {
                sample =
                        globalState.getOpenbisService().updateSampleAndRegisterDataSet(
                                sampleDataSetPair.getSampleUpdates(sampleOrNull), data);
            } else
            {
                sample =
                        globalState.getOpenbisService().registerSampleAndDataSet(
                                sampleDataSetPair.getNewSample(), data,
                                properties.getUser().getUserId());
            }
            // Update the data set information -- it will be needed later in the data set processing
            DataSetInformation dataSetInformation = sampleDataSetPair.getDataSetInformation();
            dataSetInformation.setSampleCode(sample.getCode());
            dataSetInformation.setSample(sample);
            didSucceed = true;
        } catch (UserFailureException e)
        {
            didSucceed = false;
            failureException = new RegistrationError(e);
            throw e;
        } catch (Throwable e)
        {
            logError("Could not register " + sampleDataSetPair + " in openBIS", e);
            didSucceed = false;
            failureException = new RegistrationError(e);
            throw e;
        }
    }

    private void syncDataToDataSetProperties(NewExternalData data)
    {
        data.setDataSetType(properties.getDataSetType());
        if (null != sampleDataSetPair.getFileFormatTypeCode())
        {
            FileFormatType fileFormatType =
                    new FileFormatType(sampleDataSetPair.getFileFormatTypeCode());

            data.setFileFormatType(fileFormatType);
        }
        if (null != data.getDataSetKind())
        {
            data.setDataSetKind(data.getDataSetKind());
        }
    }

    /**
     * Is the sample known to the database?
     */
    private boolean isSampleKnown()
    {
        return sampleOrNull != null;
    }

}
