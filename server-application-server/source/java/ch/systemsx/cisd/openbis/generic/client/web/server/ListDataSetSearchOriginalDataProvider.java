/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * A {@link IOriginalDataProvider} implementation for search data sets.
 * 
 * @author Izbaela Adamczyk
 */
final class ListDataSetSearchOriginalDataProvider extends
        AbstractOriginalDataProvider<AbstractExternalData>
{

    private final DetailedSearchCriteria criteria;

    ListDataSetSearchOriginalDataProvider(final ICommonServer commonServer,
            final String sessionToken, final DetailedSearchCriteria criteria)
    {
        super(commonServer, sessionToken);
        this.criteria = criteria;
    }

    //
    // AbstractOriginalDataProvider
    //

    @Override
    public final List<AbstractExternalData> getFullOriginalData()
    {
        final List<AbstractExternalData> hits = commonServer.searchForDataSets(sessionToken, criteria);
        return hits;
    }
}
