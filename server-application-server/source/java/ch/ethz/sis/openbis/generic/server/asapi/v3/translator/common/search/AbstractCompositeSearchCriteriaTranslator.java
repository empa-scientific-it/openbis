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
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * @author pkupczyk
 */
public abstract class AbstractCompositeSearchCriteriaTranslator extends AbstractSearchCriteriaTranslator
{

    private IDAOFactory daoFactory;

    private IObjectAttributeProviderFactory entityAttributeProviderFactory;

    protected AbstractCompositeSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        this.daoFactory = daoFactory;
        this.entityAttributeProviderFactory = entityAttributeProviderFactory;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        AbstractCompositeSearchCriteria compositeCriteria = (AbstractCompositeSearchCriteria) criteria;

        DetailedSearchCriteria detailedSearchCriteria = new DetailedSearchCriteria();
        detailedSearchCriteria.setUseWildcardSearchMode(true);

        if (criteria instanceof AbstractEntitySearchCriteria)
        {
            detailedSearchCriteria.setConnection(translateOperator(((AbstractEntitySearchCriteria<?>) compositeCriteria).getOperator()));
        }

        List<DetailedSearchCriterion> detailedCriterionList = new ArrayList<DetailedSearchCriterion>();
        List<DetailedSearchSubCriteria> detailedSubCriteriaList = new ArrayList<DetailedSearchSubCriteria>();

        for (ISearchCriteria subCriterion : compositeCriteria.getCriteria())
        {
            ISearchCriteriaTranslator subTranslator =
                    new SearchCriteriaTranslatorFactory(getDaoFactory(), getEntityAttributeProviderFactory()).getTranslator(subCriterion);

            SearchCriteriaTranslationResult translationResult = subTranslator.translate(context, subCriterion);

            if (translationResult == null)
            {
                continue;
            } else
            {
                for (DetailedSearchCriteria additionalCriteria : translationResult.getCriteriaList())
                {
                    detailedCriterionList.addAll(additionalCriteria.getCriteria());
                }
                detailedCriterionList.addAll(translationResult.getCriterionList());
                detailedSubCriteriaList.addAll(translationResult.getSubCriteriaList());
            }
        }

        detailedSearchCriteria.setCriteria(detailedCriterionList);
        detailedSearchCriteria.setSubCriterias(detailedSubCriteriaList);
        return new SearchCriteriaTranslationResult(detailedSearchCriteria);
    }

    private SearchCriteriaConnection translateOperator(SearchOperator operator)
    {
        switch (operator)
        {
            case AND:
                return SearchCriteriaConnection.MATCH_ALL;
            case OR:
                return SearchCriteriaConnection.MATCH_ANY;

        }
        throw new IllegalArgumentException("Unknown search operator: " + operator);
    }

    public IDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    public IObjectAttributeProviderFactory getEntityAttributeProviderFactory()
    {
        return entityAttributeProviderFactory;
    }

}
