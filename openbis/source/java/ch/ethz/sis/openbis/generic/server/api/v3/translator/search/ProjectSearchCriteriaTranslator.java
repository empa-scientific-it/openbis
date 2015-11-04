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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.search;

import java.util.EnumSet;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriteria;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class ProjectSearchCriteriaTranslator extends AbstractFieldFromCompositeSearchCriteriaTranslator
{

    public ProjectSearchCriteriaTranslator(IDAOFactory daoFactory, IEntityAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof ProjectSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        if (EnumSet.of(EntityKind.EXPERIMENT,  EntityKind.SAMPLE).contains(context.peekEntityKind()) == false)
        {
            throw new IllegalArgumentException("Project criteria can be used only in experiment or sample criteria, "
                    + "but was used in: " + context.peekEntityKind() + " context.");
        }

        return super.doTranslate(context, criteria);
    }

    @Override
    protected DetailedSearchField doTranslateSearchField(SearchTranslationContext context, ISearchCriteria criteria, ISearchCriteria subCriteria)
    {
        if (subCriteria instanceof CodeSearchCriteria)
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT);
        } else if (subCriteria instanceof PermIdSearchCriteria)
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT_PERM_ID);
        } else if (subCriteria instanceof SpaceSearchCriteria)
        {
            return DetailedSearchField.createAttributeField(ExperimentAttributeSearchFieldKind.PROJECT_SPACE);
        } else
        {
            throw new IllegalArgumentException("Unknown criteria: " + subCriteria);
        }
    }

}
