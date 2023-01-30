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
