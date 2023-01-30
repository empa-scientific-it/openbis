/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedSamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DeletedEntityTranslator;

/**
 * Helper class for finding the root entities of entities belonging to the same deletion event. It is assumed that experiments are added before
 * samples which are added before data sets.
 * 
 * @author Franz-Josef Elmer
 * @author Kaloyan Enimanev
 */
final class RootEntitiesFinder
{
    private List<DeletedExperimentPE> experiments = new ArrayList<DeletedExperimentPE>();

    private Set<Long> experimentIDs = new HashSet<Long>();

    private List<DeletedSamplePE> samples = new ArrayList<DeletedSamplePE>();

    private Set<Long> sampleIDs = new HashSet<Long>();

    private List<DeletedDataPE> dataSets = new ArrayList<DeletedDataPE>();

    void addEntity(IDeletablePE entity)
    {
        if (entity instanceof DeletedExperimentPE)
        {
            addExperiment((DeletedExperimentPE) entity);
        }
        if (entity instanceof DeletedSamplePE)
        {
            addSample((DeletedSamplePE) entity);
        }
        if (entity instanceof DeletedDataPE)
        {
            addDataSet((DeletedDataPE) entity);
        }
    }

    private void addExperiment(DeletedExperimentPE experiment)
    {
        experiments.add(experiment);
        experimentIDs.add(experiment.getId());
    }

    private void addSample(DeletedSamplePE sample)
    {
        samples.add(sample);
        sampleIDs.add(sample.getId());
    }

    private void addDataSet(DeletedDataPE dataSet)
    {
        if (experimentIDs.contains(dataSet.getExperimentId()))
        {
            return;
        }
        if (sampleIDs.contains(dataSet.getSampleId()))
        {
            return;
        }
        dataSets.add(dataSet);
    }

    void addRootEntitiesTo(Deletion deletion)
    {
        Collections.sort(experiments, createIdentifierComparator());
        for (DeletedExperimentPE experiment : experiments)
        {
            deletion.addDeletedEntity(DeletedEntityTranslator.translate(experiment));
        }
        List<DeletedSamplePE> rootSamples = findRootSamples();
        Collections.sort(rootSamples, createIdentifierComparator());
        for (DeletedSamplePE sample : rootSamples)
        {
            deletion.addDeletedEntity(DeletedEntityTranslator.translate(sample));
        }
        Collections.sort(dataSets, createIdentifierComparator());
        for (DeletedDataPE dataSet : dataSets)
        {
            deletion.addDeletedEntity(DeletedEntityTranslator.translate(dataSet));
        }
    }

    Comparator<IIdentifierHolder> createIdentifierComparator()
    {
        return new Comparator<IIdentifierHolder>()
            {
                @Override
                public int compare(IIdentifierHolder d1, IIdentifierHolder d2)
                {
                    return d1.getIdentifier().compareTo(d2.getIdentifier());
                }
            };
    }

    private List<DeletedSamplePE> findRootSamples()
    {
        List<DeletedSamplePE> rootSamples = new ArrayList<DeletedSamplePE>();
        for (DeletedSamplePE sample : samples)
        {
            if (experimentIDs.contains(sample.getExperimentId()))
            {
                continue;
            }
            if (sampleIDs.contains(sample.getContainerId()))
            {
                continue;
            }
            List<Long> parents = sample.getParents();
            if (parents != null && atLeastOneDeleted(parents))
            {
                continue;
            }
            rootSamples.add(sample);
        }
        return rootSamples;
    }

    private boolean atLeastOneDeleted(List<Long> parents)
    {
        return false == Collections.disjoint(sampleIDs, parents);
    }

}