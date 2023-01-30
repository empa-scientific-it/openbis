/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.CompleteSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.EnumFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProviderFactory;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
public class CompleteSearchCriteriaTranslator extends EnumFieldSearchCriteriaTranslator
{

    public CompleteSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof CompleteSearchCriteria;
    }

    @Override
    protected Map<Complete, String> getValueToIndexedValueMapping()
    {
        Map<Complete, String> map = new HashMap<Complete, String>();
        map.put(Complete.YES, BooleanOrUnknown.T.name());
        map.put(Complete.NO, BooleanOrUnknown.F.name());
        map.put(Complete.UNKNOWN, BooleanOrUnknown.U.name());
        return map;
    }

}
