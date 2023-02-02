/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.geometry;

/**
 * Immutable value object representing a point in the two dimensional raster.
 *
 * @author Franz-Josef Elmer
 */
public final class Point
{
    private final int x;

    private final int y;

    /**
     * Create instance for the specified x- and y-coordinates.
     */
    public Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x coordinate.
     */
    public final int getX()
    {
        return x;
    }

    /**
     * Returns the y coordinate.
     */
    public final int getY()
    {
        return y;
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
        if (obj instanceof Point == false)
        {
            return false;
        }
        final Point point = (Point) obj;
        return point.x == x && point.y == y;
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
        return "(" + x + "," + y + ")";
    }

}
