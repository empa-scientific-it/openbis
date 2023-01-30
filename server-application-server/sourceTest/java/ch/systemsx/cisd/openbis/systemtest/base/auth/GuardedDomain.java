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

package ch.systemsx.cisd.openbis.systemtest.base.auth;

import java.util.UUID;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;

/**
 * @author anttil
 */
public abstract class GuardedDomain
{
    private final String id;

    private final RoleLevel type;

    public GuardedDomain(RoleLevel type)
    {
        this.id = UUID.randomUUID().toString();
        this.type = type;
    }

    public String getId()
    {
        return this.id;
    }

    public RoleLevel getType()
    {
        return type;
    }

    public abstract GuardedDomain getSuperDomain();

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof GuardedDomain))
        {
            return false;
        }

        GuardedDomain domain = (GuardedDomain) o;
        return domain.getId().equals(this.id) && domain.getType().equals(this.type);
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(this.id);
        builder.append(this.type);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return "GuardedDomain of type " + this.type;
    }
}
