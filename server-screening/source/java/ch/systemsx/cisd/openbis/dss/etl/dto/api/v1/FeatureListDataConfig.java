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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import java.util.Collection;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;

/**
 * The builder class for creation of feature lists.
 * 
 * @author Jakub Straszewski
 */
public class FeatureListDataConfig
{
    /**
     * The name of this feature grouping.
     */
    private String name;

    /**
     * The list of features for this grouping
     */
    private Collection<String> featureList;

    /**
     * The feature vector container data set to which this list should be assigned
     */
    private IDataSetUpdatable containerDataSet;

    public FeatureListDataConfig()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Collection<String> getFeatureList()
    {
        return featureList;
    }

    public void setFeatureList(Collection<String> featureList)
    {
        this.featureList = featureList;
    }

    public IDataSetUpdatable getContainerDataSet()
    {
        return containerDataSet;
    }

    public void setContainerDataSet(IDataSetUpdatable containerDataSet)
    {
        this.containerDataSet = containerDataSet;
    }

}
