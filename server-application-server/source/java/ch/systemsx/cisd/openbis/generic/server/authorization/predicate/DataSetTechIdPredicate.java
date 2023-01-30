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

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;

/**
 * An <code>IPredicate</code> implementation based on {@link TechId} of a data set.
 * 
 * @author pkupczyk
 */
public class DataSetTechIdPredicate extends DelegatedPredicate<Collection<DataSetAccessPE>, TechId>
{

    public DataSetTechIdPredicate()
    {
        super(new DataSetAccessPECollectionPredicate());
    }

    @Override
    public Collection<DataSetAccessPE> tryConvert(TechId techId)
    {
        if (techId == null)
        {
            return Arrays.asList();
        } else
        {
            return authorizationDataProvider.getDatasetCollectionAccessDataByTechIds(Arrays.asList(techId), true);
        }
    }

    @Override
    public final String getCandidateDescription()
    {
        return "data set technical id";
    }

}
