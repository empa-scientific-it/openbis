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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class SamplesListingTest extends SystemTestCase
{
    @Test
    public void testListListableSamplesByExperimentWithAndWithoutDirectlyConnected()
    {
        logIntoCommonClientService();
        sample("/CISD/PLATE-WITH_EXPERIMENT").experiment("/CISD/NEMO/EXP1").type("CELL_PLATE")
                .property("COMMENT", "root sample").register();
        sample("/CISD/CHILD-PLATE1").type("CELL_PLATE").parents("/CISD/NEMO/PLATE-WITH_EXPERIMENT")
                .property("COMMENT", "my plate child").register();
        // WELL samples are not listable
        sample("/CISD/CHILD-WELL1").type("WELL").parents("/CISD/NEMO/PLATE-WITH_EXPERIMENT").register();
        sample("/CISD/CHILD-PLATE2").type("CELL_PLATE").parents("/CISD/CHILD-PLATE1")
                .property("COMMENT", "my plate grandchild").register();
        sample("/CISD/CHILD-WELL2").type("WELL").parents("/CISD/CHILD-PLATE1").register();
        ListSampleCriteria listCriteria = ListSampleCriteria.createForExperiment(new TechId(2));

        listCriteria.setOnlyDirectlyConnected(true);
        TypedTableResultSet<Sample> resultSet =
                commonClientService.listSamples2(new ListSampleDisplayCriteria2(listCriteria));
        assertSamples(resultSet, "/CISD/NEMO/PLATE-WITH_EXPERIMENT");
        assertEquals("[COMMENT: root sample]", resultSet.getResultSet().getList().get(0)
                .getOriginalObject().getObjectOrNull().getProperties().toString());

        listCriteria.setOnlyDirectlyConnected(false);
        resultSet = commonClientService.listSamples2(new ListSampleDisplayCriteria2(listCriteria));
        assertSamples(resultSet, "/CISD/CHILD-PLATE1", "/CISD/CHILD-PLATE2",
                "/CISD/NEMO/PLATE-WITH_EXPERIMENT");
        List<Sample> samples = asList(resultSet);
        Collections.sort(samples);
        assertEquals("[COMMENT: my plate child]", samples.get(0).getProperties().toString());
        assertEquals("/CISD/NEMO/PLATE-WITH_EXPERIMENT", samples.get(0).getParents().iterator().next()
                .getIdentifier());
        assertEquals(1, samples.get(0).getParents().size());
        assertEquals("CELL_PLATE", samples.get(0).getSampleType().getCode());
        assertEquals("test", samples.get(0).getRegistrator().getUserId());
    }

    private void assertSamples(TypedTableResultSet<Sample> actualSamples, String... expectedSamples)
    {
        GridRowModels<TableModelRowWithObject<Sample>> list =
                actualSamples.getResultSet().getList();
        Set<String> identifiers = new TreeSet<String>();
        for (GridRowModel<TableModelRowWithObject<Sample>> gridRowModel : list)
        {
            identifiers.add(gridRowModel.getOriginalObject().getObjectOrNull().getIdentifier());
        }
        assertEquals(Arrays.asList(expectedSamples).toString(), identifiers.toString());
    }
}
