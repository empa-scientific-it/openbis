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
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.ISampleId;

/**
 * @author Jakub Straszewski
 */
public class BasicMetaprojectUpdates implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String description;

    private List<ISampleId> addedSamples;

    private List<ISampleId> removedSamples;

    private List<IExperimentId> addedExperiments;

    private List<IExperimentId> removedExperiments;

    private List<IDataSetId> addedDataSets;

    private List<IDataSetId> removedDataSets;

    private List<IMaterialId> addedMaterials;

    private List<IMaterialId> removedMaterials;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setAddedEntities(Collection<? extends IObjectId> addedEntities)
    {
        addedSamples = Collections.unmodifiableList(filterSamples(addedEntities));
        addedDataSets = Collections.unmodifiableList(filterDataSets(addedEntities));
        addedExperiments = Collections.unmodifiableList(filterExperiments(addedEntities));
        addedMaterials = Collections.unmodifiableList(filterMaterials(addedEntities));
    }

    public void setRemovedEntities(Collection<? extends IObjectId> removedEntities)
    {
        removedSamples = Collections.unmodifiableList(filterSamples(removedEntities));
        removedDataSets = Collections.unmodifiableList(filterDataSets(removedEntities));
        removedExperiments = Collections.unmodifiableList(filterExperiments(removedEntities));
        removedMaterials = Collections.unmodifiableList(filterMaterials(removedEntities));
    }

    public List<ISampleId> getAddedSamples()
    {
        return addedSamples;
    }

    public List<ISampleId> getRemovedSamples()
    {
        return removedSamples;
    }

    public List<IExperimentId> getAddedExperiments()
    {
        return addedExperiments;
    }

    public List<IExperimentId> getRemovedExperiments()
    {
        return removedExperiments;
    }

    public List<IDataSetId> getAddedDataSets()
    {
        return addedDataSets;
    }

    public List<IDataSetId> getRemovedDataSets()
    {
        return removedDataSets;
    }

    public List<IMaterialId> getAddedMaterials()
    {
        return addedMaterials;
    }

    public List<IMaterialId> getRemovedMaterials()
    {
        return removedMaterials;
    }

    public static LinkedList<ISampleId> filterSamples(Collection<? extends IObjectId> entities)
    {
        LinkedList<ISampleId> list = new LinkedList<ISampleId>();

        for (IObjectId id : entities)
        {
            if (id instanceof ISampleId)
            {
                list.add((ISampleId) id);
            }
        }
        return list;
    }

    public static LinkedList<IExperimentId> filterExperiments(
            Collection<? extends IObjectId> entities)
    {
        LinkedList<IExperimentId> list = new LinkedList<IExperimentId>();

        for (IObjectId id : entities)
        {
            if (id instanceof IExperimentId)
            {
                list.add((IExperimentId) id);
            }
        }
        return list;
    }

    public static LinkedList<IDataSetId> filterDataSets(Collection<? extends IObjectId> entities)
    {
        LinkedList<IDataSetId> list = new LinkedList<IDataSetId>();

        for (IObjectId id : entities)
        {
            if (id instanceof IDataSetId)
            {
                list.add((IDataSetId) id);
            }
        }
        return list;
    }

    public static LinkedList<IMaterialId> filterMaterials(Collection<? extends IObjectId> entities)
    {
        LinkedList<IMaterialId> list = new LinkedList<IMaterialId>();

        for (IObjectId id : entities)
        {
            if (id instanceof IMaterialId)
            {
                list.add((IMaterialId) id);
            }
        }
        return list;
    }

}
