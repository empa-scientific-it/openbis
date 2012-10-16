/*
 * Copyright 2012 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author Pawel Glyzewski
 */
public interface IMetaprojectBO extends IEntityBusinessObject
{
    MetaprojectPE tryFindByMetaprojectId(final IMetaprojectId metaprojectId);

    void loadByMetaprojectId(IMetaprojectId metaprojectId);

    void addExperiments(List<IExperimentId> experiments);

    void addSamples(List<ISampleId> samples);

    void addDataSets(List<IDataSetId> dataSets);

    void addMaterials(List<IMaterialId> materials);

    void removeExperiments(List<IExperimentId> experiments);

    void removeSamples(List<ISampleId> samples);

    void removeDataSets(List<IDataSetId> dataSets);

    void removeMaterials(List<IMaterialId> materials);

    void deleteByMetaprojectId(IMetaprojectId metaprojectId) throws UserFailureException;

    MetaprojectPE getMetaproject();
}
