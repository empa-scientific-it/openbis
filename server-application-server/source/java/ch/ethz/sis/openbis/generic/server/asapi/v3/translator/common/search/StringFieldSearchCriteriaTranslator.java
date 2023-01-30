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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringGreaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringGreaterThanValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringLessThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringLessThanValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;

/**
 * @author pkupczyk
 */
public class StringFieldSearchCriteriaTranslator extends AbstractFieldSearchCriteriaTranslator
{

    public StringFieldSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof StringFieldSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        StringFieldSearchCriteria stringCriteria = (StringFieldSearchCriteria) criteria;

        AbstractStringValue valueObject = stringCriteria.getFieldValue();
        String value;

        if (valueObject instanceof StringEqualToValue)
        {
            StringEqualToValue equalToValue = (StringEqualToValue) valueObject;
            value = equalToValue.getValue();
        } else if (valueObject instanceof StringLessThanValue)
        {
            StringLessThanValue lessToValue = (StringLessThanValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, stringCriteria),
                    CompareType.LESS_THAN, lessToValue.getValue()));
        } else if (valueObject instanceof StringLessThanOrEqualToValue)
        {
            StringLessThanOrEqualToValue lessOrEqualToValue = (StringLessThanOrEqualToValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, stringCriteria),
                    CompareType.LESS_THAN_OR_EQUAL, lessOrEqualToValue.getValue()));
        } else if (valueObject instanceof StringGreaterThanOrEqualToValue)
        {
            StringGreaterThanOrEqualToValue greaterOrEqualToValue = (StringGreaterThanOrEqualToValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, stringCriteria),
                    CompareType.MORE_THAN_OR_EQUAL, greaterOrEqualToValue.getValue()));
        } else if (valueObject instanceof StringGreaterThanValue)
        {
            StringGreaterThanValue greaterToValue = (StringGreaterThanValue) valueObject;
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, stringCriteria),
                    CompareType.MORE_THAN, greaterToValue.getValue()));
        } else if (valueObject instanceof StringStartsWithValue)
        {
            StringStartsWithValue startsWithValue = (StringStartsWithValue) valueObject;
            value = startsWithValue.getValue() + "*";
        } else if (valueObject instanceof StringEndsWithValue)
        {
            StringEndsWithValue endsWithValue = (StringEndsWithValue) valueObject;
            value = "*" + endsWithValue.getValue();
        } else if (valueObject instanceof StringContainsValue)
        {
            StringContainsValue endsWithValue = (StringContainsValue) valueObject;
            value = "*" + endsWithValue.getValue() + "*";
        } else if (valueObject instanceof AnyStringValue)
        {
            value = "*";
        } else if (valueObject != null)
        {
            throw new IllegalArgumentException("Unknown string field value: " + valueObject);
        } else
        {
            throw new IllegalArgumentException("Unspecified value of criteria '" + criteria + "'.");
        }

        return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(getDetailedSearchField(context, stringCriteria), value));
    }

}
