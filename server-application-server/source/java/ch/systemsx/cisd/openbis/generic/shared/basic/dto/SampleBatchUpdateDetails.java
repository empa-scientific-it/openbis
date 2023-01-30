/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Set;

/**
 * Holds information about which sample attributes should be updated.
 * 
 * @author Piotr Buczek
 */
public class SampleBatchUpdateDetails implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private boolean experimentUpdateRequested;

    private boolean parentsUpdateRequested;

    private boolean containerUpdateRequested;

    private Set<String> propertiesToUpdate; // codes of properties to update

    public SampleBatchUpdateDetails()
    {
    }

    public SampleBatchUpdateDetails(boolean updateExperiment, boolean updateParents,
            boolean updateContainer, Set<String> propertiesToUpdate)
    {
        this.experimentUpdateRequested = updateExperiment;
        this.parentsUpdateRequested = updateParents;
        this.containerUpdateRequested = updateContainer;
        this.propertiesToUpdate = propertiesToUpdate;
    }

    public boolean isExperimentUpdateRequested()
    {
        return experimentUpdateRequested;
    }

    public boolean isParentsUpdateRequested()
    {
        return parentsUpdateRequested;
    }

    public boolean isContainerUpdateRequested()
    {
        return containerUpdateRequested;
    }

    public Set<String> getPropertiesToUpdate()
    {
        return propertiesToUpdate;
    }

    public void setExperimentUpdateRequested(boolean experimentUpdateRequested)
    {
        this.experimentUpdateRequested = experimentUpdateRequested;
    }

    public void setParentsUpdateRequested(boolean parentsUpdateRequested)
    {
        this.parentsUpdateRequested = parentsUpdateRequested;
    }

    public void setContainerUpdateRequested(boolean containerUpdateRequested)
    {
        this.containerUpdateRequested = containerUpdateRequested;
    }

    public void setPropertiesToUpdate(Set<String> propertiesToUpdate)
    {
        this.propertiesToUpdate = propertiesToUpdate;
    }

}
