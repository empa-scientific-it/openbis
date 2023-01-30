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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation for {@link DatabaseInstanceIdentifier}.
 * 
 * @author Christian Ribeaud
 */
public final class DatabaseInstanceIdentifierPredicate extends
        AbstractDatabaseInstancePredicate<Void>
{
    // Everyone can read from the database instance level, but only users with appropriate role can
    // write. This flag tells if only the read-only access is required to database instance objects.
    private final boolean isReadAccess;

    public DatabaseInstanceIdentifierPredicate(boolean isReadAccess)
    {
        this.isReadAccess = isReadAccess;
    }

    private final static boolean isMatching(final List<RoleWithIdentifier> allowedRoles, final boolean isReadAccess)
    {
        if (isReadAccess)
        {
            return true;
        }
        for (final RoleWithIdentifier role : allowedRoles)
        {
            final RoleLevel roleGroup = role.getRoleLevel();
            if (roleGroup.equals(RoleLevel.INSTANCE))
            {
                return true;
            }
        }
        return false;
    }

    //
    // AbstractDatabaseInstancePredicate
    //

    @Override
    public final String getCandidateDescription()
    {
        return "database instance identifier";
    }

    @Override
    protected final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, Void v)
    {
        assert initialized : "Predicate has not been initialized";
        final boolean matching = isMatching(allowedRoles, isReadAccess);
        if (matching)
        {
            return Status.OK;
        }
        String userId = person.getUserId();
        return Status.createError(createErrorMsg(userId));
    }

    private String createErrorMsg(String userId)
    {
        String accessType = isReadAccess ? "read from" : "modify";
        return String.format(STATUS_MESSAGE_PREFIX_FORMAT + "%s instance level entities.", userId, accessType);
    }
}
