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
import java.util.Collections;
import java.util.Set;

/**
 * Holds information about which data set attributes should be updated.
 * 
 * @author pkupczyk
 */
public class DataSetBatchUpdateDetails implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private boolean experimentUpdateRequested;

    private boolean sampleUpdateRequested;

    private boolean parentsUpdateRequested;

    private boolean containerUpdateRequested;

    private boolean fileFormatUpdateRequested;

    private Set<String> propertiesToUpdate = Collections.emptySet(); // codes of properties to
                                                                     // update

    public DataSetBatchUpdateDetails()
    {
    }

    public boolean isExperimentUpdateRequested()
    {
        return experimentUpdateRequested;
    }

    public void setExperimentUpdateRequested(boolean experimentUpdateRequested)
    {
        this.experimentUpdateRequested = experimentUpdateRequested;
    }

    public boolean isSampleUpdateRequested()
    {
        return sampleUpdateRequested;
    }

    public void setSampleUpdateRequested(boolean sampleUpdateRequested)
    {
        this.sampleUpdateRequested = sampleUpdateRequested;
    }

    public boolean isParentsUpdateRequested()
    {
        return parentsUpdateRequested;
    }

    public void setParentsUpdateRequested(boolean parentsUpdateRequested)
    {
        this.parentsUpdateRequested = parentsUpdateRequested;
    }

    public boolean isContainerUpdateRequested()
    {
        return containerUpdateRequested;
    }

    public void setContainerUpdateRequested(boolean containerUpdateRequested)
    {
        this.containerUpdateRequested = containerUpdateRequested;
    }

    public boolean isFileFormatUpdateRequested()
    {
        return fileFormatUpdateRequested;
    }

    public void setFileFormatUpdateRequested(boolean fileFormatUpdateRequested)
    {
        this.fileFormatUpdateRequested = fileFormatUpdateRequested;
    }

    public Set<String> getPropertiesToUpdate()
    {
        return propertiesToUpdate;
    }

    public void setPropertiesToUpdate(Set<String> propertiesToUpdate)
    {
        this.propertiesToUpdate = propertiesToUpdate;
    }

}
