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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * Represents a DAG of registration dependencies between new data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class NewExternalDataDAG extends EntityGroupingDAG<NewExternalData>
{
    private NewExternalDataDAG(Collection<NewExternalData> dataSetRegistrations)
    {
        super(dataSetRegistrations);
    }

    /**
     * Return the new data sets in the list of groups, where the earlier groups are independent of the latter ones.
     * 
     * @param dataSetRegistration The list of samples to create.
     */
    public static List<List<NewExternalData>> groupByDepencies(
            Collection<NewExternalData> dataSetRegistration)
    {
        if (dataSetRegistration.size() == 0)
        {
            return Collections.emptyList();
        }

        NewExternalDataDAG dag = new NewExternalDataDAG(dataSetRegistration);
        return dag.getDependencyGroups();
    }

    @Override
    protected String getCode(NewExternalData entity)
    {
        return entity.getCode();
    }

    @Override
    protected Collection<String> getDependencies(NewExternalData dataSet)
    {
        ArrayList<String> dependents = new ArrayList<String>();

        // All the parents are dependents
        dependents.addAll(dataSet.getParentDataSetCodes());

        if (dataSet instanceof NewContainerDataSet)
        {
            // All contained data sets are dependents
            List<String> containedDataSetCodes =
                    ((NewContainerDataSet) dataSet).getContainedDataSetCodes();
            dependents.addAll(containedDataSetCodes);
        }

        return dependents;
    }

    @Override
    protected Collection<String> getDependent(NewExternalData item)
    {
        return null;
    }

}
