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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServerInternal;

/**
 * Logger class for {@link GenericServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class GenericServerLogger extends AbstractServerLogger implements IGenericServerInternal
{
    /**
     * Creates an instance for the specified session manager invocation status and elapsed time. The session manager is used to retrieve user
     * information which will be a part of the log message.
     */
    GenericServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    //
    // IGenericServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    @Override
    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "get_sample_info", "ID(%s)", sampleId);
        return null;
    }

    @Override
    public Sample registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S) ATTACHMENTS(%S)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
        return null;
    }

    @Override
    public AbstractExternalData getDataSetInfo(final String sessionToken, final TechId datasetId)
    {
        logAccess(sessionToken, "get_data_set_info", "ID(%s)", datasetId);
        return null;
    }

    @Override
    public Experiment registerExperiment(String sessionToken, NewExperiment experiment,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "register_experiment",
                "EXPERIMENT_TYPE(%s) EXPERIMENT(%S) ATTACHMENTS(%S)",
                experiment.getExperimentTypeCode(), experiment.getIdentifier(), attachments.size());
        return null;
    }

    @Override
    public void registerMaterials(String sessionToken, List<NewMaterialsWithTypes> newMaterials)
    {
        logTracking(sessionToken, "register_materials", getMaterials(newMaterials));
    }

    @Override
    public int updateMaterials(String sessionToken, List<NewMaterialsWithTypes> newMaterials,
            boolean ignoreUnregisteredMaterials) throws UserFailureException
    {
        logTracking(sessionToken, "update_materials",
                "MATERIALS(%S) IGNORE_UNREGISTERED_MATERIALS(%S)", getMaterials(newMaterials),
                ignoreUnregisteredMaterials);
        return 0;
    }

    @Override
    public AttachmentWithContent getExperimentFileAttachment(final String sessionToken,
            final TechId experimentId, final String filename, final Integer versionOrNull)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_attachment", "EXPERIMENT_ID(%s) FILE(%s) VERSION(%s)",
                experimentId, filename, versionOrNull);
        return null;
    }

    @Override
    public AttachmentWithContent getProjectFileAttachment(String sessionToken, TechId projectId,
            String fileName, Integer versionOrNull)
    {
        logAccess(sessionToken, "get_attachment", "PROJECT_ID(%s) FILE(%s) VERSION(%s)", projectId,
                fileName, versionOrNull);
        return null;
    }

    @Override
    public AttachmentWithContent getSampleFileAttachment(String sessionToken, TechId sampleId,
            String fileName, Integer versionOrNull)
    {
        logAccess(sessionToken, "get_attachment", "SAMPLE_ID(%s) FILE(%s) VERSION(%s)", sampleId,
                fileName, versionOrNull);
        return null;
    }

    @Override
    public List<String> generateCodes(String sessionToken, String prefix, EntityKind entityKind,
            int number)
    {
        logAccess(sessionToken, "generate_codes", "PREFIX(%s) ENTITY_KIND(%s) NUMBER(%s)", prefix,
                entityKind, number);
        return null;
    }

    @Override
    public List<String> generateTemporaryCodes(String sessionToken, String prefix, EntityKind entityKind, int number)
    {
        logAccess(sessionToken, "generate_temporary_codes", "PREFIX(%s) ENTITY_KIND(%s) NUMBER(%s)", prefix,
                entityKind, number);
        return null;
    }

    @Override
    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_experiment",
                "EXPERIMENT(%s) ATTACHMENTS_ADDED(%s) NEW_PROJECT(%s) SAMPLES(%s)",
                updates.getExperimentId(), updates.getAttachments().size(),
                updates.getProjectIdentifier(), StringUtils.join(updates.getSampleCodes(), ","));
        return null;
    }

    @Override
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, String[] metaprojects, Date version)
    {
        logTracking(sessionToken, "edit_material", "MATERIAL(%s)", materialId);
        return null;
    }

    @Override
    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_sample",
                "SAMPLE(%s), CHANGE_TO_EXPERIMENT(%s) ATTACHMENTS(%s)",
                updates.getSampleIdOrNull(), updates.getExperimentIdentifierOrNull(), updates
                        .getAttachments().size());
        return null;
    }

    @Override
    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_data_set", "DATA_SET(%s) SAMPLE(%s) MODIFIED_PARENTS(%s)",
                updates.getDatasetId(), updates.getSampleIdentifierOrNull(),
                StringUtils.join(updates.getModifiedParentDatasetCodesOrNull(), ","));
        return null;
    }

    @Override
    public void registerSamples(String sessionToken, List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException
    {
        logTracking(sessionToken, "register_samples", "SAMPLES(%s)", getSamples(newSamplesWithType));
    }

    @Override
    public void updateSamples(String sessionToken, List<NewSamplesWithTypes> updatedSamplesWithType)
            throws UserFailureException
    {
        logTracking(sessionToken, "update_samples", "SAMPLES(%s)", getSamples(updatedSamplesWithType));
    }

    @Override
    public void registerOrUpdateMaterials(String sessionToken, List<NewMaterialsWithTypes> materials)
    {
        for (NewMaterialsWithTypes materialsWithType : materials)
        {
            logTracking(sessionToken, "registerOrUpdateMaterials",
                    "type(%s) numberOfMaterials(%s)", materialsWithType.getEntityType().getCode(),
                    materialsWithType.getNewEntities().size());
        }
    }

    @Override
    public void registerOrUpdateSamples(String sessionToken,
            List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        logTracking(sessionToken, "register_or_update_samples", getSamples(newSamplesWithType));
    }

    @Override
    public void updateDataSets(String sessionToken, NewDataSetsWithTypes dataSets)
            throws UserFailureException
    {
        logTracking(sessionToken, "update_data_sets", "TYPE(%s) DATA_SETS(%s)",
                dataSets.getDataSetType().getCode(), dataSets.getNewDataSets().size());
    }

    @Override
    public void registerExperiments(String sessionToken, NewExperimentsWithType experiments)
            throws UserFailureException
    {
        logTracking(sessionToken, "register_experiments", "TYPE(%s) EXPERIMENTS(%s)",
                experiments.getExperimentTypeCode(), experiments.getNewExperiments().size());
    }

    @Override
    public void updateExperiments(String sessionToken, UpdatedExperimentsWithType experiments)
            throws UserFailureException
    {
        logTracking(sessionToken, "update_experiments", "TYPE(%s) EXPERIMENTS(%s)", experiments
                .getExperimentType().getCode(), experiments.getUpdatedExperiments().size());
    }

    @Override
    public void registerOrUpdateSamplesAndMaterials(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType,
            final List<NewMaterialsWithTypes> newMaterialsWithType) throws UserFailureException
    {

        logTracking(sessionToken, "register_or_update_samples_and_materials",
                "SAMPLES(%s) MATERIALS(%s)", getSamples(newSamplesWithType),
                getMaterials(newMaterialsWithType));
    }

    private static String getSamples(final List<NewSamplesWithTypes> newSamplesWithType)
    {
        StringBuilder samples = new StringBuilder();
        for (NewSamplesWithTypes s : newSamplesWithType)
        {
            if (samples.length() > 0)
            {
                samples.append(",");
            }
            samples.append(s.getEntityType().getCode() + ":" + s.getNewEntities().size());
        }

        return samples.toString();
    }

    private static String getMaterials(final List<NewMaterialsWithTypes> newMaterialsWithType)
    {
        StringBuilder materials = new StringBuilder();
        for (NewMaterialsWithTypes s : newMaterialsWithType)
        {
            if (materials.length() > 0)
            {
                materials.append(",");
            }
            materials.append(s.getEntityType().getCode() + ":" + s.getNewEntities().size());
        }

        return materials.toString();
    }

    @Override
    public void updateTemporaryCodes(String sessionToken, Map<String, List<String>> sampleTypeCodeToTemporaryIdentifiers)
    {
        logTracking(sessionToken, "update_temporary_codes", "SAMPLETYPECODETOTEMPORARYIDENTIFIERS(%s)", sampleTypeCodeToTemporaryIdentifiers);
    }
}
