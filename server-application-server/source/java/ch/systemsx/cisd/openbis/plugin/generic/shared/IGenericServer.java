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

package ch.systemsx.cisd.openbis.plugin.generic.shared;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
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

/**
 * Definition of the client-server interface.
 * <p>
 * Provides backend support for the "default" rendering of detail views (e.g. for samples, materials, datasets) of the openBIS presentation layer.
 * Plugins have the choice to either use this "generic" functionality, or to implement/extend it on their own. {@link IGenericServer} can be thought
 * of as an optional convenience/utility service that goes together with the central entity-type unspecific <code>ICommonServer</code>.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericServer extends IServer
{
    /**
     * For given {@link TechId} returns the {@link Sample} and its derived (child) samples.
     * 
     * @return never <code>null</code>.
     * @throws UserFailureException if given <var>sessionToken</var> is invalid or whether sample uniquely identified by given <var>sampleId</var>
     *             does not exist.
     */
    @Transactional(readOnly = true)
    public SampleParentWithDerived getSampleInfo(final String sessionToken, final TechId sampleId)
            throws UserFailureException;

    /**
     * Registers a new sample.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public Sample registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments);

    /**
     * For given {@link TechId} returns the corresponding {@link AbstractExternalData}.
     */
    @Transactional(readOnly = true)
    public AbstractExternalData getDataSetInfo(String sessionToken, TechId datasetId);

    /**
     * Returns attachment described by given experiment identifier, filename and version.
     */
    @Transactional
    public AttachmentWithContent getExperimentFileAttachment(String sessionToken,
            TechId experimentId, String filename, Integer versionOrNull)
            throws UserFailureException;

    /**
     * Registers samples of different types in batches.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerSamples(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException;

    /**
     * Registers or updates samples of different types in batches.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerOrUpdateSamples(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException;

    /**
     * Registers or updates samples and materials of different types in batches.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value =
    { ObjectKind.SAMPLE, ObjectKind.MATERIAL })
    public void registerOrUpdateSamplesAndMaterials(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType,
            List<NewMaterialsWithTypes> newMaterialsWithType) throws UserFailureException;

    /**
     * Updates samples of different types in batches.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSamples(final String sessionToken,
            final List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException;

    /**
     * Registers experiment. At the same time samples may be registered or updated.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value =
    { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE })
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public Experiment registerExperiment(String sessionToken, final NewExperiment experiment,
            final Collection<NewAttachment> attachments) throws UserFailureException;

    /**
     * Registers experiments in batch.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public void registerExperiments(String sessionToken, final NewExperimentsWithType experiments)
            throws UserFailureException;

    /**
     * Update experiments in batch.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateExperiments(String sessionToken, final UpdatedExperimentsWithType experiments)
            throws UserFailureException;

    /**
     * Registers materials in batch.
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void registerMaterials(String sessionToken, List<NewMaterialsWithTypes> newMaterials)
            throws UserFailureException;

    /**
     * Updates materials in batch.
     * 
     * @return number of actually updated materials
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public int updateMaterials(String sessionToken, List<NewMaterialsWithTypes> newMaterials,
            boolean ignoreUnregisteredMaterials) throws UserFailureException;

    /**
     * Registers new materials or if they exist updates in batch their properties (properties which are not mentioned stay unchanged).
     */
    @Transactional
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void registerOrUpdateMaterials(String sessionToken, List<NewMaterialsWithTypes> materials)
            throws UserFailureException;

    /**
     * Returns attachment described by given sample identifier, filename and version.
     */
    @Transactional
    public AttachmentWithContent getSampleFileAttachment(String sessionToken, TechId sampleId,
            String fileName, Integer versionOrNull);

    /**
     * Returns attachment described by given project identifier, filename and version.
     */
    @Transactional
    public AttachmentWithContent getProjectFileAttachment(String sessionToken, TechId projectId,
            String fileName, Integer versionOrNull);

    /**
     * Returns a list of unique codes.
     */
    @Transactional
    public List<String> generateCodes(String sessionToken, String prefix, EntityKind entityKind,
            int number);

    @Transactional
    public List<String> generateTemporaryCodes(String sessionToken, String prefix,
            EntityKind entityKind, int number);

    /**
     * Saves changed experiment.
     */
    @Transactional
    @DatabaseUpdateModification(value =
    { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE })
    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates);

    /**
     * Saves changed material.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, String[] metaprojects, Date version);

    /**
     * Saves changed sample.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates);

    /**
     * Saves changed data set.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates);

    /**
     * Updates data sets of different types in batches.
     */
    @Transactional
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSets(final String sessionToken, final NewDataSetsWithTypes dataSets)
            throws UserFailureException;

}
