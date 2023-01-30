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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Hierarchical role. Combines {@link RoleCode} with {@link RoleLevel} and a set of {@link RoleWithHierarchy}s that are stronger.
 * <p>
 * Available roles can:
 * <ol>
 * <li>be presented to the user
 * <li>be easily mapped to database structure
 * <li>be used to restrict access to server methods
 * <li>define the role hierarchy by specifying which roles are stronger (users that have only the "stronger" role will also be able to access given
 * server method)
 * </ol>
 * </p>
 * <h4>Example:</h4>To annotate an interface method with a new role e.g. <code>SECRET_AGENT</code>:
 * <ol>
 * <li>Add <code>SECRET_AGENT</code> to {@link RoleCode} enumerator.
 * <li>Add <code>SECRET_AGENT</code> to <code>authorization_role</code> domain in the database (and prepare migration).
 * <li>Add <code>INSTANCE_SECRET_AGENT</code> (or <code>SPACE_SECRET_AGENT</code>) to {@link RoleWithHierarchy} enumerator and define the "stronger"
 * roles.
 * <li>Use the new {@link RoleWithHierarchy} to annotate the interface method.
 * </ol>
 * 
 * @author Izabela Adamczyk
 */
public enum RoleWithHierarchy implements Serializable
{
    //
    // NOTE: Each role should match the following naming convention: <RoleLevel>_<RoleCode>,
    // it will be used to automatically figure the RoleLevel and RoleCode.
    //

    INSTANCE_DISABLED(true),

    INSTANCE_ADMIN,

    INSTANCE_OBSERVER(INSTANCE_ADMIN),

    INSTANCE_ETL_SERVER(INSTANCE_ADMIN),

    SPACE_ADMIN(INSTANCE_ADMIN),

    SPACE_POWER_USER(SPACE_ADMIN),

    SPACE_USER(SPACE_POWER_USER),

    SPACE_OBSERVER(SPACE_USER, INSTANCE_OBSERVER),

    SPACE_ETL_SERVER(INSTANCE_ETL_SERVER),

    PROJECT_ADMIN(SPACE_ADMIN),

    PROJECT_POWER_USER(PROJECT_ADMIN, SPACE_POWER_USER),

    PROJECT_USER(PROJECT_POWER_USER, SPACE_USER),

    PROJECT_OBSERVER(PROJECT_USER, SPACE_OBSERVER)

    ;

    public static enum RoleLevel implements IsSerializable
    {
        INSTANCE, SPACE, PROJECT;
    }

    /**
     * Role codes corresponding to values stored in the database.
     */
    // NOTE: Adding values to this class should be followed by extending appropriate database
    // domain.
    public static enum RoleCode implements IsSerializable
    {
        DISABLED, ADMIN, USER, POWER_USER, OBSERVER, ETL_SERVER;
    }

    /**
     * Returns the {@link RoleWithHierarchy} defined by given {@link RoleLevel} and {@link RoleCode}
     */
    public static RoleWithHierarchy valueOf(final RoleLevel roleLevel, final RoleCode roleCode)
    {
        return RoleWithHierarchy.valueOf(roleLevel.name() + SEPARATOR + roleCode.name());
    }

    private static final String ERROR_MSG_ROLE_DOESN_T_MATCH_NAMING_CONVENTION =
            "Role doesn't match naming convention";

    private static final String SEPARATOR = "_";

    private final RoleCode roleCode;

    private final RoleLevel roleLevel;

    private final boolean completelyEmpty;

    private final Set<RoleWithHierarchy> strongerRoles = new LinkedHashSet<RoleWithHierarchy>();

    private RoleWithHierarchy(boolean completelyEmpty)
    {
        this.completelyEmpty = completelyEmpty;
        roleLevel = figureRoleLevel(name());
        roleCode = figureRoleCode(name(), roleLevel);
    }

    private RoleWithHierarchy(RoleWithHierarchy... strongerRoles)
    {
        completelyEmpty = false;
        roleLevel = figureRoleLevel(name());
        roleCode = figureRoleCode(name(), roleLevel);
        for (RoleWithHierarchy strongerRole : strongerRoles)
        {
            getStrongerRoles().add(strongerRole);
            for (RoleWithHierarchy role : strongerRole.getStrongerRoles())
            {
                getStrongerRoles().add(role);
            }
        }
    }

    private Set<RoleWithHierarchy> getStrongerRoles()
    {
        return strongerRoles;
    }

    static RoleLevel figureRoleLevel(String roleWithHierarchyName)
    {
        for (RoleLevel level : RoleLevel.values())
        {
            if (roleWithHierarchyName.startsWith(level.name() + SEPARATOR))
            {
                return level;
            }
        }
        throw new IllegalArgumentException(ERROR_MSG_ROLE_DOESN_T_MATCH_NAMING_CONVENTION);
    }

    static RoleCode figureRoleCode(String roleWithHierarchyName, RoleLevel roleLevel)
    {
        assert roleWithHierarchyName.startsWith(roleLevel.name() + SEPARATOR);
        for (RoleCode code : RoleCode.values())
        {
            if (code.name().equals(roleWithHierarchyName.substring(roleLevel.name().length() + 1)))
            {
                return code;
            }
        }
        throw new IllegalArgumentException(ERROR_MSG_ROLE_DOESN_T_MATCH_NAMING_CONVENTION);
    }

    public Set<RoleWithHierarchy> getRoles()
    {
        Set<RoleWithHierarchy> roles = new LinkedHashSet<RoleWithHierarchy>();
        if (completelyEmpty == false)
        {
            roles.add(this);
        }
        roles.addAll(strongerRoles);
        return roles;
    }

    public boolean isInstanceLevel()
    {
        return roleLevel.equals(RoleLevel.INSTANCE);
    }

    public boolean isSpaceLevel()
    {
        return roleLevel.equals(RoleLevel.SPACE);
    }

    public boolean isProjectLevel()
    {
        return roleLevel.equals(RoleLevel.PROJECT);
    }

    public RoleLevel getRoleLevel()
    {
        return roleLevel;
    }

    public final RoleCode getRoleCode()
    {
        return roleCode;
    }

    @Override
    public String toString()
    {
        return name();
    }

}