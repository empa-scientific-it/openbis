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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * A basic {@link IEntityInformationHolder} implementation.
 * 
 * @author Piotr Buczek
 */
public class BasicEntityInformationHolder implements IEntityInformationHolderWithPermId,
        IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private BasicEntityType entityType;

    private String code;

    private Long id;

    private String permId;

    @SuppressWarnings("unused")
    private BasicEntityInformationHolder()
    {
        // needed for serialization purposes
    }

    public BasicEntityInformationHolder(EntityKind entityKind, BasicEntityType entityType,
            String code, Long id, String permId)
    {
        this.entityKind = entityKind;
        this.entityType = entityType;
        this.code = code;
        this.id = id;
        this.permId = permId;
    }

    @Override
    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    @Override
    public BasicEntityType getEntityType()
    {
        return entityType;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public String getPermId()
    {
        return permId;
    }
}
