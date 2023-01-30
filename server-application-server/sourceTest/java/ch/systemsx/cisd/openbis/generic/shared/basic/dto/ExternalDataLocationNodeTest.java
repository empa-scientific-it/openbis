/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ContainerDataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataLocationNodeTest extends AssertJUnit
{
    @Test
    public void test()
    {
        ContainerDataSetBuilder rootContainer = new ContainerDataSetBuilder(1).code("c1");
        DataStore store = new DataStoreBuilder("DSS").getStore();
        rootContainer.component(new DataSetBuilder().code("p1").store(store).location("a/b/c/1").getDataSet());
        ContainerDataSetBuilder subContainer = new ContainerDataSetBuilder(2).code("c2");
        PhysicalDataSet component2 = new DataSetBuilder().code("p2").store(store).location("a/b/c/2").getDataSet();
        subContainer.component(component2);
        subContainer.component(new DataSetBuilder().code("p3").store(store).location("a/b/c/3").getDataSet());
        rootContainer.component(subContainer.getContainerDataSet());
        rootContainer.component(component2);

        ExternalDataLocationNode node = new ExternalDataLocationNode(rootContainer.getContainerDataSet());

        assertEquals(true, node.isContainer());
        List<IDatasetLocationNode> components = new ArrayList<IDatasetLocationNode>(node.getComponents());
        assertEquals(true, components.get(0).isContainer());
        assertEquals("p1", components.get(1).getLocation().getDataSetCode());
        assertEquals("DSS", components.get(1).getLocation().getDataStoreCode());
        assertEquals("a/b/c/1", components.get(1).getLocation().getDataSetLocation());
        assertEquals(0, components.get(1).getLocation().getOrderInContainer("c1").intValue());
        assertEquals("p2", components.get(2).getLocation().getDataSetCode());
        assertEquals("a/b/c/2", components.get(2).getLocation().getDataSetLocation());
        assertEquals(2, components.get(2).getLocation().getOrderInContainer("c1").intValue());
        assertEquals(3, components.size());
        components = new ArrayList<IDatasetLocationNode>(components.get(0).getComponents());
        assertEquals("p2", components.get(0).getLocation().getDataSetCode());
        assertEquals("a/b/c/2", components.get(0).getLocation().getDataSetLocation());
        assertEquals(0, components.get(0).getLocation().getOrderInContainer("c2").intValue());
        assertEquals("p3", components.get(1).getLocation().getDataSetCode());
        assertEquals("a/b/c/3", components.get(1).getLocation().getDataSetLocation());
        assertEquals(1, components.get(1).getLocation().getOrderInContainer("c2").intValue());
        assertEquals(2, components.size());
    }
}
