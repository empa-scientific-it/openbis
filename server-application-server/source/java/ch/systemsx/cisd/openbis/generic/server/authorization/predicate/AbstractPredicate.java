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

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <i>abstract</i> <code>IPredicate</code> implementation which mainly checks method parameters before doing the real work.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractPredicate<T> implements IPredicate<T>
{
    /**
     * Returns a simple description for the exception to be thrown.
     */
    public abstract String getCandidateDescription();

    /**
     * Does the validation after parameters have been checked.
     * <p>
     * Must be implemented by subclasses.
     * </p>
     */
    protected abstract Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T value);

    /**
     * Is the checked value allowed to be <code>null</code>, <code>false</code> by default. Can be overridden in sub-classes.
     */
    protected boolean isNullValueAllowed()
    {
        return false;
    }

    /**
     * Returns <code>true</code> if <var>allowedRoles</var> contains a role that allows writing to all entities.
     */
    public static Status hasInstanceWritePermissions(PersonPE aPerson,
            List<RoleWithIdentifier> allowedRoles)
    {
        for (RoleWithIdentifier role : allowedRoles)
        {
            if (role.getRoleLevel() == RoleLevel.INSTANCE)
            {
                final RoleCode roleCode = role.getRole().getRoleCode();
                if (RoleCode.ADMIN.equals(roleCode) || RoleCode.ETL_SERVER.equals(roleCode))
                {
                    return Status.OK;
                }
            }
        }
        return Status.createError(false,
                "None of method roles '[INSTANCE_ETL_SERVER, INSTANCE_ADMIN]' could be found in roles of user '"
                        + aPerson.getUserId() + "'.");
    }

    /**
     * Returns <code>true</code> if <var>allowedRoles</var> contains a role that allows reading all entities.
     */
    public static Status hasInstanceReadPermissions(PersonPE aPerson,
            List<RoleWithIdentifier> allowedRoles)
    {
        for (RoleWithIdentifier role : allowedRoles)
        {
            if (role.getRoleLevel() == RoleLevel.INSTANCE)
            {
                final RoleCode roleCode = role.getRole().getRoleCode();
                if (RoleCode.ADMIN.equals(roleCode) || RoleCode.ETL_SERVER.equals(roleCode)
                        || RoleCode.OBSERVER.equals(roleCode))
                {
                    return Status.OK;
                }
            }
        }
        return Status
                .createError(false,
                        "None of method roles '[INSTANCE_ETL_SERVER, INSTANCE_ADMIN, INSTANCE_OBSERVER]' "
                                + "could be found in roles of user '"
                                + aPerson.getUserId() + "'.");
    }

    //
    // IPredicate
    //

    @Override
    public final Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T valueOrNull)
    {
        assert person != null : "Unspecified person";
        assert allowedRoles != null : "Unspecified allowed roles";
        if (valueOrNull == null && isNullValueAllowed() == false)
        {
            throw UserFailureException.fromTemplate("No %s specified.", getCandidateDescription());
        }
        try
        {
            return doEvaluation(person, allowedRoles, valueOrNull);
        } catch (DataAccessException ex)
        {
            throw new UserFailureException(ex.getMessage(), ex);
        }
    }
}
