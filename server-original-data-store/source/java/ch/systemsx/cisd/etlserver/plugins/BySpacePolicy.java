package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.etlserver.plugins.grouping.DatasetListWithTotal;
import ch.systemsx.cisd.etlserver.plugins.grouping.Grouping;
import ch.systemsx.cisd.etlserver.plugins.grouping.IGroupKeyProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * An archiving policy that selects a subset from archiving candidates by grouping them on space and "packing" them into a min-max size batches
 * 
 * @author Sascha Fedorenko
 */
public class BySpacePolicy extends BaseGroupingPolicy implements IAutoArchiverPolicy
{
    private final List<IGroupKeyProvider> providers;

    public BySpacePolicy(Properties properties)
    {
        super(properties);
        providers = new ArrayList<IGroupKeyProvider>();
        providers.add(Grouping.Space);
        providers.add(Grouping.Project);
        providers.add(Grouping.Experiment);
        providers.add(Grouping.Sample);
        providers.add(Grouping.DataSet);
    }

    @Override
    public List<AbstractExternalData> filterDataSetsWithSizes(List<AbstractExternalData> dataSets)
    {
        List<AbstractExternalData> singleDataSet = findSingleBigDataSet(dataSets);
        if (singleDataSet != null)
        {
            return singleDataSet;
        }

        // split into the buckets of different file types - we never want to group those together
        List<DatasetListWithTotal> buckets = new LinkedList<DatasetListWithTotal>();
        for (DatasetListWithTotal dsList : splitDataSetsInGroupsAccordingToCriteria(dataSets, Grouping.DataSetType))
        {
            if (dsList.getCumulatedSize() >= minArchiveSize)
            {
                buckets.add(dsList);
            }
            // we don't want to archive buckets at this level. Therefore we proceed with all buckets of at least minimum size
            // ignore too small buckets
        }

        return findBestGroup(buckets);
    }

    private List<AbstractExternalData> findSingleBigDataSet(List<AbstractExternalData> dataSets)
    {
        for (AbstractExternalData ds : dataSets)
        {
            if (ds.getSize() >= maxArchiveSize)
            {
                return Collections.singletonList(ds);
            }
        }
        return null;
    }

    /**
     * Use grouping key providers to split buckets into smaller buckets in rounds. When a single bucket of proper size is encountered it is being
     * returned. If a bucket that is itself too big, but all of it's sub buckets are too small then some proper subset of a bucket is returned.
     * 
     * @return the best group of data sets that is consistent with the grouping providers and of a proper size or empty list otherwise
     */
    private List<AbstractExternalData> findBestGroup(List<DatasetListWithTotal> buckets)
    {
        List<DatasetListWithTotal> loopBuckets = buckets;

        // Loop invariant is that all buckets in the list have size above the max threshold.
        // It means we assume the buckets processed are always to big to be archived
        for (IGroupKeyProvider provider : providers)
        {
            List<DatasetListWithTotal> newBuckets = new LinkedList<DatasetListWithTotal>();

            for (DatasetListWithTotal bucket : loopBuckets)
            {
                Collection<DatasetListWithTotal> splitResult = splitDataSetsInGroupsAccordingToCriteria(bucket, provider);

                boolean allDataSetsTooSmall = true;
                for (DatasetListWithTotal dsList : splitResult)
                {
                    if (dsList.getCumulatedSize() > maxArchiveSize)
                    {
                        newBuckets.add(dsList);
                        allDataSetsTooSmall = false;
                    }
                    else if (dsList.getCumulatedSize() >= minArchiveSize)
                    {
                        return dsList.getList();
                    }
                }

                if (allDataSetsTooSmall)
                {
                    List<AbstractExternalData> result = bundleBuckets(splitResult);
                    if (result == null)
                    {
                        throw new IllegalStateException(
                                "The bucket is full of data sets, that are too small but all together too big. Yet it's impossible to find a subset that would be big enough, but not too big. Probably min / max settings are incorrect");
                    }
                    return result;
                }
            }
            loopBuckets = newBuckets;
        }

        return Collections.emptyList();
    }

    /**
     * Get's the list of buckets that are all below the threshold, yet - altogether are all above threshold. Returns some subset of all of those
     * datasets that is together in beet
     */
    List<AbstractExternalData> bundleBuckets(Iterable<DatasetListWithTotal> buckets)
    {
        long total = 0;

        List<AbstractExternalData> result = new LinkedList<AbstractExternalData>();

        for (DatasetListWithTotal bucket : buckets)
        {
            long bucketSize = bucket.getCumulatedSize();
            if (total + bucketSize < maxArchiveSize)
            {
                result.addAll(bucket.getList());
                total += bucketSize;
            }
        }
        if (total >= minArchiveSize)
        {
            return result;
        }
        else
        {
            return null;
        }
    }

}
