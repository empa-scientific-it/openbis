/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AttachmentRegistrationHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.queue.ConsumerQueue;
import ch.systemsx.cisd.openbis.generic.client.web.server.queue.ConsumerTask;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueCodeViolationException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueCodeViolationExceptionAbstract;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.IEntityImportService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AsyncBatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialBatchUpdateResultMessage;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewEntitiesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.BatchSamplesOperation;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.SampleCodeGenerator;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.MaterialUploadSectionsParser;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.MaterialUploadSectionsParser.BatchMaterialsOperation;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServerInternal;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The {@link IGenericClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.GENERIC_PLUGIN_SERVICE)
public class GenericClientService extends AbstractClientService implements IGenericClientService, IEntityImportService
{
    @Resource(name = ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServerInternal genericServer;

    @Resource(name = "registration-queue")
    private ConsumerQueue asyncRegistrationQueue;

    public GenericClientService()
    {
    }

    @Private
    protected GenericClientService(final IGenericServerInternal genericServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.genericServer = genericServer;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return genericServer;
    }

    //
    // IGenericClientService
    //

    @Override
    public final SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final SampleParentWithDerived sampleParentWithDerived =
                    genericServer.getSampleInfo(sessionToken, sampleId);
            transformXML(sampleParentWithDerived.getParent());
            return sampleParentWithDerived;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
    public final Sample getSampleInfo(final TechId sampleId)
    {
        return getSampleGenerationInfo(sampleId).getParent();
    }

    @Override
    public final Sample registerSample(final String sessionKey, final NewSample newSample)
    {
        final String sessionToken = getSessionToken();
        class SampleRegistrationHelper extends AttachmentRegistrationHelper
        {
            Sample sample;

            @Override
            public void register(Collection<NewAttachment> attachments)
            {
                sample = genericServer.registerSample(sessionToken, newSample, attachments);
            }
        }
        final SampleRegistrationHelper sampleRegistrationHelper = new SampleRegistrationHelper();
        sampleRegistrationHelper.process(sessionKey, getHttpSession(), newSample.getAttachments());
        return sampleRegistrationHelper.sample;
    }

    @Override
    public final List<BatchRegistrationResult> registerSamplesWithSilentOverrides(
            final SampleType sampleType,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final String sessionKey,
            final boolean async,
            final String userEmail,
            final String defaultGroupIdentifier,
            final boolean updateExisting)
    {
        final boolean isAutoGenerateCodes = defaultGroupIdentifier != null;
        final HttpSession httpSession = getHttpSession();
        final String sessionToken = getSessionToken();
        final boolean isCreateContinuousSampleCodes = shouldContinuousSampleCodesCreated();

        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncSamplesTask = null;
        try
        {
            final BatchOperationKind operationKind = updateExisting ? BatchOperationKind.UPDATE : BatchOperationKind.REGISTRATION;
            uploadedFiles = getUploadedFiles(sessionKey, httpSession);
            BatchSamplesOperation info =
                    parseSamples(sampleType, spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull, uploadedFiles,
                            defaultGroupIdentifier, isAutoGenerateCodes,
                            true, null,
                            operationKind, sessionToken);

            if (async)
            {
                asyncSamplesTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Sample Batch Registration";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            // Some stuff is repeated on the async executor, this is expected
                            BatchSamplesOperation asyncInfo =
                                    parseSamples(sampleType, spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull,
                                            this.getFilesForTask(), defaultGroupIdentifier,
                                            isAutoGenerateCodes, true, null,
                                            operationKind, sessionToken);
                            // Execute task
                            genericServer.registerOrUpdateSamples(sessionToken, asyncInfo.getSamples());
                            if (isAutoGenerateCodes && isCreateContinuousSampleCodes)
                            {
                                updateTemporaryCodes(sessionToken, asyncInfo);
                            }
                        }
                    };

                String fileName = info.getResultList().get(0).getFileName();
                return AsyncBatchRegistrationResult.singletonList(fileName);
            } else
            {
                genericServer.registerOrUpdateSamples(sessionToken, info.getSamples());
                if (isAutoGenerateCodes && isCreateContinuousSampleCodes)
                {
                    updateTemporaryCodes(sessionToken, info);
                }
                return info.getResultList();
            }
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            if (e.getCause() instanceof SampleUniqueCodeViolationExceptionAbstract)
            {
                SampleUniqueCodeViolationExceptionAbstract codeException =
                        (SampleUniqueCodeViolationExceptionAbstract) e.getCause();

                if (isAutoGenerateCodes)
                {
                    throw new UserFailureException(
                            String.format(
                                    "Import failed because the autogenerated codes are no longer unique. Somebody has created a sample (code: %s) that matched one of the autogenerated codes. Please run the import once again.",
                                    codeException.getSampleCode()));
                } else
                {
                    throw new UserFailureException(String.format(
                            "Import failed because sample (code: %s) already exists.",
                            codeException.getSampleCode()));
                }
            } else
            {
                throw UserFailureExceptionTranslator.translate(e);
            }
        } finally
        {
            if (async && (asyncSamplesTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncSamplesTask);
            } else
            {
                cleanUploadedFiles(sessionKey, httpSession, uploadedFiles);
            }
        }
    }

    @Override
    public final List<BatchRegistrationResult> registerSamples(
            final SampleType sampleType,
            final String sessionKey,
            final boolean async,
            final String userEmail,
            final String defaultGroupIdentifier,
            final boolean updateExisting)
    {
        return registerSamplesWithSilentOverrides(sampleType, null, null, sessionKey, async, userEmail, defaultGroupIdentifier, updateExisting);
    }

    private boolean shouldContinuousSampleCodesCreated()
    {
        return PropertyUtils.getBoolean(this.getServiceProperties(), Constants.CREATE_CONTINUOUS_SAMPLES_CODES_KEY, false);
    }

    private void updateTemporaryCodes(final String sessionToken, BatchSamplesOperation info)
    {
        // TODO FJE: Do the update in single queue which is also fed by the
        // BatchSampleRegistrationTempCodeUpdaterTask
        // 1. Fill Structure
        Map<String, List<String>> sampleTypeCodeToTemporaryIdentifiers = new HashMap<String, List<String>>();
        List<NewSamplesWithTypes> samples = info.getSamples();

        for (NewSamplesWithTypes newSamplesWithTypes : samples)
        {
            String sampleTypeCode = newSamplesWithTypes.getEntityType().getCode();
            for (NewSample newSample : newSamplesWithTypes.getNewEntities())
            {
                List<String> temporaryIdentifiers = sampleTypeCodeToTemporaryIdentifiers.get(sampleTypeCode);
                if (temporaryIdentifiers == null)
                {
                    temporaryIdentifiers = new ArrayList<String>();
                    sampleTypeCodeToTemporaryIdentifiers.put(sampleTypeCode, temporaryIdentifiers);
                }
                temporaryIdentifiers.add(newSample.getIdentifier());
            }
        }

        // 2. Update samples
        while (true)
        {
            try
            {
                genericServer.updateTemporaryCodes(sessionToken, sampleTypeCodeToTemporaryIdentifiers);
                return;
            } catch (RuntimeException ex)
            {
                Throwable originalException = ExceptionUtils.getEndOfChain(ex);
                if (originalException instanceof SampleUniqueCodeViolationException == false)
                {
                    throw ex;
                }
            }
        }
    }

    @Override
    public final List<BatchRegistrationResult> updateSamplesWithSilentOverrides(
            final SampleType sampleType,
            final String spaceIdentifierSilentOverrideOrNull,
            final String experimentIdentifierSilentOverrideOrNull,
            final String sessionKey,
            boolean async,
            final String userEmail,
            final String defaultGroupIdentifier)
    {
        final HttpSession httpSession = getHttpSession();
        final String sessionToken = getSessionToken();
        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncSamplesTask = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, httpSession);
            BatchSamplesOperation info =
                    parseSamples(sampleType, spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull, uploadedFiles,
                            defaultGroupIdentifier, false, true, null, BatchOperationKind.UPDATE,
                            sessionToken);

            if (async)
            {
                asyncSamplesTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Sample Batch Update";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            // Some stuff is repeated on the async executor, this is expected
                            BatchSamplesOperation asyncInfo =
                                    parseSamples(sampleType, spaceIdentifierSilentOverrideOrNull, experimentIdentifierSilentOverrideOrNull,
                                            this.getFilesForTask(), defaultGroupIdentifier, false, true, null,
                                            BatchOperationKind.UPDATE, sessionToken);
                            // Execute task
                            genericServer.updateSamples(sessionToken, asyncInfo.getSamples());
                        }
                    };

                String fileName = info.getResultList().get(0).getFileName();
                return AsyncBatchRegistrationResult.singletonList(fileName);
            } else
            {
                genericServer.updateSamples(sessionToken, info.getSamples());
                return info.getResultList();
            }
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            if (async && (asyncSamplesTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncSamplesTask);
            } else
            {
                cleanUploadedFiles(sessionKey, httpSession, uploadedFiles);
            }
        }
    }

    @Override
    public final List<BatchRegistrationResult> updateSamples(
            final SampleType sampleType,
            final String sessionKey,
            boolean async,
            final String userEmail,
            final String defaultGroupIdentifier)
    {
        return updateSamplesWithSilentOverrides(sampleType, null, null, sessionKey, async, userEmail, defaultGroupIdentifier);
    }

    @Override
    public final List<BatchRegistrationResult> registerOrUpdateSamplesAndMaterials(
            final String sessionKey,
            final String defaultGroupIdentifier,
            final boolean updateExisting,
            final boolean async,
            final String userEmail) throws UserFailureException
    {
        final BatchOperationKind operationKind = updateExisting ? BatchOperationKind.UPDATE : BatchOperationKind.REGISTRATION;
        final String sessionToken = getSessionToken();
        final HttpSession session = getHttpSession();
        final SampleType sampleType = new SampleType();
        sampleType.setCode(EntityType.DEFINED_IN_FILE);
        ConsumerTask asyncGeneralTask = null;
        UploadedFilesBean uploadedFiles = null;

        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);

            BatchSamplesOperation samplesInfo =
                    parseSamples(sampleType, null, null, uploadedFiles, defaultGroupIdentifier, defaultGroupIdentifier != null, true, "SAMPLES",
                            operationKind,
                            sessionToken);

            final MaterialType materialType = new MaterialType();
            materialType.setCode(EntityType.DEFINED_IN_FILE);
            BatchMaterialsOperation materialsInfo = parseMaterials(session, uploadedFiles, materialType, "MATERIALS", updateExisting);

            if (async)
            {
                asyncGeneralTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "General Batch Import";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            // Some stuff is repeated on the async executor, this is expected
                            BatchSamplesOperation asyncSamplesInfo =
                                    parseSamples(sampleType, null, null, this.getFilesForTask(), defaultGroupIdentifier,
                                            defaultGroupIdentifier != null,
                                            true,
                                            "SAMPLES", operationKind, sessionToken);
                            BatchMaterialsOperation asyncMaterialsInfo =
                                    parseMaterials(session, this.getFilesForTask(), materialType, "MATERIALS", updateExisting);
                            // Execute task
                            genericServer.registerOrUpdateSamplesAndMaterials(sessionToken, asyncSamplesInfo.getSamples(),
                                    asyncMaterialsInfo.getMaterials());
                        }
                    };

                String fileName = uploadedFiles.iterable().iterator().next().getOriginalFilename();
                return AsyncBatchRegistrationResult.singletonList(fileName);
            } else
            {
                genericServer.registerOrUpdateSamplesAndMaterials(sessionToken, samplesInfo.getSamples(), materialsInfo.getMaterials());
                List<BatchRegistrationResult> results = new ArrayList<BatchRegistrationResult>();
                results.addAll(materialsInfo.getResultList());
                results.addAll(samplesInfo.getResultList());
                return results;
            }
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            if (async && (asyncGeneralTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncGeneralTask);
            } else
            {
                cleanUploadedFiles(sessionKey, session, uploadedFiles);
            }
        }
    }

    @Override
    public final AbstractExternalData getDataSetInfo(final TechId datasetId)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final AbstractExternalData dataset = genericServer.getDataSetInfo(sessionToken, datasetId);
            transformXML(dataset);
            return dataset;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
    public Experiment registerExperiment(final String attachmentsSessionKey,
            final String samplesSessionKey, final NewExperiment experiment)
    {
        final String sessionToken = getSessionToken();
        boolean autoGenerateCodes = experiment.isGenerateCodes();
        BatchSamplesOperation parsedSamples = null;
        if (experiment.isRegisterSamples())
        {
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier();
            parsedSamples = parseSamples(
                    experiment.getSampleType(),
                    samplesSessionKey,
                    new SpaceIdentifier(identifier.getSpaceCode()).toString(), autoGenerateCodes,
                    false, null, BatchOperationKind.REGISTRATION);
            experiment.setNewSamples(parsedSamples.getSamples());
            experiment.setSamples(parsedSamples.getCodes());
        }
        class ExperimentRegistrationHelper extends AttachmentRegistrationHelper
        {
            Experiment exp;

            @Override
            public void register(Collection<NewAttachment> attachments)
            {
                exp = genericServer.registerExperiment(sessionToken, experiment, attachments);
            }
        }
        ExperimentRegistrationHelper helper = new ExperimentRegistrationHelper();
        helper.process(attachmentsSessionKey, getHttpSession(), experiment.getAttachments());
        if (autoGenerateCodes && shouldContinuousSampleCodesCreated() && parsedSamples != null)
        {
            updateTemporaryCodes(sessionToken, parsedSamples);
        }
        return helper.exp;
    }

    private BatchSamplesOperation parseSamples(final SampleType sampleType, String spaceIdentifierSilentOverrideOrNull,
            String experimentIdentifierSilentOverrideOrNull,
            UploadedFilesBean uploadedFiles,
            String defaultGroupIdentifier, final boolean isAutoGenerateCodes,
            final boolean allowExperiments, String excelSheetName, BatchOperationKind operationKind, String sessionToken)
    {
        boolean updateExisting = (operationKind == BatchOperationKind.UPDATE);
        boolean isCreateContinuousSampleCodes = shouldContinuousSampleCodesCreated();
        SampleCodeGenerator sampleCodeGeneratorOrNull =
                tryGetSampleCodeGenerator(isAutoGenerateCodes, isCreateContinuousSampleCodes, sampleType.getGeneratedCodePrefix(), sessionToken);
        Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
        for (IUncheckedMultipartFile f : uploadedFiles.iterable())
        {
            files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
        }
        boolean projectSamplesEnabled = genericServer.isProjectSamplesEnabled(sessionToken);

        BatchSamplesOperation batchSamplesOperation =
                SampleUploadSectionsParser.prepareSamples(projectSamplesEnabled, sampleType, spaceIdentifierSilentOverrideOrNull,
                        experimentIdentifierSilentOverrideOrNull,
                        files,
                        defaultGroupIdentifier, sampleCodeGeneratorOrNull, allowExperiments,
                        excelSheetName, operationKind);

        validateAutoGeneratedTypes(isAutoGenerateCodes, isCreateContinuousSampleCodes, batchSamplesOperation);

        setUpdatePossibility(batchSamplesOperation.getSamples(), updateExisting);
        return batchSamplesOperation;

    }

    private void validateAutoGeneratedTypes(final boolean isAutoGenerateCodes,
            final boolean isCreateContinuousSampleCodes,
            final BatchSamplesOperation batchSamplesOperation)
    {
        // Check that types contain different prefixes so they don't collide, this is a limitation of the current implementation
        if (isAutoGenerateCodes && isCreateContinuousSampleCodes)
        {
            Map<String, Integer> prefixes = new HashMap<String, Integer>();
            List<SampleType> sampleTypes = this.commonClientService.listSampleTypes();

            // Obtain used types
            Set<String> usedSampleTypeCodes = new HashSet<String>();
            for (NewSamplesWithTypes newSampleWithType : batchSamplesOperation.getSamples())
            {
                usedSampleTypeCodes.add(newSampleWithType.getEntityType().getCode());
            }

            for (SampleType auxSampleType : sampleTypes)
            {
                if (usedSampleTypeCodes.contains(auxSampleType.getCode()))
                {
                    Integer repetitions = prefixes.get(auxSampleType.getGeneratedCodePrefix());
                    if (repetitions == null)
                    {
                        repetitions = 0;
                    }
                    repetitions++;
                    prefixes.put(auxSampleType.getGeneratedCodePrefix(), repetitions);

                    if (repetitions > 1)
                    {
                        throw new UserFailureException("The prefix '" + auxSampleType.getGeneratedCodePrefix()
                                + "' found at " + auxSampleType.getCode() + " it's alreay used in other type, "
                                + "please change this to use continuous sample type codes by type.");
                    }
                }
            }
        }
    }

    private BatchSamplesOperation parseSamples(
            final SampleType sampleType,
            final String sessionKey,
            String defaultGroupIdentifier,
            final boolean isAutoGenerateCodes,
            final boolean allowExperiments,
            String excelSheetName,
            BatchOperationKind operationKind)
    {
        HttpSession httpSession = getHttpSession();
        String sessionToken = getSessionToken();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, httpSession);
            return parseSamples(sampleType, null, null, uploadedFiles, defaultGroupIdentifier, isAutoGenerateCodes, allowExperiments, excelSheetName,
                    operationKind, sessionToken);
        } finally
        {
            cleanUploadedFiles(sessionKey, httpSession, uploadedFiles);
        }
    }

    private static void setUpdatePossibility(
            List<? extends NewEntitiesWithTypes<?, ?>> batchOperation, boolean updateExisting)
    {
        for (NewEntitiesWithTypes<?, ?> entitiesWithTypes : batchOperation)
        {
            entitiesWithTypes.setAllowUpdateIfExist(updateExisting);
        }
    }

    private SampleCodeGenerator tryGetSampleCodeGenerator(boolean isAutoGenerateCodes,
            final boolean isCreateContinuousSampleCodes, final String codePrefix, final String sessionToken)
    {
        if (isAutoGenerateCodes)
        {
            return new SampleCodeGenerator()
                {
                    @Override
                    public List<String> generateCodes(int size)
                    {
                        if (isCreateContinuousSampleCodes)
                        {
                            return genericServer.generateTemporaryCodes(sessionToken, codePrefix,
                                    EntityKind.SAMPLE, size);
                        } else
                        {
                            return genericServer.generateCodes(sessionToken, codePrefix,
                                    EntityKind.SAMPLE, size);
                        }

                    }
                };
        } else
        {
            return null;
        }
    }

    @Override
    public final List<BatchRegistrationResult> registerMaterials(
            final MaterialType materialType,
            final boolean updateExisting,
            final String sessionKey,
            final boolean async,
            final String userEmail)
    {
        final String sessionToken = getSessionToken();
        final HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncMaterialTask = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);
            BatchMaterialsOperation results = parseMaterials(session, uploadedFiles, materialType, null, updateExisting);

            String fileName = results.getResultList().get(0).getFileName();
            List<NewMaterialsWithTypes> materials = results.getMaterials();

            if (async)
            {
                asyncMaterialTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Material Batch Registration";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            // Some stuff is repeated on the async executor, this is expected
                            BatchMaterialsOperation asyncResults =
                                    parseMaterials(session, this.getFilesForTask(), materialType, null, updateExisting);
                            List<NewMaterialsWithTypes> asyncMaterials = asyncResults.getMaterials();
                            // Execute task
                            genericServer.registerOrUpdateMaterials(sessionToken, asyncMaterials);
                        }
                    };

                return AsyncBatchRegistrationResult.singletonList(fileName);
            } else
            {
                genericServer.registerOrUpdateMaterials(sessionToken, materials);
                return results.getResultList();
            }
        } finally
        {
            if (async && (asyncMaterialTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncMaterialTask);
            } else
            {
                cleanUploadedFiles(sessionKey, session, uploadedFiles);
            }
        }
    }

    @Override
    public List<BatchRegistrationResult> updateMaterials(
            final MaterialType materialType,
            final String sessionKey,
            final boolean ignoreUnregisteredMaterials,
            final boolean async,
            final String userEmail)
    {
        final String sessionToken = getSessionToken();
        final HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncMaterialTask = null;

        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);
            BatchMaterialsOperation results = parseMaterials(session, uploadedFiles, materialType, null, true);
            String fileName = results.getResultList().get(0).getFileName();

            if (async)
            {
                asyncMaterialTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Material Batch Update";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            try
                            {
                                // Some stuff is repeated on the async executor, this is expected
                                BatchMaterialsOperation asyncResults = parseMaterials(session, this.getFilesForTask(), materialType, null, true);
                                // Execute task
                                int updateCount =
                                        genericServer.updateMaterials(sessionToken, asyncResults.getMaterials(), ignoreUnregisteredMaterials);
                                MaterialBatchUpdateResultMessage message =
                                        new MaterialBatchUpdateResultMessage(asyncResults.getMaterials(), updateCount, ignoreUnregisteredMaterials);
                                writer.write(message.toString());
                            } catch (IOException e)
                            {
                                CheckedExceptionTunnel.wrapIfNecessary(e);
                            }
                        }
                    };

                return AsyncBatchRegistrationResult.singletonList(fileName);
            } else
            {
                int updateCount = genericServer.updateMaterials(sessionToken, results.getMaterials(), ignoreUnregisteredMaterials);
                MaterialBatchUpdateResultMessage message =
                        new MaterialBatchUpdateResultMessage(results.getMaterials(), updateCount, ignoreUnregisteredMaterials);
                return Arrays.asList(new BatchRegistrationResult(fileName, message.toString()));
            }
        } finally
        {
            if (async && (asyncMaterialTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncMaterialTask);
            } else
            {
                cleanUploadedFiles(sessionKey, session, uploadedFiles);
            }
        }

    }

    @Override
    public List<BatchRegistrationResult> updateExperiments(final ExperimentType experimentType,
            final String sessionKey, final boolean async, final String userEmail)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncExperimentTask = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);
            Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
            for (IUncheckedMultipartFile f : uploadedFiles.iterable())
            {
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
            }
            UpdatedExperimentLoader loader = new UpdatedExperimentLoader();
            loader.load(files);
            // Update the identifiers using the default project and space when possible
            final String sessionToken = getSessionToken();
            applyDefaultSpaceProjectToExperiments(loader.getNewBasicExperiments(), sessionToken);
            final UpdatedExperimentsWithType updatedExperiments = new UpdatedExperimentsWithType(experimentType, loader.getNewBasicExperiments());

            if (async)
            {
                asyncExperimentTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Experiment Batch Update";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            // Some stuff is repeated on the async executor, this is expected
                            final Collection<NamedInputStream> asyncFiles = new ArrayList<NamedInputStream>(this.getFilesForTask().size());
                            for (IUncheckedMultipartFile f : this.getFilesForTask().iterable())
                            {
                                asyncFiles.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
                            }
                            UpdatedExperimentLoader loaderAsync = new UpdatedExperimentLoader();
                            loaderAsync.load(asyncFiles);
                            applyDefaultSpaceProjectToExperiments(loaderAsync.getNewBasicExperiments(), sessionToken);
                            final UpdatedExperimentsWithType updatedExperimentsAsync =
                                    new UpdatedExperimentsWithType(experimentType, loaderAsync.getNewBasicExperiments());
                            // Execute task
                            genericServer.updateExperiments(sessionToken, updatedExperimentsAsync);
                        }
                    };

                String fileName = loader.getResults().get(0).getFileName();
                List<BatchRegistrationResult> batchRegistrationResults = AsyncBatchRegistrationResult.singletonList(fileName);
                return batchRegistrationResults;
            } else
            {
                genericServer.updateExperiments(sessionToken, updatedExperiments);
                return loader.getResults();
            }
        } finally
        {
            if (async && (asyncExperimentTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncExperimentTask);
            } else
            {
                cleanUploadedFiles(sessionKey, session, uploadedFiles);
            }
        }
    }

    private final ExperimentLoader getExperimentsFromFiles(UploadedFilesBean uploadedFiles)
    {
        Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
        for (IUncheckedMultipartFile f : uploadedFiles.iterable())
        {
            files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
        }
        ExperimentLoader loader = new ExperimentLoader();
        loader.load(files);
        return loader;
    }

    @Override
    public final List<BatchRegistrationResult> registerExperiments(
            final ExperimentType experimentType, final String sessionKey, final boolean async, final String userEmail)
    {
        final HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncExperimentTask = null;
        try
        {
            final String sessionToken = getSessionToken();
            uploadedFiles = getUploadedFiles(sessionKey, session);
            ExperimentLoader loader = getExperimentsFromFiles(uploadedFiles);

            // Update the identifiers using the default project and space when possible
            applyDefaultSpaceProjectToExperiments(loader.getNewBasicExperiments(), sessionToken);
            NewExperimentsWithType newExperiments = new NewExperimentsWithType(experimentType.getCode(), loader.getNewBasicExperiments());
            if (async)
            {
                asyncExperimentTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Experiment Batch Registration";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            // Some stuff is repeated on the async executor, this is expected
                            ExperimentLoader asyncLoader = getExperimentsFromFiles(this.getFilesForTask());
                            applyDefaultSpaceProjectToExperiments(asyncLoader.getNewBasicExperiments(), sessionToken);
                            NewExperimentsWithType newExperiments =
                                    new NewExperimentsWithType(experimentType.getCode(), asyncLoader.getNewBasicExperiments());
                            // Execute task
                            genericServer.registerExperiments(sessionToken, newExperiments);
                        }
                    };

                String fileName = loader.getResults().get(0).getFileName();
                return AsyncBatchRegistrationResult.singletonList(fileName);
            } else
            {
                genericServer.registerExperiments(sessionToken, newExperiments);
                return loader.getResults();
            }
        } finally
        {
            if (async && (asyncExperimentTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncExperimentTask);
            } else
            {
                cleanUploadedFiles(sessionKey, session, uploadedFiles);
            }
        }
    }

    private String getUserDefaultProject(String sessionToken)
    {
        String projectIdentifier = null;

        PersonPE person = this.getServer().getAuthSession(sessionToken).tryGetPerson();

        // Get Project from user settings
        if (person != null && person.getDisplaySettings() != null)
        {
            projectIdentifier = person.getDisplaySettings().getDefaultProject();
        }

        if (projectIdentifier != null)
        {
            return projectIdentifier;
        } else
        {
            return null;
        }
    }

    private String getUserDefaultSpace(String sessionToken)
    {
        String spaceIdentifier = null;

        PersonPE person = this.getServer().getAuthSession(sessionToken).tryGetPerson();

        // Get Space from user settings
        if (person != null && person.getHomeSpace() != null)
        {
            spaceIdentifier = person.getHomeSpace().getCode();
        }

        if (spaceIdentifier != null)
        {
            return spaceIdentifier;
        } else
        {
            return null;
        }
    }

    private int count(char character, String string)
    {
        int count = 0;
        int fromIndex = -1;
        while (string.indexOf(character, fromIndex + 1) > fromIndex)
        {
            fromIndex = string.indexOf(character, fromIndex + 1);
            count++;
        }
        return count;
    }

    private void applyDefaultSpaceProjectToExperiments(List<? extends NewBasicExperiment> experiments, String sessionToken)
    {
        String defaultProjectIdentifier = getUserDefaultProject(sessionToken); // If default project is present
        String defaultSpaceIdentifier = getUserDefaultSpace(sessionToken); // If default space is present

        for (NewBasicExperiment experiment : experiments)
        {
            String newExperimentIdentifier = experiment.getIdentifier();
            int numberOfSlashes = count('/', newExperimentIdentifier);

            switch (numberOfSlashes)
            {
                case 0:
                    if (defaultProjectIdentifier != null)
                    {
                        experiment.setIdentifier(defaultProjectIdentifier + "/" + newExperimentIdentifier);
                    }
                    break;
                case 1:
                    if (defaultSpaceIdentifier != null && (newExperimentIdentifier.startsWith("/") == false)
                            && (newExperimentIdentifier.endsWith("/") == false))
                    {
                        experiment.setIdentifier("/" + defaultSpaceIdentifier + "/" + newExperimentIdentifier);
                    }
                    break;
                default:
            }
        }

        for (NewBasicExperiment experiment : experiments)
        {
            String newExperimentIdentifier = experiment.getIdentifier();
            int numberSlash = count('/', newExperimentIdentifier);
            if (numberSlash != 3)
            {
                throw new UserFailureException("The given identifier is not complete '" + newExperimentIdentifier
                        + "' and the required home space or default project is missing.");
            }
        }
    }

    private BatchMaterialsOperation parseMaterials(HttpSession session,
            UploadedFilesBean uploadedFiles, MaterialType materialType, String excelSheetName,
            boolean updateExisting)
    {
        Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
        for (IUncheckedMultipartFile f : uploadedFiles.iterable())
        {
            files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
        }

        BatchMaterialsOperation batchMaterialsOperation =
                MaterialUploadSectionsParser.prepareMaterials(materialType, files, excelSheetName);
        setUpdatePossibility(batchMaterialsOperation.getMaterials(), updateExisting);
        return batchMaterialsOperation;
    }

    @Override
    public SampleUpdateResult updateSample(final SampleUpdates updates)
    {
        final String sessionToken = getSessionToken();
        final SampleUpdateResult result = new SampleUpdateResult();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    SampleIdentifier sampleOwner = null;
                    ExperimentIdentifier convExperimentIdentifierOrNull = getExperimentIdentifier(updates);
                    ProjectIdentifier projectIdentifier = getProjectIdentifier(updates);
                    if (StringUtils.isBlank(updates.getSampleIdentifier()) == false)
                    {
                        sampleOwner =
                                new SampleIdentifierFactory(updates.getSampleIdentifier())
                                        .createIdentifier();
                    }

                    SampleUpdatesDTO updatesDTO =
                            new SampleUpdatesDTO(updates.getSampleIdOrNull(),
                                    updates.getProperties(), convExperimentIdentifierOrNull,
                                    projectIdentifier, attachments, updates.getVersion(), sampleOwner,
                                    updates.getContainerIdentifierOrNull(),
                                    updates.getModifiedParentCodesOrNull());
                    updatesDTO.setMetaprojectsOrNull(updates.getMetaprojectsOrNull());

                    SampleUpdateResult updateResult =
                            genericServer.updateSample(sessionToken, updatesDTO);

                    result.copyFrom(updateResult);
                }

            }.process(updates.getSessionKey(), getHttpSession(), updates.getAttachments());
        return result;
    }

    private ExperimentIdentifier getExperimentIdentifier(final SampleUpdates updates)
    {
        ExperimentIdentifier convExperimentIdentifierOrNull = null;
        if (updates.getExperimentIdentifierOrNull() != null)
        {
            convExperimentIdentifierOrNull =
                    new ExperimentIdentifierFactory(updates
                            .getExperimentIdentifierOrNull().getIdentifier())
                                    .createIdentifier();
        }
        return convExperimentIdentifierOrNull;
    }

    private ProjectIdentifier getProjectIdentifier(SampleUpdates updates)
    {
        String identifier = updates.getProjectIdentifierOrNull();
        if (identifier == null)
        {
            return null;
        }
        return new ProjectIdentifierFactory(identifier).createIdentifier();
    }

    @Override
    public Date updateMaterial(TechId materialId, List<IEntityProperty> properties,
            String[] metaprojects, Date version)
    {
        try
        {
            final String sessionToken = getSessionToken();
            return genericServer.updateMaterial(sessionToken, materialId, properties, metaprojects,
                    version);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
    public ExperimentUpdateResult updateExperiment(final ExperimentUpdates updates)
    {
        final String sessionToken = getSessionToken();
        final ExperimentUpdateResult result = new ExperimentUpdateResult();
        boolean autoGenerateCodes = updates.isGenerateCodes();
        BatchSamplesOperation info = null;
        if (updates.isRegisterSamples())
        {
            final ProjectIdentifier newProject =
                    new ProjectIdentifierFactory(updates.getProjectIdentifier()).createIdentifier();
            info = parseSamples(
                    updates.getSampleType(),
                    updates.getSamplesSessionKey(),
                    new SpaceIdentifier(newProject.getSpaceCode()).toString(), autoGenerateCodes, false,
                    null, BatchOperationKind.REGISTRATION);
            updates.setNewSamples(info.getSamples());
            updates.setSampleCodes(info.getCodes());
        }
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    ExperimentUpdatesDTO updatesDTO =
                            createExperimentUpdatesDTO(updates, attachments);
                    ExperimentUpdateResult updateResult =
                            genericServer.updateExperiment(sessionToken, updatesDTO);
                    result.copyFrom(updateResult);
                }
            }.process(updates.getAttachmentSessionKey(), getHttpSession(), updates.getAttachments());
        if (autoGenerateCodes && shouldContinuousSampleCodesCreated() && info != null)
        {
            updateTemporaryCodes(sessionToken, info);
        }
        return result;
    }

    private static ExperimentUpdatesDTO createExperimentUpdatesDTO(ExperimentUpdates updates,
            Collection<NewAttachment> attachments)
    {
        ExperimentUpdatesDTO updatesDTO = new ExperimentUpdatesDTO();

        updatesDTO.setExperimentId(updates.getExperimentId());

        final ProjectIdentifier project =
                new ProjectIdentifierFactory(updates.getProjectIdentifier()).createIdentifier();
        updatesDTO.setProjectIdentifier(project);
        updatesDTO.setAttachments(attachments);
        updatesDTO.setProperties(updates.getProperties());
        updatesDTO.setSampleCodes(updates.getSampleCodes());
        updatesDTO.setVersion(updates.getVersion());
        updatesDTO.setRegisterSamples(updates.isRegisterSamples());
        updatesDTO.setNewSamples(updates.getNewSamples());
        updatesDTO.setSampleType(updates.getSampleType());
        updatesDTO.setMetaprojectsOrNull(updates.getMetaprojectsOrNull());
        return updatesDTO;
    }

    @Override
    public DataSetUpdateResult updateDataSet(final DataSetUpdates updates)
    {
        final String sessionToken = getSessionToken();
        return genericServer.updateDataSet(sessionToken, createDataSetUpdatesDTO(updates));
    }

    private static DataSetUpdatesDTO createDataSetUpdatesDTO(DataSetUpdates updates)
    {
        DataSetUpdatesDTO updatesDTO = new DataSetUpdatesDTO();

        updatesDTO.setDatasetId(updates.getDatasetId());
        updatesDTO.setProperties(updates.getProperties());
        updatesDTO.setVersion(updates.getVersion());
        updatesDTO.setModifiedParentDatasetCodesOrNull(updates
                .getModifiedParentDatasetCodesOrNull());
        updatesDTO.setModifiedContainedDatasetCodesOrNull(updates
                .getModifiedContainedDatasetCodesOrNull());
        String sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        updatesDTO.setSampleIdentifierOrNull(sampleIdentifierOrNull == null ? null
                : SampleIdentifierFactory.parse(sampleIdentifierOrNull));
        String experimentIdentifierOrNull = updates.getExperimentIdentifierOrNull();
        updatesDTO.setExperimentIdentifierOrNull(experimentIdentifierOrNull == null ? null
                : new ExperimentIdentifierFactory(experimentIdentifierOrNull).createIdentifier());
        updatesDTO.setFileFormatTypeCode(updates.getFileFormatTypeCode());
        updatesDTO.setExternalCode(updates.getExternalCode());
        updatesDTO.setExternalDataManagementSystemCode(updates
                .getExternalDataManagementSystemCode());
        updatesDTO.setMetaprojectsOrNull(updates.getMetaprojectsOrNull());
        return updatesDTO;
    }

    @Override
    public List<BatchRegistrationResult> updateDataSets(
            final DataSetType dataSetType,
            final String sessionKey,
            final boolean async,
            final String userEmail)
    {
        final HttpSession session = getHttpSession();
        final String sessionToken = getSessionToken();
        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncDatasetsTask = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);

            Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
            for (IUncheckedMultipartFile f : uploadedFiles.iterable())
            {
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
            }
            DataSetLoader loader = new DataSetLoader();
            loader.load(files);

            NewDataSetsWithTypes newDataSetsWithTypes = new NewDataSetsWithTypes(dataSetType, loader.getNewDataSets());

            if (async)
            {
                asyncDatasetsTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Data Set Batch Update";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            // Some stuff is repeated on the async executor, this is expected
                            Collection<NamedInputStream> asyncFiles = new ArrayList<NamedInputStream>(this.getFilesForTask().size());
                            for (IUncheckedMultipartFile f : this.getFilesForTask().iterable())
                            {
                                asyncFiles.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
                            }
                            DataSetLoader asyncLoader = new DataSetLoader();
                            asyncLoader.load(asyncFiles);
                            NewDataSetsWithTypes asyncNewDataSetsWithTypes = new NewDataSetsWithTypes(dataSetType, asyncLoader.getNewDataSets());
                            // Execute task
                            genericServer.updateDataSets(sessionToken, asyncNewDataSetsWithTypes);
                        }
                    };
                String fileName = loader.getResults().get(0).getFileName();
                return AsyncBatchRegistrationResult.singletonList(fileName);
            } else
            {
                genericServer.updateDataSets(getSessionToken(), newDataSetsWithTypes);
                return loader.getResults();
            }
        } finally
        {
            if (async && (asyncDatasetsTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncDatasetsTask);
            } else
            {
                cleanUploadedFiles(sessionKey, session, uploadedFiles);
            }
        }
    }

    @Override
    public Map<String, Object> uploadedSamplesInfo(SampleType sampleType, String sessionKey)
    {
        HttpSession httpSession = getHttpSession();
        UploadedFilesBean uploadedFiles = (UploadedFilesBean) httpSession.getAttribute(sessionKey);

        Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());

        for (IUncheckedMultipartFile f : uploadedFiles.iterable())
        {
            files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
        }

        Map<String, Object> info = new HashMap<String, Object>();
        try
        {
            boolean projectSamplesEnabled = genericServer.isProjectSamplesEnabled(getSessionToken());

            BatchSamplesOperation batchSamplesOperation = SampleUploadSectionsParser.prepareSamples(
                    projectSamplesEnabled,
                    sampleType,
                    null,
                    null,
                    files,
                    null,
                    null,
                    true,
                    null,
                    BatchOperationKind.REGISTRATION);
            info.put("identifiersPressent", Boolean.TRUE);
        } catch (Exception ex)
        {
            if (ex.getMessage() != null && ex.getMessage().contains("Mandatory column 'identifier' is missing."))
            {
                info.put("identifiersPressent", Boolean.FALSE);
            }
        }

        return info;
    }
}
