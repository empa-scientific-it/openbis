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

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class TabularDataHistogramTest extends AbstractTabularDataGraphTest
{
    @Test
    public void testHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Infected Cells",
                        CodeAndLabelUtil.create("InfectedCells"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSmallNumberHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Small Numbers",
                        CodeAndLabelUtil.create("SmallNumbers"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
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
                new TabularDataHistogramConfiguration("Big Number",
                        CodeAndLabelUtil.create("BigNumber"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testLotsOf0sHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Zero", CodeAndLabelUtil.create("Zero"), 300,
                        200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testJustNaNHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Just NaN",
                        CodeAndLabelUtil.create("JustNaN"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSomeNaNHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Some NaN",
                        CodeAndLabelUtil.create("SomeNaN"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testJustInfHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Just Inf",
                        CodeAndLabelUtil.create("JustInf"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSomeInfHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Some Inf",
                        CodeAndLabelUtil.create("SomeInf"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testLotsOfBlanksHistogram() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHistogramConfiguration config =
                new TabularDataHistogramConfiguration("Blanks", CodeAndLabelUtil.create("Blanks"),
                        300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
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
                new TabularDataHistogramConfiguration("Test",
                        CodeAndLabelUtil.create("Non-Existant"), 300, 200, 6);
        AbstractTabularDataGraph<TabularDataHistogramConfiguration> graph =
                new TabularDataHistogram(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        assertTrue(graph.tryXColumnNumber() < 0);

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
