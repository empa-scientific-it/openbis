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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.tree.model;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIsStub;

/**
 * @author pkupczyk
 */
public class MetaprojectTreeEntityItemData extends MetaprojectTreeItemData
{
    private static final long serialVersionUID = 1L;

    private Long metaprojectId;

    private IEntityInformationHolderWithIdentifier entity;

    // GWT
    @SuppressWarnings("unused")
    private MetaprojectTreeEntityItemData()
    {
    }

    public MetaprojectTreeEntityItemData(Long metaprojectId,
            IEntityInformationHolderWithIdentifier entity)
    {
        this.metaprojectId = metaprojectId;
        this.entity = entity;
    }

    public Long getMetaprojectId()
    {
        return metaprojectId;
    }

    public IEntityInformationHolderWithIdentifier getEntity()
    {
        return entity;
    }

    public String getEntityLabel()
    {
        if (isEntityStub())
        {
            return getEntity().getPermId();
        } else
        {
            return getEntity().getIdentifier();
        }
    }

    public boolean isEntityStub()
    {
        if (entity instanceof IIsStub)
        {
            return ((IIsStub) entity).isStub();
        } else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return getMetaprojectId().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        MetaprojectTreeEntityItemData other = (MetaprojectTreeEntityItemData) obj;
        return getMetaprojectId().equals(other.getMetaprojectId())
                && getEntity().getEntityKind().equals(other.getEntity().getEntityKind())
                && getEntity().getId().equals(other.getEntity().getId());
    }
}