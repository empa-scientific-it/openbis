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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * Widget for {@link DetailedSearchCriteria} management.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchMainCriteriaWidget extends DetailedSearchCriteriaWidget
{
    private final MatchCriteriaRadio matchRadios;

    public DetailedSearchMainCriteriaWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind)
    {
        super(viewContext, entityKind);
        matchRadios =
                new MatchCriteriaRadio(viewContext.getMessage(Dict.MATCH_ALL),
                        viewContext.getMessage(Dict.MATCH_ANY));
    }

    @Override
    protected String getCriteriaLabel()
    {
        return "Main Criteria";
    }

    @Override
    protected void addInitialWidgets()
    {
        add(matchRadios);
        super.addInitialWidgets();
    }

    @Override
    public void reset()
    {
        matchRadios.reset();
        super.reset();
    }

    @Override
    public String getCriteriaDescription()
    {
        return matchRadios.getSelectedLabel() + ": " + super.getCriteriaDescription();
    }

    @Override
    protected SearchCriteriaConnection getConnection()
    {
        return matchRadios.getSelected();
    }

    @Override
    protected void setConnection(SearchCriteriaConnection connection)
    {
        matchRadios.setValue(connection);
    }
}
