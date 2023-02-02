/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.image;

import static org.testng.AssertJUnit.assertEquals;

import java.awt.Color;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.image.MixColors;
import ch.systemsx.cisd.common.image.WavelengthColor;

/**
 * Test cases for {@link MixColors}.
 * 
 * @author Bernd Rinn
 */
public class MixColorsTest
{
    private static String tohex(Color c)
    {
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static Color fromhex(String hex)
    {
        int red = Integer.valueOf(hex.substring(0, 2), 16);
        int green = Integer.valueOf(hex.substring(2, 4), 16);
        int blue = Integer.valueOf(hex.substring(4, 6), 16);
        return new Color(red, green, blue);
    }

    @Test
    public void testMixTwoPureColorsFullIntensity()
    {
        Color[] colors = new Color[]
        { fromhex("ff0000"), fromhex("00cccc") };
        assertEquals("ffcccc", tohex(MixColors.calcMixedColorLinear(colors)));
        assertEquals("ffa3a3", tohex(MixColors.calcMixedColorQuadratic(colors)));
    }

    @Test
    public void testMixTwoPureColorsLowBrightness()
    {
        Color[] colors = new Color[]
        { fromhex("100000"), fromhex("000808") };
        assertEquals("100808", tohex(MixColors.calcMixedColorLinear(colors)));
        assertEquals("100404", tohex(MixColors.calcMixedColorQuadratic(colors)));
    }

    @Test
    public void testMixTwoPureColorsLowBrightnessIncreasing()
    {
        Color[] colors = new Color[]
        { fromhex("100000"), fromhex("080808") };
        assertEquals("180808", tohex(MixColors.calcMixedColorLinear(colors)));
        assertEquals("180505", tohex(MixColors.calcMixedColorQuadratic(colors)));
    }

    @Test
    public void testMixTwoPureColorsLowIntensity()
    {
        Color[] colors = new Color[]
        { fromhex("FF0000"), fromhex("00FFFF") };
        float[] intensities = new float[] { 0.063f, 0.0314f };
        assertEquals("100808", tohex(MixColors.calcMixedColorLinear(colors, intensities)));
        assertEquals("100404", tohex(MixColors.calcMixedColorQuadratic(colors, intensities)));
    }

    @Test
    public void testMixTwoPureColorsLowIntensityBrightnessIncreaseing()
    {
        Color[] colors = new Color[]
        { fromhex("FF0000"), fromhex("FFFFFF") };
        float[] intensities = new float[] { 0.063f, 0.0314f };
        assertEquals("180808", tohex(MixColors.calcMixedColorLinear(colors, intensities)));
        assertEquals("180505", tohex(MixColors.calcMixedColorQuadratic(colors, intensities)));
    }

    @Test
    public void testMixDAPIGFPCY5()
    {
        Color dapi = WavelengthColor.getColorForWavelength(461); // 006bff
        Color gfp = WavelengthColor.getColorForWavelength(509); // 00ff0d
        Color cy5 = WavelengthColor.getColorForWavelength(660); // ff0000

        assertEquals("3ee5ff", tohex(MixColors.calcMixedColorLinear(new Color[]
        { dapi, gfp, cy5 }, new float[]
        { 1f, 0.5f, 0.25f })));
        assertEquals("10a9ff", tohex(MixColors.calcMixedColorQuadratic(new Color[]
        { dapi, gfp, cy5 }, new float[]
        { 1f, 0.5f, 0.25f })));

        assertEquals("7ea9ff", tohex(MixColors.calcMixedColorLinear(new Color[]
        { dapi, gfp, cy5 }, new float[]
        { 1f, 0.25f, 0.5f })));
        assertEquals("407bff", tohex(MixColors.calcMixedColorQuadratic(new Color[]
        { dapi, gfp, cy5 }, new float[]
        { 1f, 0.25f, 0.5f })));
    }

    @Test
    public void testManyLowIntensityColors()
    {
        Color[] colors = new Color[100];
        float[] intensities = new float[colors.length];
        colors[0] = fromhex("0000ff");
        intensities[0] = 1.0f;
        for (int i = 1; i < colors.length; ++i)
        {
            colors[i] = fromhex("ffff00");
            intensities[i] = 0.01f;
        }
        // Linear: pretty white
        assertEquals("fcfcff", tohex(MixColors.calcMixedColorLinear(colors, intensities)));
        // Quadratic: still blue
        assertEquals("0303ff", tohex(MixColors.calcMixedColorQuadratic(colors, intensities)));
    }

    @Test
    public void testManyColors()
    {
        Color[] colors = new Color[100];
        float[] intensities = new float[colors.length];
        colors[0] = fromhex("0000ff");
        intensities[0] = 1.0f;
        for (int i = 1; i < colors.length; ++i)
        {
            colors[i] = fromhex("ffff00");
            intensities[i] = 0.1f;
        }
        // Linear: yellow
        assertEquals("ffff1a", tohex(MixColors.calcMixedColorLinear(colors, intensities)));
        // Quadratic: white
        assertEquals("fcfcff", tohex(MixColors.calcMixedColorQuadratic(colors, intensities)));
    }

}
