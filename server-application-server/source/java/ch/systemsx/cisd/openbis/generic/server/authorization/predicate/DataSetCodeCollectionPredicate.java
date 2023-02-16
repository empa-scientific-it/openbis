/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;

/**
 * A {@link IPredicate} based on a list of data set codes.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@ShouldFlattenCollections(value = false)
public class DataSetCodeCollectionPredicate extends DelegatedPredicate<Collection<DataSetAccessPE>, List<String>>
{

    public DataSetCodeCollectionPredicate()
    {
        super(new DataSetAccessPECollectionPredicate());
    }

    @Override
    public Collection<DataSetAccessPE> tryConvert(List<String> codes)
    {
        return authorizationDataProvider.getDatasetCollectionAccessDataByCodes(codes);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "data set codes";
    }

}
