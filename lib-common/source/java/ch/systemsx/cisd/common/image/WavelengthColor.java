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

import java.awt.Color;

/**
 * Class to calculate a physiological color for display on a computer monitor for a given wavelength of the light using Bruton's algorithm, see <a
 * href="http://www.midnightkite.com/color.html">COLOR SCIENCE web page</a> for details.
 * 
 * @author Bernd Rinn
 */
public class WavelengthColor
{
    private final static float NO_GAMMA = 1.0f;

    private final static int MAX_INTENSITY_VALUE = 255;

    private static final int MIN_WAVELENGTH = 380;

    private static final int MAX_WAVELENGTH = 780;

    private static float calcIntensityCorrectionFactor(int wavelength)
    {
        float factor;
        if (wavelength >= MIN_WAVELENGTH && wavelength < 420)
        {
            factor = 0.3f + 0.7f * (wavelength - MIN_WAVELENGTH) / (420.0f - MIN_WAVELENGTH);
        } else if (wavelength >= 420 && wavelength <= 700)
        {
            factor = 1.0f;
        } else if (wavelength > 700 && wavelength <= MAX_WAVELENGTH)
        {
            factor = 0.3f + 0.7f * (MAX_WAVELENGTH - wavelength) / (MAX_WAVELENGTH - 700.0f);
        } else
        {
            factor = 0.0f;
        }
        return factor;
    }

    private static int adjust(double value, double factor, double gamma)
    {
        if (value == 0.0)
        {
            return 0;
        } else if (gamma == 1.0)
        {
            return (int) Math.round(MAX_INTENSITY_VALUE * Math.pow(value * factor, gamma));
        } else
        {
            return (int) Math.round(MAX_INTENSITY_VALUE * value * factor);
        }
    }

    /**
     * Creates a physiological RGB color for the given <var>wavelength<var> in nanometer.
     * <p>
     * Does not perform a Gamma correction.
     */
    public static Color getColorForWavelength(int wavelength)
    {
        return getColorForWavelength(wavelength, NO_GAMMA);
    }

    /**
     * Creates a physiological RGB color for the given <var>wavelength<var> in nano-meters.
     * <p>
     * Performs a Gamma correction with the given <var>gamma</var> value.
     */
    public static Color getColorForWavelength(int wavelength, float gamma)
    {
        final float red, green, blue;

        if (wavelength >= MIN_WAVELENGTH && wavelength <= 440)
        {
            red = -(wavelength - 440.0f) / (440.0f - MIN_WAVELENGTH);
            green = 0.0f;
            blue = 1.0f;
        } else if (wavelength > 440 && wavelength <= 490)
        {
            red = 0.0f;
            green = (wavelength - 440.0f) / (490.0f - 440.0f);
            blue = 1.0f;
        } else if (wavelength > 490 && wavelength <= 510)
        {
            red = 0.0f;
            green = 1.0f;
            blue = -(wavelength - 510.0f) / (510.0f - 490.0f);

        } else if (wavelength > 510 && wavelength <= 580)
        {
            red = (wavelength - 510.0f) / (580.0f - 510.0f);
            green = 1.0f;
            blue = 0.0f;
        } else if (wavelength > 580 && wavelength <= 645)
        {
            red = 1.0f;
            green = -(wavelength - 645.0f) / (645.0f - 580.0f);
            blue = 0.0f;
        } else if (wavelength > 645 && wavelength <= MAX_WAVELENGTH)
        {
            red = 1.0f;
            green = 0.0f;
            blue = 0.0f;
        } else
        {
            // Wavelength is not visible.
            red = 0.0f;
            green = 0.0f;
            blue = 0.0f;
        }

        final double factor = calcIntensityCorrectionFactor(wavelength);

        final int r = adjust(red, factor, gamma);
        final int g = adjust(green, factor, gamma);
        final int b = adjust(blue, factor, gamma);

        return new Color(r, g, b);
    }

}