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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.AbstractCompositeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchCriteriaTranslationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;

/**
 * @author pkupczyk
 */
public class MaterialSearchCriteriaTranslator extends AbstractCompositeSearchCriteriaTranslator
{

    public MaterialSearchCriteriaTranslator(IDAOFactory idaoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(idaoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof MaterialSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        context.pushObjectKind(SearchObjectKind.MATERIAL);
        SearchCriteriaTranslationResult translationResult = super.doTranslate(context, criteria);
        context.popObjectKind();

        if (context.peekObjectKind() == null)
        {
            return translationResult;
        } else
        {
            throw new IllegalArgumentException("Material criteria cannot be nested inside other criteria");
        }
    }

}
