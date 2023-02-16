/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GlobalSearchTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * {@link IViewLocatorResolver} to handle global search.
 * 
 * @author Kaloyan Enimanev
 */
public class GlobalSearchLocatorResolver extends AbstractViewLocatorResolver
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public final static String GLOBAL_SEARCH_ACTION = "GLOBAL_SEARCH";

    public final static String ENTITY_PARAMETER_KEY = "type";

    public final static String QUERY_PARAMETER_KEY = "query";

    public final static String USE_WILDCARDS_PARAMETER_KEY = "usewildcards";

    public GlobalSearchLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(GLOBAL_SEARCH_ACTION);
        this.viewContext = viewContext;
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        final SearchableEntity selectedSearchableEntity = getSearchableEntity(locator);
        // TODO KE: 2011-02-16 we should parse queries that can contain spaces
        final String queryText = getMandatoryParameter(locator, QUERY_PARAMETER_KEY);

        final String useWildcards = getOptionalParameter(locator, USE_WILDCARDS_PARAMETER_KEY);

        GlobalSearchTabItemFactory.openTab(viewContext, selectedSearchableEntity, queryText, useWildcards.equals("true"));
    }

    private SearchableEntity getSearchableEntity(ViewLocator locator)
    {
        SearchableEntity result = null;
        String entity = getOptionalParameter(locator, ENTITY_PARAMETER_KEY);
        if (entity != null)
        {
            result = new SearchableEntity();
            result.setName(entity);
            return result;
        }
        return null;
    }
}