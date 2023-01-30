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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A generic sample <i>Business Object</i>.
 * 
 * @author Christian Ribeaud
 */
public interface ISampleBO extends IEntityBusinessObject
{

    /**
     * Returns a sample found by the given id or null if it does not exist. Does not change the state of this object, especially the result of
     * {@link #getSample()}.
     */
    SamplePE tryFindBySampleId(final ISampleId sampleId);

    /**
     * Loads a sample given by its identifier. Use {@link #loadDataByTechId(TechId)} instead if possible.
     * 
     * @throws UserFailureException if no sample found.
     */
    void loadBySampleIdentifier(final SampleIdentifier identifier) throws UserFailureException;

    /**
     * Loads a sample given by its permanent identifier.
     * 
     * @throws UserFailureException if no sample found.
     */
    void loadBySamplePermId(final String permId) throws UserFailureException;

    /**
     * Tries to load the sample with specified identifier. Use {@link #loadDataByTechId(TechId)} instead if possible.
     */
    void tryToLoadBySampleIdentifier(SampleIdentifier identifier);

    /**
     * Tries to load the samlpe with specified permId.
     */
    void tryToLoadBySamplePermId(String permId);

    /**
     * Enrich with property types.
     */
    public void enrichWithPropertyTypes();

    /**
     * Enrich with properties.
     */
    public void enrichWithProperties();

    /**
     * Returns the loaded or defined sample or <code>null</code>.
     */
    SamplePE tryToGetSample();

    /** Returns the sample which has been loaded. */
    SamplePE getSample() throws IllegalStateException;

    /**
     * Defines a new sample. After invocation of this method {@link ISampleBO#save()} should be invoked to store the new sample in the Data Access
     * Layer.
     * 
     * @throws UserFailureException if specified sample type code is not a valid one.
     */
    void define(final NewSample newSample) throws UserFailureException;

    /**
     * Writes changed are added data to the Data Access Layers.
     */
    @Override
    public void save() throws UserFailureException;

    /**
     * Connects sample with specified experiment.
     */
    void setExperiment(ExperimentPE experiment);

    /**
     * Changes given sample.
     */
    void update(SampleUpdatesDTO updates);

    /**
     * Adds the specified experiment attachment to the sample.
     */
    void addAttachment(AttachmentPE attachment);

    /**
     * Returns attachment (with content) given defined by filename and version (or latest one if version is <code>null</code>). Returns null if the
     * attachment does not exist.
     */
    public AttachmentPE tryGetSampleFileAttachment(String fileName, Integer versionOrNull);

    /**
     * Returns attachment (with content) given defined by filename and version (or latest one if version is <code>null</code>). Throws
     * {@link UserFailureException} if the attachment does not exist.
     */
    public AttachmentPE getSampleFileAttachment(String fileName, Integer versionOrNull);

    /** Adds attachments */
    void enrichWithAttachments();

    /**
     * Changes the value of a managed property.
     */
    void updateManagedProperty(IManagedProperty managedProperty);

}
