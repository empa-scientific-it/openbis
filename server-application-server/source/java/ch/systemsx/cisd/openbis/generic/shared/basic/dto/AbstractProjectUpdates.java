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

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Description of the updates which should be performed on the project.
 * 
 * @author Tomasz Pylak
 */
public class AbstractProjectUpdates implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private int version;

    /**
     * If set, id takes precedence over permId.
     */
    private TechId id;

    /**
     * If set, id takes precedence over identifier.
     */
    private String permId;

    private String identifier;

    // ----- the data which should be changed:

    private String description;

    // Code of the data space to which project should be moved. If null nothing happens.
    private String spaceCodeOrNull;

    public String getSpaceCode()
    {
        return spaceCodeOrNull;
    }

    public void setSpaceCode(String spaceCode)
    {
        this.spaceCodeOrNull = spaceCode;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public TechId getTechId()
    {
        return id;
    }

    public void setTechId(TechId id)
    {
        this.id = id;
    }

    public String getPermId()
    {
        return permId;
    }

    public void setPermId(String permId)
    {
        this.permId = permId;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }
}
