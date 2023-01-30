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

/**
 * Contains a list of new entities and their type.
 * 
 * @author Pawel Glyzewski
 */
public abstract class NewEntitiesWithTypes<T extends EntityType, E extends Serializable> implements
        Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private T entityType;

    private List<E> newEntities;

    private boolean allowUpdateIfExist = false;

    public boolean isAllowUpdateIfExist()
    {
        return allowUpdateIfExist;
    }

    public void setAllowUpdateIfExist(boolean allowUpdateIfExist)
    {
        this.allowUpdateIfExist = allowUpdateIfExist;
    }

    public NewEntitiesWithTypes()
    {
    }

    public NewEntitiesWithTypes(T entityType, List<E> newEntities)
    {
        setEntityType(entityType);
        setNewEntities(newEntities);
    }

    public T getEntityType()
    {
        return entityType;
    }

    public void setEntityType(T entityType)
    {
        this.entityType = entityType;
    }

    public List<E> getNewEntities()
    {
        return newEntities;
    }

    public void setNewEntities(List<E> newEntities)
    {
        this.newEntities = newEntities;
    }
}
