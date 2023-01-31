package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.etlserver.plugins.grouping.DatasetListWithTotal;
import ch.systemsx.cisd.etlserver.plugins.grouping.Grouping;
import ch.systemsx.cisd.etlserver.plugins.grouping.IGroupKeyProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * An archiving policy that selects a subset from archiving candidates by grouping them on data set type, experiment and project and "packing" them
 * into a min-max size batches
 * 
 * @author Sascha Fedorenko
 */
public class ByExperimentPolicy extends BaseGroupingPolicy implements IAutoArchiverPolicy
{
    private final List<IGroupKeyProvider> providers;

    public ByExperimentPolicy(Properties properties)
    {
        super(properties);
        providers = new ArrayList<IGroupKeyProvider>();
        providers.add(Grouping.ExperimentAndDataSetType);
        providers.add(Grouping.Experiment);
        providers.add(Grouping.Project);
    }

    @Override
    public List<AbstractExternalData> filterDataSetsWithSizes(List<AbstractExternalData> dataSets)
    {
        // if there is one huge data set. archive it first
        for (AbstractExternalData ds : dataSets)
        {
            if (ds.getSize() >= maxArchiveSize)
            {
                return Collections.singletonList(ds);
            }
        }

        for (IGroupKeyProvider provider : providers)
        {
            Collection<DatasetListWithTotal> result = splitDataSetsInGroupsAccordingToCriteria(dataSets, provider);
            if (result.size() > 0)
            {
                DatasetListWithTotal best = Collections.max(result);
                long size = best.getCumulatedSize();
                if (size > minArchiveSize)
                {
                    if (size < maxArchiveSize)
                    {
                        return best.getList();
                    }

                    sortBySamples(best);
                    return splitDatasets(best);
                }
            }
        }

        return Collections.emptyList();
    }

    private void sortBySamples(DatasetListWithTotal datasets)
    {
        datasets.sort(new SimpleComparator<AbstractExternalData, String>()
            {
                @Override
                public String evaluate(AbstractExternalData data)
                {
                    String sid1 = data.getSampleIdentifier();
                    return sid1 == null ? "" : sid1;
                }
            });
    }

    private List<AbstractExternalData> splitDatasets(Iterable<AbstractExternalData> datasets)
    {
        DatasetListWithTotal result = new DatasetListWithTotal();

        for (AbstractExternalData ds : datasets)
        {
            if (result.getCumulatedSize() + ds.getSize() <= maxArchiveSize)
            {
                result.add(ds);
            }
        }

        return result.getList();
    }
}
