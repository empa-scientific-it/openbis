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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search;

import java.util.ArrayList;
import java.util.Collection;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * @author pkupczyk
 */
public class CollectionSearchCriteriaTranslator extends AbstractFieldSearchCriteriaTranslator
{

    public CollectionSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof CollectionFieldSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        CollectionFieldSearchCriteria<?> collectionCriteria = (CollectionFieldSearchCriteria<?>) criteria;
        Collection<String> stringValues = new ArrayList<String>();

        if (collectionCriteria.getFieldValue() != null)
        {
            for (Object value : collectionCriteria.getFieldValue())
            {
                if (value != null)
                {
                    stringValues.add(value.toString());
                }
            }
        }

        return new SearchCriteriaTranslationResult(
                new DetailedSearchCriterion(getDetailedSearchField(context, collectionCriteria), stringValues));
    }

}
