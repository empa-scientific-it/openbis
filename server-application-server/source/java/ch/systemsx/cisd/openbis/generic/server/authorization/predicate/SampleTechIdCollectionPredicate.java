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

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;

/**
 * An <code>IPredicate</code> implementation based on a list of sample {@link TechId}s.
 * 
 * @author Piotr Buczek
 */
@ShouldFlattenCollections(value = false)
public class SampleTechIdCollectionPredicate extends DelegatedPredicate<Collection<SampleAccessPE>, List<TechId>>
{

    public SampleTechIdCollectionPredicate()
    {
        this(true);
    }

    public SampleTechIdCollectionPredicate(boolean isReadAccess)
    {
        super(new SampleAccessPECollectionPredicate(isReadAccess));
    }

    @Override
    public Collection<SampleAccessPE> tryConvert(List<TechId> techIds)
    {
        return authorizationDataProvider.getSampleCollectionAccessDataByTechIds(techIds, true);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample technical ids";
    }

}
