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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * @author pkupczyk
 */
public class PersonRole implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private RoleWithHierarchy role;

    private DatabaseInstance databaseInstance;

    private Space space;

    // GWT
    @SuppressWarnings("unused")
    private PersonRole()
    {
    }

    public PersonRole(RoleWithHierarchy role, Space space)
    {
        if (role == null)
        {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (role.isInstanceLevel() && space != null)
        {
            throw new IllegalArgumentException("Space cannot be specified for instance role");
        }
        if (role.isSpaceLevel() && space == null)
        {
            throw new IllegalArgumentException("Space cannot be null for space role");
        }
        this.role = role;
        this.space = space;
    }

    public RoleWithHierarchy getRole()
    {
        return role;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public Space getSpace()
    {
        return space;
    }

}
