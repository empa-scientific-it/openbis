/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space.search;

import java.util.EnumSet;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.AbstractFieldFromCompositeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchCriteriaTranslationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SimpleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.NullBridge;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * @author pkupczyk
 */
public class SpaceSearchCriteriaTranslator extends AbstractFieldFromCompositeSearchCriteriaTranslator
{
    private static final DetailedSearchField SPACE_ID_FIELD = DetailedSearchField.createAttributeField(
            new SimpleAttributeSearchFieldKind(SearchFieldConstants.SAMPLE_SPACE_ID, "Space ID"));

    private static final EnumSet<SearchObjectKind> ENTITY_KINDS_WITH_SPACE = EnumSet.of(SearchObjectKind.EXPERIMENT, SearchObjectKind.SAMPLE);

    public SpaceSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof NoSpaceSearchCriteria || criteria instanceof SpaceSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        SearchObjectKind peekObjectKind = context.peekObjectKind();
        if (ENTITY_KINDS_WITH_SPACE.contains(peekObjectKind) == false)
        {
            throw new IllegalArgumentException("Space criteria can be used only in experiment and sample criteria, "
                    + "but was used in: " + peekObjectKind + " context.");
        }
        if (criteria instanceof NoSpaceSearchCriteria)
        {
            if (SearchObjectKind.SAMPLE.equals(peekObjectKind) == false)
            {
                throw new IllegalArgumentException("No space criteria can be used only in "
                        + "sample search criteria, but was used in: " + peekObjectKind + " context.");
            }
            return new SearchCriteriaTranslationResult(new DetailedSearchCriterion(SPACE_ID_FIELD, NullBridge.NULL));
        }
        return super.doTranslate(context, criteria);
    }

    @Override
    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriteria criteria, ISearchCriteria subCriteria)
    {
        SearchObjectKind objectKind = context.peekObjectKind();

        if (SearchObjectKind.EXPERIMENT.equals(objectKind))
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT_SPACE);
        } else if (SearchObjectKind.SAMPLE.equals(objectKind))
        {
            return DetailedSearchField.createAttributeField(SampleAttributeSearchFieldKind.SPACE);
        } else
        {
            throw new IllegalArgumentException("Unknown object kind: " + objectKind);
        }
    }

}
