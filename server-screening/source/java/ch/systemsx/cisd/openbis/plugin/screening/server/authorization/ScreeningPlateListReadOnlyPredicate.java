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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleAccessPECollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdentifierCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedSpaceException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * A predicate for lists of entities which have {@link PlateIdentifier} as their super-class. This predicate authorizes for read-only access, i.e. it
 * will allow access to shared samples for all users.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
@ShouldFlattenCollections(value = false)
public class ScreeningPlateListReadOnlyPredicate extends AbstractPredicate<List<? extends PlateIdentifier>>
{

    private IAuthorizationDataProvider provider;

    private SampleIdentifierCollectionPredicate identifierCollectionPredicate = new SampleIdentifierCollectionPredicate(true);

    private SampleAccessPECollectionPredicate accessPECollectionPredicate = new SampleAccessPECollectionPredicate(true);

    @Override
    public void init(IAuthorizationDataProvider dataProvider)
    {
        provider = dataProvider;
        identifierCollectionPredicate.init(provider);
        accessPECollectionPredicate.init(provider);
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            List<? extends PlateIdentifier> plates)
    {
        List<SampleIdentifier> identifiers = new ArrayList<SampleIdentifier>();
        List<PermId> permIds = new ArrayList<PermId>();

        for (PlateIdentifier plate : plates)
        {
            final String spaceCodeOrNull = SpaceCodeHelper.tryGetSpaceCode(person, plate.tryGetSpaceCode());

            if (spaceCodeOrNull == null && plate.getPermId() == null)
            {
                throw new UndefinedSpaceException();
            }

            if (spaceCodeOrNull != null)
            {
                SampleIdentifier identifier = SampleIdentifierFactory.parse(plate.getAugmentedCode());
                identifiers.add(identifier);
            }

            if (plate.getPermId() != null)
            {
                permIds.add(new PermId(plate.getPermId()));
            }
        }

        if (false == identifiers.isEmpty())
        {
            Status status = identifierCollectionPredicate.evaluate(person, allowedRoles, identifiers);

            if (status.isError())
            {
                return status;
            }
        }

        if (false == permIds.isEmpty())
        {
            Collection<SampleAccessPE> accessPECollection = provider.getSampleCollectionAccessDataByPermIds(permIds);
            Status status = accessPECollectionPredicate.evaluate(person, allowedRoles, accessPECollection);

            if (status.isError())
            {
                return status;
            }
        }

        return Status.OK;
    }

    @Override
    public String getCandidateDescription()
    {
        return "plate";
    }

}
