/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.hcs;

import java.io.Serializable;

import ch.systemsx.cisd.common.geometry.ConversionUtils;
import ch.systemsx.cisd.common.geometry.Point;

/**
 * A location in (x, y) coordinate space, specified in integer precision. X and Y start from 1. *
 * 
 * @author Christian Ribeaud
 */
public final class Location implements Serializable
{
    private static final long serialVersionUID = 1L;

    static final String NOT_POSITIVE = "Given coordinate '%s' must be > 0 (%d <= 0).";

    /**
     * The <i>x</i> (or <i>column</i>) coordinate.
     */
    private final int x;

    /**
     * The <i>y</i> (or <i>row</i>) coordinate.
     */
    private final int y;

    public Location(final int x, final int y)
    {
        assert x > 0 : String.format(NOT_POSITIVE, "x", x);
        this.x = x;
        assert y > 0 : String.format(NOT_POSITIVE, "y", y);
        this.y = y;
    }

    public static final Location createLocationFromRowAndColumn(final int row, final int column)
    {
        return new Location(column, row);
    }

    /**
     * For given <var>position</var> in given <code>geometry</code> returns corresponding <code>Location</code>. Position should be greater than 0.<br>
     * Assumes that element index grows as column/row numbers grow, so for a 3x2 geometry elements would be numbered like:<br>
     * 1 2 3<br>
     * 4 5 6<br>
     * 
     * @return <code>null</code> if position is out of range.
     */
    public static final Location tryCreateLocationFromRowwisePosition(final int position,
            final Geometry geometry)
    {
        return tryCreateLocationFromPosition(position, geometry, false);
    }

    /**
     * For given <var>position</var> in given <code>geometry</code> returns corresponding <code>Location</code>. Position should be greater than 0.<br>
     * Assumes that element index grows as row/column numbers grow, so for a 3x2 geometry elements would be numbered like:<br>
     * 1 3 5<br>
     * 2 4 6<br>
     * 
     * @return <code>null</code> if position is out of range.
     */
    public static final Location tryCreateLocationFromColumnwisePosition(final int position,
            final Geometry geometry)
    {
        return tryCreateLocationFromPosition(position, geometry, true);
    }

    private static final Location tryCreateLocationFromPosition(final int position,
            final Geometry geometry, boolean isColumnwise)
    {
        assert geometry != null : "Given geometry can not be null.";
        int rows = geometry.getRows();
        int columns = geometry.getColumns();
        int divisor = (isColumnwise ? rows : columns);
        final int max = columns * rows;
        // Given position is within the range.
        if (position > 0 && position <= max)
        {
            final int modulo = position % divisor;
            final int x = modulo == 0 ? divisor : modulo;
            final int y = (int) Math.ceil(position / (float) divisor);
            if (isColumnwise)
            {
                return new Location(y, x);
            } else
            {
                return new Location(x, y);
            }
        }
        return null;
    }

    /**
     * For given matrix <var>coordinate</var> (<strong>which is given in transposed form</strong>) returns corresponding <code>Location</code>.
     * 
     * @return <code>null</code> if given <var>coordinate</var> is not a matrix coordinate.
     */
    public static final Location tryCreateLocationFromTransposedMatrixCoordinate(
            final String coordinate)
    {
        try
        {
            Point point = ConversionUtils.parseSpreadsheetLocation(coordinate);
            return new Location(point.getY() + 1, point.getX() + 1);
        } catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    /**
     * Do the matrix coordinate conversion for coordinates that are specified this way, but in two columns. <strong>Note that first the
     * <var>columnCoord</var> is given and then the <var>rowCoord</var>.
     * 
     * @return <code>null</code> if given <var>coordinate</var> is not a matrix coordinate.
     */
    public static final Location tryCreateLocationFromTransposedSplitMatrixCoordinate(
            final String columnCoord, final String rowCoord)
    {
        return tryCreateLocationFromTransposedMatrixCoordinate(columnCoord + rowCoord);
    }

    /**
     * For given location returns corresponding matrix coordinate.
     */
    public final String createMatrixCoordinateFromLocation()
    {
        Point point = new Point(getY() - 1, getX() - 1);
        return ConversionUtils.convertToSpreadsheetLocation(point);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Location == false)
        {
            return false;
        }
        final Location location = (Location) obj;
        return location.x == x && location.y == y;
    }

    @Override
    public final int hashCode()
    {
        int hashCode = 17;
        hashCode = hashCode * 37 + x;
        hashCode = hashCode * 37 + y;
        return hashCode;
    }

    @Override
    public final String toString()
    {
        return "[x=" + x + ",y=" + y + "]";
    }

    /**
     * Returns the <i>x</i> (or <i>column</i>) coordinate.
     */
    public int getX()
    {
        return x;
    }

    /**
     * Returns the <i>y</i> (or <i>row</i>) coordinate.
     */
    public int getY()
    {
        return y;
    }
}
