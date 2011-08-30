/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Information about deletion.
 * 
 * @author Christian Ribeaud
 */
public final class Deletion extends AbstractRegistrationHolder implements IIdHolder
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    /** Reason of deletion. */
    private String reasonOrNull;
    
    private List<IEntityInformationHolderWithProperties> deletedEntities = new ArrayList<IEntityInformationHolderWithProperties>();

    public final String getReason()
    {
        return reasonOrNull;
    }

    public final void setReason(final String reasonOrNull)
    {
        this.reasonOrNull = reasonOrNull;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
    
    public void addDeletedEntity(IEntityInformationHolderWithProperties entity)
    {
        deletedEntities.add(entity);
    }

    public List<IEntityInformationHolderWithProperties> getDeletedEntities()
    {
        return deletedEntities;
    }

}
