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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ContentCopySearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.AbstractCompositeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchCriteriaTranslationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author anttil
 */
public class ContentCopySearchCriteriaTranslator extends AbstractCompositeSearchCriteriaTranslator
{

    public ContentCopySearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof ContentCopySearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        context.pushObjectKind(SearchObjectKind.CONTENT_COPY);
        SearchCriteriaTranslationResult translationResult = super.doTranslate(context, criteria);
        context.popObjectKind();

        return translationResult;
    }

}
