/*
 * Copyright 2009 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * An <code>IPredicate</code> implementation based on permId of a sample.
 * 
 * @author Izabela Adamczyk
 */
public class SamplePermIdPredicate extends AbstractDatabaseInstancePredicate<PermId>
{

    private final SamplePEPredicate samplePEPredicate;

    private final boolean nullAllowed;

    public SamplePermIdPredicate()
    {
        this(true, false);
    }

    public SamplePermIdPredicate(boolean isReadAccess, boolean nullAllowed)
    {
        this.nullAllowed = nullAllowed;
        samplePEPredicate = new SamplePEPredicate(isReadAccess);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        samplePEPredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample perm id";
    }

    @Override
    protected final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final PermId id)
    {
        SamplePE sample = authorizationDataProvider.tryGetSampleByPermId(id.getId());
        if (sample == null)
        {
            return nullAllowed ? Status.OK
                    : Status.createError(String.format("There is no sample with perm id '%s'.", id.getId()));
        }
        return samplePEPredicate.evaluate(person, allowedRoles, sample);
    }

}
