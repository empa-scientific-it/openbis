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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * A generic experiment <i>Business Object</i>.
 * 
 * @author Izabela Adamczyk
 */
public interface IExperimentBO extends IEntityBusinessObject
{

    /** Loads a experiment given by its identifier. */
    void loadByExperimentIdentifier(final ExperimentIdentifier identifier);

    /**
     * Returns an experiment found at the given identifier or null if it does not exist. Does not change the state of this object, especially the
     * result of {@link #getExperiment()}.
     */
    ExperimentPE tryFindByExperimentIdentifier(final ExperimentIdentifier identifier);

    /**
     * Returns an experiment found by the given id or null if it does not exist. Does not change the state of this object, especially the result of
     * {@link #getExperiment()}.
     */
    ExperimentPE tryFindByExperimentId(final IExperimentId experimentId);

    /** Returns the sample which has been loaded. */
    ExperimentPE getExperiment();

    /** Adds properties */
    public void enrichWithProperties();

    /** Adds attachments */
    public void enrichWithAttachments();

    /**
     * Returns attachment (with content) given defined by filename and version (or latest one if version is <code>null</code>. Returns null if the
     * attachment does not exist.
     */
    public AttachmentPE tryGetExperimentFileAttachment(String filename, Integer versionOrNull);

    /**
     * Returns attachment (with content) given defined by filename and version (or latest one if version is <code>null</code>. Throws
     * {@link UserFailureException} if the attachment does not exist.
     */
    public AttachmentPE getExperimentFileAttachment(String filename, Integer versionOrNull);

    /**
     * Defines a new experiment. After invocation of this method {@link IExperimentBO#save()} should be invoked to store the new experiment in the
     * Data Access Layer.
     */
    public void define(NewExperiment experiment);

    /**
     * Adds the specified experiment attachment to the experiment.
     */
    public void addAttachment(AttachmentPE attachment);

    /**
     * Changes given experiment.
     */
    public void update(ExperimentUpdatesDTO updates);

    /**
     * Deletes experiment for specified reason.
     * 
     * @param experimentIds experiment technical identifiers
     * @throws UserFailureException if one of the experiments can not be deleted.
     */
    void deleteByTechIds(List<TechId> experimentIds, String reason);

    /**
     * Changes the value of a managed property.
     */
    void updateManagedProperty(IManagedProperty managedProperty);
}
