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
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups = "slow")
public class TabularDataScatterplotTest extends AbstractTabularDataGraphTest
{
    @Test
    public void testScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        CodeAndLabel yAxisColumn = CodeAndLabelUtil.create("<INFECTEDCELLS> Infected Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Total Cells vs. Infected Cells",
                        xAxisColumn, yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertEquals(1, graph.tryXColumnNumber());
        assertEquals("Total Cells", graph.getXAxisLabel());
        assertEquals(2, graph.tryYColumnNumber());
        assertEquals("Infected Cells", graph.getYAxisLabel());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testBigNumberScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("BigNumber");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Big Number vs Total Cells", xAxisColumn,
                        yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertEquals(18, graph.tryXColumnNumber());
        assertEquals("BigNumber", graph.getXAxisLabel());
        assertEquals(1, graph.tryYColumnNumber());
        assertEquals("Total Cells", graph.getYAxisLabel());
        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSmallNumberScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("SmallNumbers");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Small Numbers vs Total Cells",
                        xAxisColumn, yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testLotsOf0sScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("Zero");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Zero vs Total Cells", xAxisColumn,
                        yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testJustNaNScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("JustNaN");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Just NaN vs Total Cells", xAxisColumn,
                        yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSomeNaNScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("SomeNaN");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Some NaN vs Total Cells", xAxisColumn,
                        yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testJustInfScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("JustInf");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Just Inf vs Total Cells", xAxisColumn,
                        yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testSomeInfScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("SomeInf");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Some Inf vs Total Cells", xAxisColumn,
                        yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testLotsOfBlanksScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        CodeAndLabel xAxisColumn = CodeAndLabelUtil.create("Blanks");
        CodeAndLabel yAxisColumn = CodeNormalizer.create("TotalCells", "Total Cells");
        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Blanks vs Total Cells", xAxisColumn,
                        yAxisColumn, 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    public void testIncorrectlyConfiguredScatterplot() throws IOException
    {
        File outputFile = getImageOutputFile();

        TabularDataScatterplotConfiguration config =
                new TabularDataScatterplotConfiguration("Test", CodeAndLabelUtil.create("TotalCells"),
                        CodeAndLabelUtil.create("Non-existant"), 300, 200);
        TabularDataScatterplot graph =
                new TabularDataScatterplot(config, getTestDatasetFileLines(),
                        getOutputStream(outputFile));
        assertTrue(graph.tryYColumnNumber() < 0);

        graph.generateImage();

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
