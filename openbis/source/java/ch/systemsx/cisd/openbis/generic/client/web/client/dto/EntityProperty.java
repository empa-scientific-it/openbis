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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An <i>abstract</i> entity property.
 * 
 * @author Christian Ribeaud
 */
public abstract class EntityProperty<T extends EntityType, P extends EntityTypePropertyType<T>>
        implements IsSerializable
{
    private String value;

    private P entityTypePropertyType;

    public final String getValue()
    {
        return value;
    }

    public final void setValue(final String value)
    {
        this.value = value;
    }

    public final P getEntityTypePropertyType()
    {
        return entityTypePropertyType;
    }

    public final void setEntityTypePropertyType(final P entityTypePropertyType)
    {
        this.entityTypePropertyType = entityTypePropertyType;
    }
}
