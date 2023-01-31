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

import java.text.DecimalFormat;

import org.jfree.chart.axis.NumberTickUnit;

/**
 * This is a variation on the NumberTickUnit that gracefully switches from standard to scientific notation depending on the size of the numbers for
 * the labels.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataTickUnit extends NumberTickUnit
{
    private static final long serialVersionUID = 1L;

    /** Constant for log(10.0). */
    final private static double LOG_10_VALUE = Math.log(10.0);

    final private static int largeScientificNotationTransition = 6;

    final private static int smallScientificNotationTransition = 3;

    public TabularDataTickUnit(double size)
    {
        // The precision is determined by the number of digits in size
        this(size, Math.abs(Math.ceil(Math.log(size) / LOG_10_VALUE)), size >= 1.0);
    }

    /**
     * @param size The tick unit size
     * @param precision The number of digits we want to show
     * @param greaterThan1 Is the tick unit greater than 1?
     */
    public TabularDataTickUnit(double size, double precision, boolean greaterThan1)
    {
        super(size, getNumberFormat(precision, greaterThan1));
    }

    /**
     * Use the precision to determine if we should return standard or scientific notation.
     * 
     * @param precision The desired precision of the numbers to display.
     * @param greaterThan1 True if the numbers to be shown are greater then 1
     */
    private static DecimalFormat getNumberFormat(double precision, boolean greaterThan1)
    {
        DecimalFormat numberFormat;
        if (greaterThan1)
        {
            if (precision > largeScientificNotationTransition)
            {
                numberFormat = new DecimalFormat("0.0##E0");
            } else
            {
                numberFormat = getStandardNumberFormat((int) precision, greaterThan1);
            }
        } else
        {
            if (precision > smallScientificNotationTransition)
            {
                numberFormat = new DecimalFormat("0.0##E0");
            } else
            {
                numberFormat = getStandardNumberFormat((int) precision, greaterThan1);
            }
        }
        return numberFormat;
    }

    /**
     * Return a DecimalFormat that uses standard notation (not scientific notation).
     * 
     * @param precision The desired precision of the numbers to display.
     * @param greaterThan1 True if the numbers to be shown are greater then 1
     */
    private static DecimalFormat getStandardNumberFormat(int precision, boolean greaterThan1)
    {
        StringBuilder sb = new StringBuilder();
        if (greaterThan1)
        {
            for (int i = 0; i < precision; ++i)
            {
                sb.append("#");
            }
            sb.append("0");
        } else
        {
            sb.append("0.0");
            for (int i = 0; i < precision - 1; ++i)
            {
                sb.append("0");
            }
        }

        return new DecimalFormat(sb.toString());
    }

    @Override
    public String valueToString(double value)
    {
        return super.valueToString(value);
    }

}
