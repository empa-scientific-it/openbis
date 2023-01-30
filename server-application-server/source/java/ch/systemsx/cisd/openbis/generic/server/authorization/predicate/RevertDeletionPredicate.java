/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author Pawel Glyzewski
 */
@ShouldFlattenCollections(value = false)
public class RevertDeletionPredicate extends AbstractPredicate<List<TechId>>
{
    private IAuthorizationDataProvider provider;

    private DeletionTechIdCollectionPredicate deletionTechIdCollectionPredicate;

    public RevertDeletionPredicate()
    {
        this.deletionTechIdCollectionPredicate = new DeletionTechIdCollectionPredicate();
    }

    @Override
    public void init(@SuppressWarnings("hiding") IAuthorizationDataProvider provider)
    {
        this.provider = provider;
        deletionTechIdCollectionPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "revert deletion technical id";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<TechId> value)
    {
        if (isInstanceAdmin(person))
        {
            return Status.OK;
        } else
        {
            List<DeletionPE> deletions = provider.getDeletions(value);
            List<TechId> deletionsToCheck = new ArrayList<TechId>();
            for (DeletionPE deletion : deletions)
            {
                if (false == isRegistrator(person, deletion))
                {
                    deletionsToCheck.add(new TechId(deletion.getId()));
                }
            }
            if (deletionsToCheck.size() > 0)
            {
                return deletionTechIdCollectionPredicate.evaluate(person, allowedRoles,
                        deletionsToCheck);
            }
        }

        return Status.OK;
    }

    private boolean isRegistrator(final PersonPE person, final DeletionPE value)
    {
        PersonPE registrator = value.getRegistrator();
        return person.getUserId().equals(registrator.getUserId());
    }

    private static boolean isInstanceAdmin(final PersonPE person)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            if (roleAssignment.getRoleWithHierarchy().isInstanceLevel() && roleAssignment.getRole().equals(RoleCode.ADMIN))
            {
                return true;
            }
        }
        return false;
    }
}
