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
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectTechIdPredicate extends AbstractPredicate<TechId>
{
    private IAuthorizationDataProvider authorizationDataProvider;

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        this.authorizationDataProvider = provider;
    }

    @Override
    public String getCandidateDescription()
    {
        return "Metaproject TechId";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            TechId metaprojectId)
    {
        MetaprojectPE metaprojectPE = authorizationDataProvider.getMetaproject(metaprojectId);

        if (metaprojectPE.getOwner().getId().longValue() == person.getId().longValue())
        {
            return Status.OK;
        }

        return Status.createError(String.format(
                "User '%s' is not an owner of the metaproject '%s'.", person.getUserId(),
                metaprojectPE.getName()));
    }
}
