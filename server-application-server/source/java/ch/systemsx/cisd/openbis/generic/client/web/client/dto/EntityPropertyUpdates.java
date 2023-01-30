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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Updates of properties of an entity.
 * 
 * @author Piotr Buczek
 */
public class EntityPropertyUpdates implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private TechId entityId;

    private List<PropertyUpdates> modifiedProperties = new ArrayList<PropertyUpdates>();

    private String resultSetKey;

    public EntityPropertyUpdates()
    {
    }

    public EntityPropertyUpdates(String resultSetKey, EntityKind entityKind, TechId entityId)
    {
        this.resultSetKey = resultSetKey;
        this.entityKind = entityKind;
        this.entityId = entityId;
    }

    public String getResultSetKey()
    {
        return resultSetKey;
    }

    public void setResultSetKey(String resultSetKey)
    {
        this.resultSetKey = resultSetKey;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public TechId getEntityId()
    {
        return entityId;
    }

    public void setEntityId(TechId entityId)
    {
        this.entityId = entityId;
    }

    public List<PropertyUpdates> getModifiedProperties()
    {
        return modifiedProperties;
    }

    public void addModifiedProperty(PropertyUpdates propertyUpdates)
    {
        modifiedProperties.add(propertyUpdates);
    }

    public void addModifiedProperty(String propertyCode, String propertyValue)
    {
        if (modifiedProperties == null)
        {
            modifiedProperties = new ArrayList<PropertyUpdates>();
        }
        modifiedProperties.add(new PropertyUpdates(propertyCode, propertyValue));
    }

    public void setModifiedProperties(List<PropertyUpdates> modifiedProperties)
    {
        this.modifiedProperties = modifiedProperties;
    }

}
