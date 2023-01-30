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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdsSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.AbstractFieldFromCompositeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public abstract class PersonSearchCriteriaTranslator extends AbstractFieldFromCompositeSearchCriteriaTranslator
{

    public PersonSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected DetailedSearchCriterion doTranslateField(SearchTranslationContext context, ISearchCriteria criteria, ISearchCriteria subCriteria)
    {
        if (subCriteria instanceof UserIdsSearchCriteria)
        {
            UserIdsSearchCriteria userIdsCriteria = (UserIdsSearchCriteria) subCriteria;
            IAttributeSearchFieldKind userIdFieldKind = getUserIdFieldKind(context.peekObjectKind());
            DetailedSearchField searchField = DetailedSearchField.createAttributeField(userIdFieldKind);
            return new DetailedSearchCriterion(searchField, userIdsCriteria.getFieldValue());
        } else
        {
            throw new IllegalArgumentException("Unknown criteria: " + subCriteria.getClass());
        }
    }

    protected abstract IAttributeSearchFieldKind getUserIdFieldKind(SearchObjectKind objectKind);

}
