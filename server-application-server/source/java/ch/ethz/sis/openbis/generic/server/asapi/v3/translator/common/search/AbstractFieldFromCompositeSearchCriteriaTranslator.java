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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public abstract class AbstractFieldFromCompositeSearchCriteriaTranslator extends AbstractFieldSearchCriteriaTranslator
{

    public AbstractFieldFromCompositeSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        AbstractCompositeSearchCriteria compositeCriteria = (AbstractCompositeSearchCriteria) criteria;
        Collection<ISearchCriteria> subCriteria = compositeCriteria.getCriteria();

        if (subCriteria != null && false == subCriteria.isEmpty())
        {
            List<DetailedSearchCriterion> detailedCriteria = new LinkedList<DetailedSearchCriterion>();

            for (ISearchCriteria subCriterion : subCriteria)
            {
                DetailedSearchCriterion detailedCriterion = doTranslateField(context, compositeCriteria, subCriterion);
                detailedCriteria.add(detailedCriterion);
            }

            return new SearchCriteriaTranslationResult(detailedCriteria.toArray(new DetailedSearchCriterion[0]));
        }

        return null;
    }

    protected DetailedSearchCriterion doTranslateField(SearchTranslationContext context, ISearchCriteria criteria, ISearchCriteria subCriteria)
    {
        ISearchCriteriaTranslator subTranslator =
                new SearchCriteriaTranslatorFactory(getDaoFactory(), getEntityAttributeProviderFactory())
                        .getTranslator(subCriteria);

        SearchCriteriaTranslationResult translationResult = subTranslator.translate(context, subCriteria);
        Collection<String> values = translationResult.getCriterion().getValues();
        DetailedSearchField searchField = doTranslateSearchField(context, criteria, subCriteria);
        DetailedSearchCriterion detailedSearchCriterion = new DetailedSearchCriterion(searchField, values);
        return detailedSearchCriterion;
    }

    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriteria criteria,
            ISearchCriteria subCriteria)
    {
        IAttributeSearchFieldKind attribute = getEntityAttributeProvider(context).getAttribute(criteria);
        DetailedSearchField searchField = DetailedSearchField.createAttributeField(attribute);
        return searchField;
    }

}
