/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.xoai;

import java.util.LinkedList;
import java.util.List;

import org.dspace.xoai.dataprovider.handlers.results.ListSetsResult;
import org.dspace.xoai.dataprovider.model.Set;
import org.dspace.xoai.dataprovider.repository.SetRepository;

/**
 * <p>
 * Simple implementation of {@link org.dspace.xoai.dataprovider.repository.SetRepository} that allows adding new sets via {@link #addSet(Set)}.
 * method. If no sets are defined then the repository {@link #supportSets()} method returns false.
 * </p>
 * 
 * @author pkupczyk
 */
public class SimpleSetRepository implements SetRepository
{

    private List<Set> sets = new LinkedList<Set>();

    public void addSet(Set set)
    {
        sets.add(set);
    }

    @Override
    public boolean supportSets()
    {
        return sets.isEmpty() == false;
    }

    @Override
    public ListSetsResult retrieveSets(int offset, int length)
    {
        return new ListSetsResult(offset + length < this.sets.size(), this.sets.subList(offset, Math.min(offset + length, sets.size())));
    }

    @Override
    public boolean exists(String setSpec)
    {
        for (Set s : this.sets)
            if (s.getSpec().equals(setSpec))
                return true;

        return false;
    }

}
