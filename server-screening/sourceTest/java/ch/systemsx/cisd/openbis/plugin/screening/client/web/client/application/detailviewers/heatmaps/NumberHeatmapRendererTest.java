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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.NumberHeatmapRenderer.GREATER_THAN_EQUAL;
import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.NumberHeatmapRenderer.LESS_THAN_EQUAL;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.model.MinMaxAndRange;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.Range;

/**
 * @author Izabela Adamczyk
 */
public class NumberHeatmapRendererTest
{
    private static final String COLOR1 = "#111";

    private static final String COLOR2 = "#222";

    private static final String COLOR3 = "#333";

    @Test
    public void testFirstLabel() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        assertEquals(3f + "", renderer.tryGetFirstLabel());
    }

    private NumberHeatmapRenderer createMinNeg1Max3Renderer(List<String> colors)
    {
        MinMaxAndRange minMaxAndRange = new MinMaxAndRange(-1, 3, new Range(-1, 3));
        return new NumberHeatmapRenderer(minMaxAndRange, colors, createDummyRealRenderer());
    }

    private NumberHeatmapRenderer createMin0Max3Renderer(List<String> colors)
    {
        MinMaxAndRange minMaxAndRange = new MinMaxAndRange(0, 3, new Range(0, 3));
        return new NumberHeatmapRenderer(minMaxAndRange, colors, createDummyRealRenderer());
    }

    private IRealNumberRenderer createDummyRealRenderer()
    {
        return new IRealNumberRenderer()
            {
                @Override
                public String render(float value)
                {
                    return "" + value;
                }
            };
    }

    public void testValueTooSmall() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        assertEquals(COLOR1, renderer.getColor(-1f).getHexColor());
    }

    public void testValueTooBig() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        assertEquals(COLOR1, renderer.getColor(10f).getHexColor());
    }

    @Test
    public void testMiddleValueOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        assertEquals(COLOR1, renderer.getColor(1.5f).getHexColor());
    }

    @Test
    public void testMaxValueOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        assertEquals(COLOR1, renderer.getColor(3f).getHexColor());
    }

    @Test
    public void testMinValueOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        assertEquals(COLOR1, renderer.getColor(1f).getHexColor());
    }

    @Test
    public void testTwoColors() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR2, COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        assertEquals(COLOR1, renderer.getColor(0f).getHexColor());
        assertEquals(COLOR1, renderer.getColor(1f).getHexColor());
        assertEquals(COLOR1, renderer.getColor(1.5f).getHexColor());
        assertEquals(COLOR2, renderer.getColor(2f).getHexColor());
        assertEquals(COLOR2, renderer.getColor(3f).getHexColor());
    }

    @Test
    public void testManyColors() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1, COLOR2);
        NumberHeatmapRenderer renderer = createMinNeg1Max3Renderer(colors);
        assertEquals(COLOR2, renderer.getColor(-1f).getHexColor());
        assertEquals(COLOR2, renderer.getColor(1f).getHexColor());
        assertEquals(COLOR1, renderer.getColor(1.5f).getHexColor());
        assertEquals(COLOR1, renderer.getColor(2f).getHexColor());
        assertEquals(COLOR1, renderer.getColor(3f).getHexColor());
    }

    @Test
    public void testScaleOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMinNeg1Max3Renderer(colors);
        List<HeatmapScaleElement> scale = renderer.calculateScale();
        assertEquals(1, scale.size());
        HeatmapScaleElement element = scale.get(0);
        assertEquals(3f + "", renderer.tryGetFirstLabel());
        assertEquals(COLOR1, element.getColor().getHexColor());
        assertEquals(-1f + "", element.getLabel());
    }

    @Test
    public void testScaleTwoColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1, COLOR2);
        NumberHeatmapRenderer renderer = createMinNeg1Max3Renderer(colors);
        List<HeatmapScaleElement> scale = renderer.calculateScale();
        assertEquals(2, scale.size());
        assertEquals(3f + "", renderer.tryGetFirstLabel());
        HeatmapScaleElement element1 = scale.get(0);
        assertEquals(COLOR1, element1.getColor().getHexColor());
        assertEquals(1.0 + "", element1.getLabel());
        HeatmapScaleElement element2 = scale.get(1);
        assertEquals(COLOR2, element2.getColor().getHexColor());
        assertEquals(-1f + "", element2.getLabel());
    }

    @Test
    public void testScaleInsideMinMax()
    {
        MinMaxAndRange minMaxAndRange = new MinMaxAndRange(0, 5, new Range(1, 4));
        NumberHeatmapRenderer renderer =
                new NumberHeatmapRenderer(minMaxAndRange, Arrays.asList(COLOR1, COLOR2, COLOR3),
                        createDummyRealRenderer());

        String firstLabel = renderer.tryGetFirstLabel();
        List<HeatmapScaleElement> scale = renderer.calculateScale();

        assertEquals("[3.0:#111, 2.0:#222, " + LESS_THAN_EQUAL + " 1.0:#333]", scale.toString());
        assertEquals(GREATER_THAN_EQUAL + " 4.0", firstLabel);
    }

    @Test
    public void testScaleOutsideMinMax()
    {
        MinMaxAndRange minMaxAndRange = new MinMaxAndRange(2, 3, new Range(1, 4));
        NumberHeatmapRenderer renderer =
                new NumberHeatmapRenderer(minMaxAndRange, Arrays.asList(COLOR1, COLOR2, COLOR3),
                        createDummyRealRenderer());

        String firstLabel = renderer.tryGetFirstLabel();
        List<HeatmapScaleElement> scale = renderer.calculateScale();

        assertEquals("[3.0:#111, 2.0:#222, 1.0:#333]", scale.toString());
        assertEquals("4.0", firstLabel);
    }

    @Test
    public void testScaleInsideMinMaxButScaleInverted()
    {
        MinMaxAndRange minMaxAndRange = new MinMaxAndRange(0, 5, new Range(4, 1));
        NumberHeatmapRenderer renderer =
                new NumberHeatmapRenderer(minMaxAndRange, Arrays.asList(COLOR1, COLOR2, COLOR3),
                        createDummyRealRenderer());

        String firstLabel = renderer.tryGetFirstLabel();
        List<HeatmapScaleElement> scale = renderer.calculateScale();

        assertEquals("[2.0:#111, 3.0:#222, " + GREATER_THAN_EQUAL + " 4.0:#333" + "]", scale.toString());
        assertEquals(LESS_THAN_EQUAL + " 1.0", firstLabel);
    }

    @Test
    public void testScaleOutsideMinMaxButScaleInverted()
    {
        MinMaxAndRange minMaxAndRange = new MinMaxAndRange(200, 300, new Range(400.1f, 100));
        NumberHeatmapRenderer renderer =
                new NumberHeatmapRenderer(minMaxAndRange, Arrays.asList(COLOR1, COLOR2, COLOR3),
                        createDummyRealRenderer());

        String firstLabel = renderer.tryGetFirstLabel();
        List<HeatmapScaleElement> scale = renderer.calculateScale();

        assertEquals("[200.0:#111, 300.0:#222, 400.0:#333]", scale.toString());
        assertEquals("100.0", firstLabel);
    }
}
