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

import org.jfree.chart.axis.NumberTickUnit;

import ch.systemsx.cisd.common.geometry.ConversionUtils;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SpreadsheetColumnTickUnit extends NumberTickUnit
{
    private static final long serialVersionUID = 1L;

    // Note: this should work as a way of letting the axis select the appropriate
    // tick interval, but it doesn't work for some reason.
    //
    // public static TickUnitSource createStandardTickUnits()
    // {
    // TickUnits tickUnits = new TickUnits();
    // tickUnits.add(new SpreadsheetColumnTickUnit(1.0));
    // tickUnits.add(new SpreadsheetColumnTickUnit(2.0));
    // tickUnits.add(new SpreadsheetColumnTickUnit(5.0));
    // return tickUnits;
    // }

    /**
     * Constructor for a spreadsheet tick unit label.
     */
    public SpreadsheetColumnTickUnit(double size)
    {
        super(size);
    }

    /**
     * Converts the supplied value to a string.
     * <P>
     * Subclasses may implement special formatting by overriding this method.
     * 
     * @param value the data value.
     * @return Value as string.
     */
    @Override
    public String valueToString(double value)
    {
        if (value < 1)
        {
            return "";
        }
        return ConversionUtils.translateRowNumberIntoLetterCode((int) value);
    }
}
