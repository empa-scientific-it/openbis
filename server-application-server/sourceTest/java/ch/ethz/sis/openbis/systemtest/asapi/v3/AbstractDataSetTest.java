package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertCollectionContainsOnly;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.systemsx.cisd.common.collection.SimpleComparator;

public class AbstractDataSetTest extends AbstractTest
{
    protected static final SimpleComparator<DataSet, String> DATA_SET_COMPARATOR = new SimpleComparator<DataSet, String>()
        {
            @Override
            public String evaluate(DataSet item)
            {
                return item.getCode();
            }
        };

    protected static void assertIdentifiers(Collection<DataSet> dataSets, String... expectedCodesIdentifiers)
    {
        Set<String> actualSet = new HashSet<String>();
        for (DataSet dataSet : dataSets)
        {
            actualSet.add(dataSet.getPermId().getPermId());
        }

        assertCollectionContainsOnly(actualSet, expectedCodesIdentifiers);
    }

}
