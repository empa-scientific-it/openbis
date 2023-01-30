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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * @author Pawel Glyzewski
 */
@MappedSuperclass
public abstract class AbstractEntityHistoryPE extends AbstractEntityPropertyHistoryPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private RelationType relationType;

    private String entityPermId;

    @Column(name = ColumnNames.RELATION_TYPE_COLUMN)
    @Enumerated(EnumType.STRING)
    public RelationType getRelationType()
    {
        return relationType;
    }

    public void setRelationType(RelationType relationType)
    {
        this.relationType = relationType;
    }

    @Column(name = ColumnNames.ENTITY_PERM_ID_COLUMN)
    public String getEntityPermId()
    {
        return entityPermId;
    }

    public void setEntityPermId(String entityPermId)
    {
        this.entityPermId = entityPermId;
    }

    @Transient
    public abstract IRelatedEntity getRelatedEntity();

}
