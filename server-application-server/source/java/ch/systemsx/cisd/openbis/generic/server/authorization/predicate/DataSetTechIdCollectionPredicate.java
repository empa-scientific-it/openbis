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
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;

/**
 * An <code>IPredicate</code> implementation based on a list of data set {@link TechId}s.
 * 
 * @author pkupczyk
 */
@ShouldFlattenCollections(value = false)
public class DataSetTechIdCollectionPredicate extends DelegatedPredicate<Collection<DataSetAccessPE>, List<TechId>>
{

    public DataSetTechIdCollectionPredicate()
    {
        super(new DataSetAccessPECollectionPredicate());
    }

    @Override
    public Collection<DataSetAccessPE> tryConvert(List<TechId> techIds)
    {
        return authorizationDataProvider.getDatasetCollectionAccessDataByTechIds(techIds, true);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "data set technical ids";
    }

}
