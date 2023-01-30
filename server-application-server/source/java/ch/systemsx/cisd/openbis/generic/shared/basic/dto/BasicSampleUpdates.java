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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author Izabela Adamczyk
 */
public class BasicSampleUpdates implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // if technical id is null old identifier must be provided by subclass
    private TechId sampleIdOrNull;

    private List<IEntityProperty> properties;

    private int version;

    private String containerIdentifierOrNull;

    // New set of parent sample codes which will replace the old ones. In this way some
    // parent samples can be unassigned and some assigned as a result. It will be assumed that
    // all the samples belong to the same group as the child sample.
    // If equals to null nothing should be changed.
    // If some previously assigned parent sample is missing on this list, it will be unassigned.
    private String[] modifiedParentCodesOrNull;

    private String[] metaprojectsOrNull;

    public String getParentIdentifierOrNull()
    {
        if (modifiedParentCodesOrNull == null || modifiedParentCodesOrNull.length == 0)
        {
            return null;
        } else
        {
            return modifiedParentCodesOrNull[0];
        }
    }

    public String getContainerIdentifierOrNull()
    {
        return containerIdentifierOrNull;
    }

    public void setContainerIdentifierOrNull(String containerIdentifierOrNull)
    {
        this.containerIdentifierOrNull = containerIdentifierOrNull;
    }

    public BasicSampleUpdates()
    {
    }

    public BasicSampleUpdates(TechId sampleId, List<IEntityProperty> properties, int version,
            String containerIdentifierOrNull, String[] modifiedParentCodesOrNull)
    {
        this.sampleIdOrNull = sampleId;
        this.properties = properties;
        this.version = version;
        this.containerIdentifierOrNull = containerIdentifierOrNull;
        this.modifiedParentCodesOrNull = modifiedParentCodesOrNull;
    }

    public TechId getSampleIdOrNull()
    {
        return sampleIdOrNull;
    }

    public void setSampleId(TechId sampleId)
    {
        this.sampleIdOrNull = sampleId;
    }

    public List<IEntityProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(List<IEntityProperty> properties)
    {
        this.properties = properties;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    // if null nothing should be changed
    public String[] getModifiedParentCodesOrNull()
    {
        return modifiedParentCodesOrNull;
    }

    public void setModifiedParentCodesOrNull(String[] modifiedParentCodesOrNull)
    {
        this.modifiedParentCodesOrNull = modifiedParentCodesOrNull;
    }

    public String[] getMetaprojectsOrNull()
    {
        return metaprojectsOrNull;
    }

    public void setMetaprojectsOrNull(String[] metaprojectsOrNull)
    {
        this.metaprojectsOrNull = metaprojectsOrNull;
    }

}
