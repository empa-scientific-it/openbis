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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.ISampleId;

/**
 * A metaproject to register.
 * 
 * @author Jakub Straszewski
 */
public class NewMetaproject implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String name;

    private String description;

    private String ownerId;

    private List<ISampleId> samples;

    private List<IExperimentId> experiments;

    private List<IDataSetId> datasets;

    private List<IMaterialId> materials;

    public NewMetaproject(String name, String descriptionOrNull, String ownerId)
    {
        this.name = name;
        this.description = descriptionOrNull;
        this.ownerId = ownerId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    public List<ISampleId> getSamples()
    {
        return samples;
    }

    public List<IExperimentId> getExperiments()
    {
        return experiments;
    }

    public List<IDataSetId> getDatasets()
    {
        return datasets;
    }

    public List<IMaterialId> getMaterials()
    {
        return materials;
    }

    public void setEntities(Collection<IObjectId> entities)
    {
        samples = Collections.unmodifiableList(BasicMetaprojectUpdates.filterSamples(entities));
        experiments =
                Collections.unmodifiableList(BasicMetaprojectUpdates.filterExperiments(entities));
        datasets = Collections.unmodifiableList(BasicMetaprojectUpdates.filterDataSets(entities));
        materials = Collections.unmodifiableList(BasicMetaprojectUpdates.filterMaterials(entities));
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("name", getName());
        builder.append("description", getDescription());
        builder.append("ownerId", getOwnerId());
        return builder.toString();
    }
}
