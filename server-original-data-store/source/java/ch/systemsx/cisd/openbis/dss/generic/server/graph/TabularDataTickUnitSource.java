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

import java.io.Serializable;

import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;

/**
 * A {@link TickUnitSource} derived from {@link org.jfree.chart.axis.StandardTickUnitSource}.
 * <p>
 * Whereas StandardTickUnitSource always generates labels that use scientific notation, this class uses standard numerical notation and switched to
 * scientific notation only if the number of digits exceeds a configurable value.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataTickUnitSource implements TickUnitSource, Serializable
{
    final private static long serialVersionUID = 1L;

    /** Constant for log(10.0). */
    final private static double LOG_10_VALUE = Math.log(10.0);

    /**
     * Returns a tick unit that is larger than the supplied unit.
     * 
     * @param unit the unit (<code>null</code> not permitted).
     * @return A tick unit that is larger than the supplied unit.
     */
    @Override
    public TickUnit getLargerTickUnit(TickUnit unit)
    {
        double x = unit.getSize();
        return getCeilingTickUnit(x);
    }

    /**
     * Returns the tick unit in the collection that is greater than or equal to (in size) the specified unit.
     * 
     * @param unit the unit (<code>null</code> not permitted).
     * @return A unit from the collection.
     */
    @Override
    public TickUnit getCeilingTickUnit(TickUnit unit)
    {
        return getLargerTickUnit(unit);
    }

    /**
     * Returns the tick unit in the collection that is greater than or equal to the specified size.
     * 
     * @param size the size.
     * @return A unit from the collection.
     */
    @Override
    public TickUnit getCeilingTickUnit(double size)
    {
        final double numberOfDigits = Math.log(size) / LOG_10_VALUE;
        final double higher = Math.ceil(numberOfDigits);
        final double fullTickIncrement = Math.pow(10, higher);
        final double halfTickIncrement = fullTickIncrement / 2;
        final double tickIncrement =
                (size <= halfTickIncrement) ? halfTickIncrement : fullTickIncrement;
        return new TabularDataTickUnit(tickIncrement, Math.abs(higher), size >= 1.0);
    }
}
