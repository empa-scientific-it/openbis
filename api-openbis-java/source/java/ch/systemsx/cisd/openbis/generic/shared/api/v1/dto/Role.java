/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Immutable value object representing an authorization role. A role has a code and a flag which tells whether this role is for a certain space or for
 * all spaces.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("Role")
public final class Role implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;

    private boolean spaceLevel;

    /**
     * Creates a new instance from specified code and space level flag.
     * 
     * @throws IllegalArgumentException if specified code is <code>null</code> or an empty string.
     */
    public Role(String code, boolean spaceLevel)
    {
        if (code == null || code.length() == 0)
        {
            throw new IllegalArgumentException("Unspecified code.");
        }
        this.code = code;
        this.spaceLevel = spaceLevel;
    }

    /**
     * Returns role code.
     */
    public final String getCode()
    {
        return code;
    }

    /**
     * Returns <code>true</code> if this role is for a particular space.
     */
    public final boolean isSpaceLevel()
    {
        return spaceLevel;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Role == false)
        {
            return false;
        }
        Role role = (Role) obj;
        return role.spaceLevel == spaceLevel && role.code.equals(code);
    }

    @Override
    public int hashCode()
    {
        return 37 * code.hashCode() + (spaceLevel ? 1 : 0);
    }

    @Override
    public String toString()
    {
        return code + "(" + (spaceLevel ? "space" : "instance") + ")";
    }

    //
    // JSON-RPC
    //
    private Role()
    {
    }

    private void setCode(String code)
    {
        this.code = code;
    }

    private void setSpaceLevel(boolean spaceLevel)
    {
        this.spaceLevel = spaceLevel;
    }
}
