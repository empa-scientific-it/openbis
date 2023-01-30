/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class DataSetListingTest extends SystemTestCase
{
    @Test
    public void testListExperimentDataSetsDirectlyConnected()
    {
        logIntoCommonClientService();
        DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria =
                new DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>>();

        TypedTableResultSet<AbstractExternalData> resultSet =
                commonClientService.listExperimentDataSets(new TechId(18), criteria, true);

        List<AbstractExternalData> dataSets = asList(resultSet);
        assertEquals("20081105092159111-1", dataSets.get(0).getCode());
        assertProperties(
                "[ANY_MATERIAL: 1000_C (SIRNA), BACTERIUM: BACTERIUM1 (BACTERIUM), COMMENT: no comment, GENDER: FEMALE]",
                dataSets.get(0));
        assertEquals(1, dataSets.size());
    }

    @Test
    public void testListExperimentDataSetsAlsoIndirectlyConnected()
    {
        logIntoCommonClientService();
        DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria =
                new DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>>();

        TypedTableResultSet<AbstractExternalData> resultSet =
                commonClientService.listExperimentDataSets(new TechId(19), criteria, false);

        List<AbstractExternalData> dataSets = asList(resultSet);
        Collections.sort(dataSets, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData e1, AbstractExternalData e2)
                {
                    return e1.getCode().compareTo(e2.getCode());
                }
            });
        assertDataSets("20081105092159111-1, 20081105092159222-2, 20081105092259000-9, "
                + "20081105092259900-0, 20081105092259900-1, 20081105092359990-2", dataSets);
        assertProperties(
                "[ANY_MATERIAL: 1000_C (SIRNA), BACTERIUM: BACTERIUM1 (BACTERIUM), COMMENT: no comment, GENDER: FEMALE]",
                dataSets.get(0));
        assertEquals("/CISD/NEMO/CP-TEST-1", dataSets.get(0).getSampleIdentifier());
        assertEquals(18, dataSets.get(0).getExperiment().getId().intValue());
        assertEquals(null, dataSets.get(0).getParents());
        assertProperties("[COMMENT: no comment]", dataSets.get(3));
        assertEquals(null, dataSets.get(3).getSampleIdentifier());
        assertEquals(1, dataSets.get(3).getParents().size());
        assertEquals("20081105092259000-9", dataSets.get(3).getParents().iterator().next()
                .getCode());
        assertEquals(null, dataSets.get(3).getChildren()); // Children are not added even though
                                                           // there is one child
        assertEquals(6, dataSets.size());
    }

    private void assertDataSets(String expectedCodes, List<AbstractExternalData> dataSets)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (AbstractExternalData dataSet : dataSets)
        {
            builder.append(dataSet.getCode());
        }
        assertEquals(expectedCodes, builder.toString());
    }
}
