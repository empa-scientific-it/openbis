/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;

/**
 * An <code>IPredicate</code> implementation based on {@link TechId} of a sample.
 * 
 * @author Piotr Buczek
 */
public class SampleTechIdPredicate extends AbstractDatabaseInstancePredicate<TechId>
{

    private final SampleAccessPECollectionPredicate sampleAccessPECollectionPredicate;

    public SampleTechIdPredicate()
    {
        this(true);
    }

    public SampleTechIdPredicate(boolean isReadAccess)
    {
        sampleAccessPECollectionPredicate = new SampleAccessPECollectionPredicate(isReadAccess);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        sampleAccessPECollectionPredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample technical id";
    }

    @Override
    protected final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final TechId techId)
    {
        Set<SampleAccessPE> samples =
                authorizationDataProvider.getSampleCollectionAccessDataByTechIds(techId != null ? Arrays.asList(techId) : Arrays.asList(), true);
        return sampleAccessPECollectionPredicate.doEvaluation(person, allowedRoles, samples);
    }

}
