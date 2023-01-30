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
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link SampleOwnerIdentifier}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@ShouldFlattenCollections(value = false)
public final class SampleOwnerIdentifierCollectionPredicate extends
        AbstractPredicate<List<SampleOwnerIdentifier>>
{
    private final SpaceIdentifierPredicate spacePredicate;

    private final DatabaseInstanceIdentifierPredicate databaseInstanceIdentifierPredicate;

    public SampleOwnerIdentifierCollectionPredicate()
    {
        this(true);
    }

    public SampleOwnerIdentifierCollectionPredicate(boolean isReadAccess)
    {
        spacePredicate = new SpaceIdentifierPredicate();
        databaseInstanceIdentifierPredicate = new DatabaseInstanceIdentifierPredicate(isReadAccess);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        spacePredicate.init(provider);
        databaseInstanceIdentifierPredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample identifier";
    }

    @Override
    protected final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final List<SampleOwnerIdentifier> values)
    {
        for (SampleOwnerIdentifier value : values)
        {
            if (value.isDatabaseInstanceLevel())
            {
                Status result =
                        databaseInstanceIdentifierPredicate.doEvaluation(person, allowedRoles, null);
                if (Status.OK != result)
                {
                    return result;
                }
            } else
            {
                Status result =
                        spacePredicate.doEvaluation(person, allowedRoles, value.getSpaceLevel());
                if (Status.OK != result)
                {
                    return result;
                }
            }
        }
        return Status.OK;
    }
}
