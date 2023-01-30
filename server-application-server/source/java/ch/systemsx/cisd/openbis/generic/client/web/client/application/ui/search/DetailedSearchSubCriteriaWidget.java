/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.EnumSet;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityTypeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * Widget for {@link DetailedSearchSubCriteria} management.
 * 
 * @author Piotr Buczek
 */
public class DetailedSearchSubCriteriaWidget extends DetailedSearchCriteriaWidget
{
    private AssociatedEntityKind association;

    private SearchCriteriaConnection connection;

    public DetailedSearchSubCriteriaWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            AssociatedEntityKind association)
    {
        super(viewContext, association.getEntityKind());
        this.association = association;
        setConnection(SearchCriteriaConnection.MATCH_ALL);
    }

    @Override
    protected String getCriteriaLabel()
    {
        if (EnumSet.of(AssociatedEntityKind.EXPERIMENT, AssociatedEntityKind.SAMPLE).contains(association))
        {
            return EntityTypeUtils.translatedEntityKindForUI(viewContext, association.getEntityKind());
        }
        return association.getDescription();
    }

    @Override
    protected SearchCriteriaConnection getConnection()
    {
        return connection;
    }

    @Override
    protected void setConnection(SearchCriteriaConnection connection)
    {
        this.connection = connection;
    }

    @Override
    public String getCriteriaDescription()
    {
        return getCriteriaLabel() + "(" + super.getCriteriaDescription() + ")";
    }

    public DetailedSearchSubCriteria extractSubCriteria(boolean useWildcardSearchMode)
    {
        return new DetailedSearchSubCriteria(association, extractCriteria(useWildcardSearchMode));
    }

}
