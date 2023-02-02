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

/**
 * A <code>Geometry</code> is composed of 2 dimensions:
 * <ul>
 * <li>rows</li>
 * <li>columns</li>
 * </ul>
 * <p>
 * This class is not <code>abstract</code> but {@link #getGeometryDirectoryName()} must be overridden by subclasses in order to work properly.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class Geometry
{
    /**
     * The <code>rows</code>-<code>columns</code> separator in the string representation of this object.
     */
    private static final String X = "x";

    static final String NOT_POSITIVE = "Given geometry component '%s' must be > 0 (%d <= 0).";

    final static String ROWS = "rows";

    final static String COLUMNS = "columns";

    private final int rows;

    private final int columns;

    public Geometry(final int rows, final int columns)
    {
        assert columns > 0 : String.format(NOT_POSITIVE, "columns", columns);
        this.columns = columns;
        assert rows > 0 : String.format(NOT_POSITIVE, "rows", rows);
        this.rows = rows;
    }

    /** Return the number of columns this <code>Geometry</code> is composed of. */
    public final int getColumns()
    {
        return columns;
    }

    /** Return the number of rows this <code>Geometry</code> is composed of. */
    public final int getRows()
    {
        return rows;
    }

    /**
     * Loads a <code>Geometry</code> from given <var>toString</var>.
     * 
     * @param toString the output you get when calling {@link #toString()}.
     * @return <code>null</code> if operation fails.
     */
    public static Geometry createFromString(final String toString)
    {
        assert toString != null : "Given string can not be null.";
        final int index = toString.indexOf(X);
        if (index > -1)
        {
            try
            {
                int rows = Integer.parseInt(toString.substring(0, index));
                int columns = Integer.parseInt(toString.substring(index + X.length()));
                return new Geometry(rows, columns);
            } catch (NumberFormatException ex)
            {
                // Nothing to do here.
            }
        }
        return null;
    }

    /**
     * Whether this <code>Geometry</code> contains given <var>location</var>, meaning that it is a valid <code>Location</code>.
     */
    public final boolean contains(final Location location)
    {
        assert location != null : "Given location can not be null.";
        return location.getX() <= getColumns() && location.getY() <= getRows();
    }

    /**
     * Returns the directory name where this <code>Geometry</code> is saved.
     * <p>
     * Currently this method is not supported and must be implemented.
     * </p>
     */
    protected String getGeometryDirectoryName()
    {
        throw new UnsupportedOperationException();
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
        if (obj instanceof Geometry == false)
        {
            return false;
        }
        final Geometry geometry = (Geometry) obj;
        return geometry.rows == rows && geometry.columns == columns;
    }

    @Override
    public final int hashCode()
    {
        int hashCode = 17;
        hashCode = hashCode * 37 + getRows();
        hashCode = hashCode * 37 + getColumns();
        return hashCode;
    }

    @Override
    public final String toString()
    {
        return getRows() + X + getColumns();
    }
}
