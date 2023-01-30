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
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

/**
 * An <code>IPredicate</code> implementation based on {@link SpaceIdentifier}.
 * 
 * @author Christian Ribeaud
 */
@ShouldFlattenCollections
public class SpaceIdentifierPredicate extends AbstractSpacePredicate<SpaceIdentifier>
{
    public SpaceIdentifierPredicate()
    {
    }

    public SpaceIdentifierPredicate(boolean okForNonExistentSpaces)
    {
        super(okForNonExistentSpaces);
    }

    //
    // AbstractDatabaseInstancePredicate
    //

    @Override
    public final String getCandidateDescription()
    {
        return "space identifier";
    }

    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final SpaceIdentifier spaceIdentifier)
    {
        // Skip all further checks if the person has instance-wide write permissions.
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        assert initialized : "Predicate has not been initialized";
        final String spaceCode = SpaceCodeHelper.getSpaceCode(person, spaceIdentifier);
        return evaluate(allowedRoles, person, spaceCode);
    }
}
