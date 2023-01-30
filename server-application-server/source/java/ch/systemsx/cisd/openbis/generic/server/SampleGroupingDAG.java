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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;

/**
 * @author Jakub Straszewski
 */
public class SampleGroupingDAG extends EntityGroupingDAG<NewSample>
{
    private SampleGroupingDAG(Collection<NewSample> samples)
    {
        super(samples);
    }

    /**
     * Return the new samples in the list of groups, where the earlier groups are independent to the latter ones.
     * 
     * @param samples The list of samples to create.
     */
    public static List<List<NewSample>> groupByDepencies(Collection<NewSample> samples)
    {
        if (samples.size() == 0)
        {
            return Collections.emptyList();
        }

        SampleGroupingDAG dag = new SampleGroupingDAG(samples);
        return dag.getDependencyGroups();
    }

    @Override
    public String getCode(NewSample sample)
    {
        return sample.getIdentifier();
    }

    @Override
    public Collection<String> getDependent(NewSample sample)
    {
        return null;
    }

    @Override
    public Collection<String> getDependencies(NewSample sample)
    {
        LinkedList<String> parentsAndContainers = null;

        if (sample.getContainerIdentifier() != null)
        {
            parentsAndContainers = new LinkedList<String>();
            parentsAndContainers.add(sample.getContainerIdentifier());
        }

        String[] parents = sample.getParentsOrNull();
        if (parents != null && parents.length > 0)
        {
            if (parentsAndContainers == null)
            {
                parentsAndContainers = new LinkedList<String>();
            }

            for (String parent : parents)
            {
                parentsAndContainers.add(parent);
            }
        }

        return parentsAndContainers;
    }

}
