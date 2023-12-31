/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author pkupczyk
 */
public class MetaprojectTreeEntityKindItemData extends MetaprojectTreeItemData
{
    private static final long serialVersionUID = 1L;

    private Long metaprojectId;

    private EntityKind entityKind;

    private int entityCount;

    // GWT
    @SuppressWarnings("unused")
    private MetaprojectTreeEntityKindItemData()
    {
    }

    public MetaprojectTreeEntityKindItemData(Long metaprojectId, EntityKind entityKind,
            int entityCount)
    {
        this.metaprojectId = metaprojectId;
        this.entityKind = entityKind;
        this.entityCount = entityCount;
    }

    public Long getMetaprojectId()
    {
        return metaprojectId;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public int getEntityCount()
    {
        return entityCount;
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

        MetaprojectTreeEntityKindItemData other = (MetaprojectTreeEntityKindItemData) obj;
        return getMetaprojectId().equals(other.getMetaprojectId())
                && getEntityKind().equals(other.getEntityKind());
    }

}