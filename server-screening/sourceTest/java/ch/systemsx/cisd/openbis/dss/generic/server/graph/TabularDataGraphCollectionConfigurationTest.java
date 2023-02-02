/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.net.URL;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphCollectionConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.graph.TabularDataGraphConfiguration;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphCollectionConfigurationTest extends AssertJUnit
{

    @Test
    public void testGoodConfigurationFile()
    {
        URL propertiesUrl = getClass().getResource("graph.properties");
        String propertiesFilePath = propertiesUrl.getFile();
        TabularDataGraphCollectionConfiguration configuration =
                TabularDataGraphCollectionConfiguration.getConfiguration(propertiesFilePath);

        // test the file global stuff
        assertEquals(',', configuration.getColumnDelimiter());
        assertFalse(configuration.isIgnoreComments());
        assertEquals(1024, configuration.getImageWidth());
        assertEquals(768, configuration.getImageHeight());
        assertEquals(30, configuration.getThumbnailWidth());
        assertEquals(30, configuration.getThumbnailHeight());

        // test the graph configurations
        assertEquals(4, configuration.getGraphNames().size());

        List<String> graphNames = configuration.getGraphNames();
        assertEquals("scatter1", graphNames.get(0));
        TabularDataGraphConfiguration graphConfig =
                configuration.getGraphConfiguration(graphNames.get(0));
        // If not specified, the title should default to the name
        assertEquals("scatter1", graphConfig.getTitle());
        assertEquals(30, graphConfig.getImageHeight());
        assertEquals(30, graphConfig.getImageWidth());
        assertEquals("<TOTALCELLS> TotalCells", graphConfig.getXAxisColumn().toString());
        assertEquals("<INFCELLS> Infected Cells", graphConfig.getYAxisColumn().toString());

        assertEquals("hist", graphNames.get(1));
        graphConfig = configuration.getGraphConfiguration(graphNames.get(1));
        assertEquals("Total Cells Histogram", graphConfig.getTitle());

        assertEquals("heat", graphNames.get(2));
        graphConfig = configuration.getGraphConfiguration(graphNames.get(2));
        assertEquals("Infected Cells", graphConfig.getTitle());
        assertEquals("<WELLNAME> WellName", graphConfig.getXAxisColumn().toString());

        assertEquals("scatter2", graphNames.get(3));
    }
}
