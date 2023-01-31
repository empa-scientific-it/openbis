package ch.systemsx.cisd.etlserver.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.etlserver.plugins.grouping.DatasetListWithTotal;
import ch.systemsx.cisd.etlserver.plugins.grouping.IGroupKeyProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * An base for archiving policies respecting desired target min-max size of the archive
 * 
 * @author Sascha Fedorenko
 */
public abstract class BaseGroupingPolicy implements IAutoArchiverPolicy
{
    public static final String MINIMAL_ARCHIVE_SIZE = "minimal-archive-size";

    public static final String MAXIMAL_ARCHIVE_SIZE = "maximal-archive-size";

    protected static final long DEFAULT_MINIMAL_ARCHIVE_SIZE = 0;

    protected static final long DEFAULT_MAXIMAL_ARCHIVE_SIZE = Long.MAX_VALUE;

    protected final long minArchiveSize;

    protected final long maxArchiveSize;

    private IDataSetPathInfoProvider pathInfoProvider;

    public BaseGroupingPolicy(Properties properties)
    {
        minArchiveSize =
                PropertyUtils.getLong(properties, MINIMAL_ARCHIVE_SIZE, DEFAULT_MINIMAL_ARCHIVE_SIZE);

        maxArchiveSize =
                PropertyUtils.getLong(properties, MAXIMAL_ARCHIVE_SIZE, DEFAULT_MAXIMAL_ARCHIVE_SIZE);
    }

    @Override
    public final List<AbstractExternalData> filter(List<AbstractExternalData> dataSets)
    {
        if (dataSets.isEmpty())
        {
            return dataSets;
        }
        makeSureAllDataSetsWithSize(dataSets);
        return filterDataSetsWithSizes(dataSets);
    }

    /**
     * Method to be overridden by concrete implementations assuming the data sets already have size calculated.
     */
    protected abstract List<AbstractExternalData> filterDataSetsWithSizes(List<AbstractExternalData> dataSets);

    private void makeSureAllDataSetsWithSize(List<AbstractExternalData> dataSets)
    {
        for (AbstractExternalData dataSet : dataSets)
        {
            Long size = dataSet.getSize();
            if (size == null)
            {
                size = patchDataSetSize(dataSet);
            }

            if (size == null)
            {
                throw new ch.systemsx.cisd.common.exceptions.EnvironmentFailureException(
                        "Some datasets have no size, and it cannot be calculated! E.g. " + dataSet.getCode());
            }
        }
    }

    private Long patchDataSetSize(AbstractExternalData ds)
    {
        ISingleDataSetPathInfoProvider dsInfoProvider = getDatasetPathInfoProvider().tryGetSingleDataSetPathInfoProvider(ds.getCode());
        Long size = null;
        if (dsInfoProvider != null)
        {
            size = dsInfoProvider.getRootPathInfo().getSizeInBytes();
            ds.setSize(size);
        }
        return size;
    }

    private IDataSetPathInfoProvider getDatasetPathInfoProvider()
    {
        if (pathInfoProvider == null)
        {
            pathInfoProvider = ServiceProvider.getDataSetPathInfoProvider();
        }
        return pathInfoProvider;
    }

    protected Collection<DatasetListWithTotal> splitDataSetsInGroupsAccordingToCriteria(Iterable<AbstractExternalData> dataSets,
            IGroupKeyProvider provider)
    {
        Map<String, DatasetListWithTotal> result = new HashMap<String, DatasetListWithTotal>();

        for (AbstractExternalData ds : dataSets)
        {
            String key = provider.getGroupKey(ds);

            DatasetListWithTotal list = result.get(key);

            if (list == null)
            {
                result.put(key, list = new DatasetListWithTotal());
            }

            list.add(ds);

        }
        return result.values();
    }
}