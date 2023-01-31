package ch.systemsx.cisd.openbis.dss.generic.server.api.v2.sequencedatabases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFileSearchResultLocation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchResult;

public class FileSearchDomain extends AbstractSearchDomainService
{
    private static final String SEARCH_OPTIONS_KEY = "search-options";
    
    private static final String SUBSTRING_OPTION = "SUBSTRING";
    
    private static final String WILDCARD_OPTION = "WILDCARD";
    
    private Boolean available;

    public FileSearchDomain(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    public boolean isAvailable()
    {
        if (available == null)
        {
            available = PathInfoDataSourceProvider.isDataSourceDefined();
        }
        return available;
    }

    @Override
    public String getPossibleSearchOptionsKey()
    {
        return SEARCH_OPTIONS_KEY;
    }

    @Override
    public List<SearchDomainSearchOption> getPossibleSearchOptions()
    {
        List<SearchDomainSearchOption> options = new ArrayList<>();
        options.add(createOption(SUBSTRING_OPTION, "Substring", "Search for specified substring"));
        options.add(createOption(WILDCARD_OPTION, "Wildcards", "Interprete '*' and '?' as wildcard characters"));
        return options;
    }
    
    private SearchDomainSearchOption createOption(String code, String label, String description)
    {
        SearchDomainSearchOption option = new SearchDomainSearchOption();
        option.setCode(code);
        option.setLabel(label);
        option.setDescription(description);
        return option;
    }

    @Override
    public List<SearchDomainSearchResult> search(String searchString, Map<String, String> optionalParametersOrNull)
    {
        IDataSetPathInfoProvider dataSetPathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();
        Map<String, List<DataSetPathInfo>> allPathInfos = dataSetPathInfoProvider.listPathInfosBySearchString(
                createActualSearchString(searchString, optionalParametersOrNull));

        List<SearchDomainSearchResult> results = new ArrayList<SearchDomainSearchResult>();

        for (String datasetCode : allPathInfos.keySet())
        {
            List<DataSetPathInfo> dataSetPathInfos = allPathInfos.get(datasetCode);

            for (DataSetPathInfo dataSetPathInfo : dataSetPathInfos)
            {
                SearchDomainSearchResult searchDomainSearchResult = new SearchDomainSearchResult();
                DataSetFileSearchResultLocation dfsrl = new DataSetFileSearchResultLocation();
                dfsrl.setPermId(datasetCode);
                dfsrl.setCode(datasetCode);
                dfsrl.setPathInDataSet(dataSetPathInfo.getRelativePath());
                dfsrl.setEntityKind(EntityKind.DATA_SET);

                searchDomainSearchResult.setResultLocation(dfsrl);
                results.add(searchDomainSearchResult);
            }
        }

        return results;
    }

    private String createActualSearchString(String searchString, Map<String, String> optionalParametersOrNull)
    {
        String actualSearchString = "%" + searchString + "%";
        String option = optionalParametersOrNull == null ? null : optionalParametersOrNull.get(SEARCH_OPTIONS_KEY);
        if (WILDCARD_OPTION.equals(option))
        {
            actualSearchString = searchString.replace('*', '%').replace('?', '_');
        }
        return actualSearchString;
    }

}