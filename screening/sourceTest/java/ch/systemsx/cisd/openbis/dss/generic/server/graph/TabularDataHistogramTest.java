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

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataHistogramTest extends AbstractTabularDataGraphTest
{
    @Test
    public void testHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Test", "InfectedCells", 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getDatasetFileLines(), getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testBigNumberHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Test", "BigNumber", 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getBigNumberDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testIncorrectlyConfiguredHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Test", "Non-Existant", 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getDatasetFileLines(), getOutputStream(outputFile));

        assertTrue(graph.tryXColumnNumber() < 0);

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
