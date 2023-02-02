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

package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractSpacePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * A predicate for {@link PlateIdentifier}.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Piotr Buczek
 */
public class PlateIdentifierPredicate extends AbstractSpacePredicate<PlateIdentifier>
{

    private final SampleIdentifierPredicate sampleIdentifierPredicate;

    public PlateIdentifierPredicate()
    {
        this(true);
    }

    public PlateIdentifierPredicate(boolean isReadAccess)
    {
        sampleIdentifierPredicate = new SampleIdentifierPredicate(isReadAccess);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        sampleIdentifierPredicate.init(provider);
    }

    @Override
    public String getCandidateDescription()
    {
        return "plate identifier";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            PlateIdentifier value)
    {
        final SampleIdentifier plateIdentifier;

        if (value.getPermId() != null)
        {
            final SamplePE sample =
                    authorizationDataProvider.tryGetSampleByPermId(value.getPermId());
            if (sample == null)
            {
                return Status.createError(String.format(
                        "User '%s' does not have enough privileges.", person.getUserId()));
            }
            plateIdentifier = sample.getSampleIdentifier();
        } else
        {
            plateIdentifier = SampleIdentifierFactory.parse(value.getAugmentedCode());
        }
        return performSamplePredicateEvaluation(person, allowedRoles, plateIdentifier);
    }

    private Status performSamplePredicateEvaluation(PersonPE person,
            List<RoleWithIdentifier> allowedRoles, SampleIdentifier sampleIdentifier)
    {
        return sampleIdentifierPredicate.evaluate(person, allowedRoles, sampleIdentifier);
    }
}
