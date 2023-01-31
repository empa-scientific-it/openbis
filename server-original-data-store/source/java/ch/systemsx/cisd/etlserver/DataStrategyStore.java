/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Default <code>IDataStrategyStore</code> implementation.
 * <p>
 * Decides which {@link IDataStoreStrategy} will be applied for an incoming data set.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class DataStrategyStore implements IDataStrategyStore
{
    static final String SUBJECT_FORMAT = "ATTENTION: experiment '%s'";

    static final String SUBJECT_SAMPLE_FORMAT = "ATTENTION: sample '%s'";

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DataStrategyStore.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataStrategyStore.class);

    private final IEncapsulatedOpenBISService limsService;

    private final IMailClient mailClient;

    private final Map<DataStoreStrategyKey, IDataStoreStrategy> dataStoreStrategies;

    private final IOpenbisWrapper openbisServiceWrapper;

    public DataStrategyStore(final IEncapsulatedOpenBISService limsService,
            final IMailClient mailClient)
    {
        this.mailClient = mailClient;
        dataStoreStrategies = createDataStoreStrategies();
        this.limsService = limsService;
        this.openbisServiceWrapper = new BasicOpenbisWrapper();
    }

    /**
     * Create version of data strategy store with the same contents as the argument, but caching openbis calls
     */
    public DataStrategyStore(final DataStrategyStore other)
    {
        this.mailClient = other.mailClient;
        this.dataStoreStrategies = other.dataStoreStrategies;
        this.limsService = other.limsService;
        this.openbisServiceWrapper = new CachedOpenbisWrapper();
    }

    private final static void putDataStoreStrategy(
            final Map<DataStoreStrategyKey, IDataStoreStrategy> map,
            final IDataStoreStrategy dataStoreStrategy)
    {
        map.put(dataStoreStrategy.getKey(), dataStoreStrategy);
    }

    private final static Map<DataStoreStrategyKey, IDataStoreStrategy> createDataStoreStrategies()
    {
        final Map<DataStoreStrategyKey, IDataStoreStrategy> map =
                new EnumMap<DataStoreStrategyKey, IDataStoreStrategy>(DataStoreStrategyKey.class);
        putDataStoreStrategy(map, new IdentifiedDataStrategy());
        putDataStoreStrategy(map, new NamedDataStrategy(DataStoreStrategyKey.UNIDENTIFIED));
        putDataStoreStrategy(map, new NamedDataStrategy(DataStoreStrategyKey.INVALID));
        return map;
    }

    final static String createInvalidSampleCodeMessage(final DataSetInformation dataSetInfo)
    {
        return "ETL server: Sample '" + dataSetInfo.getSampleIdentifier()
                + "' is not valid for experiment '" + dataSetInfo.getExperimentIdentifier()
                + "' (it has maybe been invalidated?).";
    }

    private static String createNotificationMessage(final DataSetInformation dataSetInfo,
            final File incomingDataSetPathForLogging)
    {
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        return String.format("Dataset '%s', sample identifier '%s': unknown to openBIS",
                incomingDataSetPathForLogging, sampleIdentifier);
    }

    //
    // IDataStrategyStore
    //

    @Override
    public IDataStoreStrategy getDataStoreStrategy(DataStoreStrategyKey key)
    {
        return dataStoreStrategies.get(key);
    }

    @Override
    public IDataStoreStrategy getDataStoreStrategy(final DataSetInformation dataSetInfo,
            final File incomingDataSetPath)
    {
        if (dataSetInfo == null)
        {
            return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
        }

        assertIncomingDataSetPath(incomingDataSetPath, dataSetInfo);
        injectContainerDataSet(dataSetInfo);

        String emailOrNull = dataSetInfo.tryGetUploadingUserEmail();
        ExperimentIdentifier experimentIdentifier = dataSetInfo.getExperimentIdentifier();
        final SampleIdentifier sampleIdentifier = dataSetInfo.getSampleIdentifier();
        if (sampleIdentifier == null)
        {
            Experiment experiment = tryGetExperiment(dataSetInfo);
            if (experiment == null)
            {
                error(emailOrNull, "Unknown experiment identifier '" + experimentIdentifier + "'.");
                return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
            }
            dataSetInfo.setExperiment(experiment);
        } else
        {
            Sample sample = tryGetSample(dataSetInfo);
            if (sample == null)
            {
                error(emailOrNull, createNotificationMessage(dataSetInfo, incomingDataSetPath));
                return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
            }
            dataSetInfo.setSample(sample);
            final Experiment experiment = sample.getExperiment();
            if (experiment == null)
            {
                if (sample.getSpace() == null)
                {
                    error(emailOrNull, String.format("Data set for sample '%s' can not be registered "
                            + "because the sample is a shared sample not assigned to a particular space.",
                            sampleIdentifier));
                    return dataStoreStrategies.get(DataStoreStrategyKey.UNIDENTIFIED);
                }
            } else
            {
                experimentIdentifier = new ExperimentIdentifier(experiment);
                dataSetInfo.setExperimentIdentifier(experimentIdentifier);
            }

            final IEntityProperty[] properties =
                    openbisServiceWrapper.tryGetPropertiesOfSample(sampleIdentifier);
            if (properties == null)
            {
                final Person registrator = experiment == null ? sample.getRegistrator() : experiment.getRegistrator();
                assert registrator != null : "Registrator must be known";
                final String message = createInvalidSampleCodeMessage(dataSetInfo);
                final String recipientMail = registrator.getEmail();
                if (StringUtils.isNotBlank(recipientMail))
                {
                    sendEmail(message, experimentIdentifier, sampleIdentifier, recipientMail);
                } else
                {
                    error(emailOrNull, "The registrator '" + registrator
                            + "' has a blank email, sending the following email failed:\n"
                            + message);
                }
                operationLog.error(createLogMessageForMissingSampleProperties(incomingDataSetPath,
                        experimentIdentifier, sampleIdentifier));
                return dataStoreStrategies.get(DataStoreStrategyKey.INVALID);
            }
            dataSetInfo.setSampleProperties(properties);
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(createLogMessageForIdentified(experimentIdentifier, sampleIdentifier));
        }
        return dataStoreStrategies.get(DataStoreStrategyKey.IDENTIFIED);
    }

    private String createLogMessageForIdentified(ExperimentIdentifier experimentIdentifier,
            SampleIdentifier sampleIdentifier)
    {
        String prefix = "Identified that database knows ";
        if (sampleIdentifier == null)
        {
            return prefix + "experiment '" + experimentIdentifier + "'.";
        } else if (experimentIdentifier == null)
        {
            return prefix + "sample '" + sampleIdentifier + "'.";
        }
        return prefix + "experiment '" + experimentIdentifier + "' and sample '" + sampleIdentifier + "'.";
    }

    private String createLogMessageForMissingSampleProperties(final File incomingDataSetPath,
            ExperimentIdentifier experimentIdentifier, final SampleIdentifier sampleIdentifier)
    {
        String claimedOwner;
        if (experimentIdentifier == null)
        {
            claimedOwner = String.format("sample '%s'", sampleIdentifier);
        } else
        {
            claimedOwner = String.format("experiment '%s' and sample '%s'",
                    experimentIdentifier, sampleIdentifier);
        }
        return String.format("Incoming data set '%s' claims to belong to %s, but according to the openBIS server "
                + "there is no such sample (maybe it has been deleted?). We thus consider it invalid.",
                incomingDataSetPath, claimedOwner);
    }

    public Sample tryGetSample(final DataSetInformation dataSetInfo)
    {
        Sample sample = dataSetInfo.tryToGetSample();
        if (sample == null)
        {
            sample = openbisServiceWrapper.tryGetSample(dataSetInfo.getSampleIdentifier());
        }
        return sample;
    }

    public Experiment tryGetExperiment(final DataSetInformation dataSetInfo)
    {
        Experiment experiment = dataSetInfo.tryToGetExperiment();
        if (experiment == null)
        {
            experiment = openbisServiceWrapper.tryGetExperiment(dataSetInfo.getExperimentIdentifier());
        }
        return experiment;
    }

    public void injectContainerDataSet(final DataSetInformation dataSetInfo)
    {
        String containerDatasetPermId = dataSetInfo.tryGetContainerDatasetPermId();
        if (containerDatasetPermId != null)
        {
            AbstractExternalData container =
                    openbisServiceWrapper.tryGetDataSet(containerDatasetPermId);
            if (container != null)
            {
                dataSetInfo.setContainerDataSet(container);
            }
        }
    }

    public void assertIncomingDataSetPath(final File incomingDataSetPath, final DataSetInformation dataSetInfo)
    {
        if (dataSetInfo.isNoFileDataSet())
        {
            assert incomingDataSetPath == null : "Incoming data set path for a no-file data set must be null";
        } else
        {
            assert incomingDataSetPath != null : "Incoming data set path for a normal data set can not be null.";
        }
    }

    private void error(String emailOrNull, String message)
    {
        if (emailOrNull == null)
        {
            notificationLog.error(message);
        } else
        {
            mailClient.sendMessage("Error during registration of a data set", message, null, null,
                    emailOrNull);
            operationLog.error(message);
        }
    }

    private void sendEmail(final String message, final ExperimentIdentifier experimentIdentifier,
            SampleIdentifier sampleIdentifier, final String recipientMail)
    {
        final String subject;
        if (experimentIdentifier == null)
        {
            subject = String.format(SUBJECT_SAMPLE_FORMAT, sampleIdentifier);
        } else
        {
            subject = String.format(SUBJECT_FORMAT, experimentIdentifier);
        }
        try
        {
            mailClient.sendMessage(subject, message, null, null, recipientMail);
        } catch (final EnvironmentFailureException ex)
        {
            operationLog.error(ex.getMessage());
        }
    }

    @Override
    public IDataStrategyStore getCachedDataStrategyStore()
    {
        return new DataStrategyStore(this);
    }

    private interface IOpenbisWrapper
    {
        Sample tryGetSample(final SampleIdentifier sampleIdentifier);

        AbstractExternalData tryGetDataSet(String containerDatasetPermId);

        Experiment tryGetExperiment(ExperimentIdentifier experimentIdentifier);

        IEntityProperty[] tryGetPropertiesOfSample(final SampleIdentifier sampleIdentifier);
    }

    private class BasicOpenbisWrapper implements IOpenbisWrapper
    {
        @Override
        public Sample tryGetSample(final SampleIdentifier sampleIdentifier)
        {
            try
            {
                return limsService.tryGetSampleWithExperiment(sampleIdentifier);
            } catch (UserFailureException ex)
            {
                return null;
            }
        }

        @Override
        public AbstractExternalData tryGetDataSet(String containerDatasetPermId)
        {
            return limsService.tryGetDataSet(containerDatasetPermId);
        }

        @Override
        public Experiment tryGetExperiment(ExperimentIdentifier experimentIdentifier)
        {
            return limsService.tryGetExperiment(experimentIdentifier);
        }

        @Override
        public IEntityProperty[] tryGetPropertiesOfSample(final SampleIdentifier sampleIdentifier)
        {
            return limsService.tryGetPropertiesOfSample(sampleIdentifier);
        }
    }

    private class CachedOpenbisWrapper extends BasicOpenbisWrapper
    {
        final HashMap<SampleIdentifier, Sample> sampleCache;

        final HashMap<String, AbstractExternalData> dataSetCache;

        final HashMap<ExperimentIdentifier, Experiment> experimentCache;

        final HashMap<SampleIdentifier, IEntityProperty[]> samplePropertiesCache;

        private CachedOpenbisWrapper()
        {
            this.sampleCache = new HashMap<SampleIdentifier, Sample>();
            this.dataSetCache = new HashMap<String, AbstractExternalData>();
            this.experimentCache = new HashMap<ExperimentIdentifier, Experiment>();
            this.samplePropertiesCache = new HashMap<SampleIdentifier, IEntityProperty[]>();
        }

        @Override
        public Sample tryGetSample(SampleIdentifier sampleIdentifier)
        {
            if (false == sampleCache.containsKey(sampleIdentifier))
            {
                sampleCache.put(sampleIdentifier, super.tryGetSample(sampleIdentifier));
            }
            return sampleCache.get(sampleIdentifier);
        }

        @Override
        public AbstractExternalData tryGetDataSet(String containerDatasetPermId)
        {
            if (false == dataSetCache.containsKey(containerDatasetPermId))
            {
                dataSetCache.put(containerDatasetPermId,
                        super.tryGetDataSet(containerDatasetPermId));
            }
            return dataSetCache.get(containerDatasetPermId);
        }

        @Override
        public Experiment tryGetExperiment(ExperimentIdentifier experimentIdentifier)
        {
            if (false == experimentCache.containsKey(experimentIdentifier))
            {
                experimentCache.put(experimentIdentifier,
                        super.tryGetExperiment(experimentIdentifier));
            }
            return experimentCache.get(experimentIdentifier);
        }

        @Override
        public IEntityProperty[] tryGetPropertiesOfSample(SampleIdentifier sampleIdentifier)
        {
            if (false == samplePropertiesCache.containsKey(sampleIdentifier))
            {
                samplePropertiesCache.put(sampleIdentifier,
                        super.tryGetPropertiesOfSample(sampleIdentifier));
            }
            return samplePropertiesCache.get(sampleIdentifier);
        }

    }

}