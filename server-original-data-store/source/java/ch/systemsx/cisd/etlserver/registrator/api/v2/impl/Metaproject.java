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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.etlserver.registrator.api.v2.IMetaproject;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IMetaprojectContent;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;

/**
 * @author Jakub Straszewski
 */
public class Metaproject extends MetaprojectImmutable implements IMetaproject
{

    private Set<IObjectId> addedEntities = new HashSet<IObjectId>();

    private Set<IObjectId> removedEntities = new HashSet<IObjectId>();

    public static Metaproject createMetaproject(String name, String description, String ownerId)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject metaproject =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject();
        metaproject.setName(name);
        metaproject.setDescription(description);
        metaproject.setOwnerId(ownerId);
        return new Metaproject(metaproject, false);
    }

    private Metaproject(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject metaproject,
            boolean existingMetaproject)
    {
        super(metaproject, existingMetaproject);
    }

    public Metaproject(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject metaproject)
    {
        super(metaproject);
    }

    @Override
    public void setDescription(String description)
    {
        getMetaproject().setDescription(description);
    }

    Long getId()
    {
        return getMetaproject().getId();
    }

    @Override
    public void addEntity(IMetaprojectContent entity)
    {
        IObjectId id = entity.getEntityId();
        removedEntities.remove(id);
        addedEntities.add(id);
    }

    @Override
    public void removeEntity(IMetaprojectContent entity)
    {
        IObjectId id = entity.getEntityId();
        addedEntities.remove(id);
        removedEntities.add(id);
    }

    Set<IObjectId> getAddedEntities()
    {
        return addedEntities;
    }

    Set<IObjectId> getRemovedEntities()
    {
        return removedEntities;
    }

}
