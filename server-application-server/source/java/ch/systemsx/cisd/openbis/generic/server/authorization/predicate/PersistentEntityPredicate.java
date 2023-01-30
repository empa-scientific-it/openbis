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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * A predicate for persistent entities. This predicate by default authenticates for write access.
 * 
 * @author anttil
 */
public abstract class PersistentEntityPredicate<T> implements IPredicate<T>
{
    // Everyone can read from the database instance level, but only users with appropriate role can
    // write. This flag tells if only the read-only access is required to database instance objects.
    private final boolean isReadAccess;

    protected IAuthorizationDataProvider provider;

    /**
     * Default: authenticate for write access.
     */
    public PersistentEntityPredicate()
    {
        this(false);
    }

    public PersistentEntityPredicate(boolean isReadAccess)
    {
        this.isReadAccess = isReadAccess;
    }

    @Override
    public Status evaluate(PersonPE person, List<RoleWithIdentifier> allowedRoles, T value)
            throws UserFailureException
    {
        final SpacePE space = getSpace(value);
        final boolean isInstanceEntity = (space == null);
        if (isInstanceEntity && isReadAccess)
        {
            return Status.OK;
        }

        for (RoleWithIdentifier allowed : allowedRoles)
        {
            RoleLevel level = allowed.getRoleLevel();

            if (level.equals(RoleLevel.INSTANCE))
            {
                return Status.OK;
            } else if (level.equals(RoleLevel.SPACE) && allowed.getAssignedSpace().equals(space))
            {
                return Status.OK;
            }
        }

        return Status.createError(person.getUserId() + " does not have enough privileges.");
    }

    public abstract SpacePE getSpace(T value);

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        this.provider = provider;
    }
}
