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
public class TabularDataHeatmapTest extends AbstractTabularDataGraphTest
{
    @Test
    public void testHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Infection Index",
                        CodeAndLabelUtil.create("WellName"),
                        CodeAndLabelUtil.create("InfectionIndex"), 300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSmallNumberHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Small Numbers",
                        CodeAndLabelUtil.create("WellName"),
                        CodeAndLabelUtil.create("SmallNumbers"), 300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testBigNumberHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Big Number",
                        CodeAndLabelUtil.create("WellName"), CodeAndLabelUtil.create("BigNumber"),
                        300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testLotsOf0sHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Zero", CodeAndLabelUtil.create("WellName"),
                        CodeAndLabelUtil.create("Zero"), 300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertEquals(0, graph.tryXColumnNumber());
        assertEquals(0, graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testJustNaNHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Just NaN",
                        CodeAndLabelUtil.create("WellName"), CodeAndLabelUtil.create("JustNaN"),
                        300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSomeNaNHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Some NaN",
                        CodeAndLabelUtil.create("WellName"), CodeAndLabelUtil.create("SomeNaN"),
                        300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testJustInfHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Just Inf",
                        CodeAndLabelUtil.create("WellName"), CodeAndLabelUtil.create("JustInf"),
                        300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSomeInfHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Some Inf",
                        CodeAndLabelUtil.create("WellName"), CodeAndLabelUtil.create("SomeInf"),
                        300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertSame(graph.tryXColumnNumber(), graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testLotsOfBlanksHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Blanks", CodeAndLabelUtil.create("WellRow"),
                        CodeAndLabelUtil.create("WellCol"), CodeAndLabelUtil.create("Blanks"), 300,
                        200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertEquals(15, graph.tryXColumnNumber());
        assertEquals(16, graph.tryYColumnNumber());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testIncorrectlyConfiguredHeatmap() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataHeatmapConfiguration config =
                new TabularDataHeatmapConfiguration("Non-Existant",
                        CodeAndLabelUtil.create("WellName"),
                        CodeAndLabelUtil.create("Non-Existant"), 300, 200);
        AbstractTabularDataGraph<TabularDataHeatmapConfiguration> graph =
                new TabularDataHeatmap(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        assertTrue(graph.tryColumnNumberForHeader(config.getZAxisColumn()) < 0);

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
