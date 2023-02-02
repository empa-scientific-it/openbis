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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Immutable class defining the width and height of something. Two instances are equal if and only if the widths and heights are equal.
 * 
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("Geometry")
public final class Geometry implements Serializable
{
    public static final Geometry GEOMETRY_96_8X12 = new Geometry(12, 8);

    public static final Geometry GEOMETRY_384_16X24 = new Geometry(24, 16);

    public static final Geometry GEOMETRY_1536_32X48 = new Geometry(48, 32);

    private static final long serialVersionUID = 1L;

    private int width;

    private int height;

    /**
     * Creates a new instance from the given cartesian dimensions.
     */
    public static Geometry createFromCartesianDimensions(int maxX, int maxY)
    {
        return new Geometry(maxX, maxY);
    }

    /**
     * Creates a new instance from the given cartesian dimensions.
     */
    public static Geometry createFromCartesianDimensions(int[] cartesianDims)
    {
        assert cartesianDims != null;
        assert cartesianDims.length == 2;

        return new Geometry(cartesianDims[0], cartesianDims[1]);
    }

    /**
     * Creates a new instance from the given number of rows and columns.
     */
    public static Geometry createFromRowColDimensions(int numberOfRows, int numberOfColumns)
    {
        return new Geometry(numberOfColumns, numberOfRows);
    }

    /**
     * Creates a new instance from the given number of rows and columns.
     */
    public static Geometry createFromPlateGeometryString(String plateGeometryStr)
    {
        int lastIndexOfUnderscore = plateGeometryStr.lastIndexOf('_');
        int lastIndexOfX = plateGeometryStr.lastIndexOf('X');
        if (lastIndexOfUnderscore < 0 || lastIndexOfX < 0)
        {
            throw new IllegalArgumentException("Invalid plate geometry string '" + plateGeometryStr
                    + "'");
        }
        try
        {
            int numberOfRows =
                    Integer.parseInt(plateGeometryStr.substring(lastIndexOfUnderscore + 1,
                            lastIndexOfX));
            int numberOfColumns = Integer.parseInt(plateGeometryStr.substring(lastIndexOfX + 1));
            return createFromRowColDimensions(numberOfRows, numberOfColumns);
        } catch (NumberFormatException ex)
        {
            throw new IllegalArgumentException("Invalid plate geometry string '" + plateGeometryStr
                    + "'");
        }
    }

    /**
     * Creates a new instance with specified width and height.
     * 
     * @throws IllegalArgumentException in case of negative arguments
     */
    private Geometry(int width, int height)
    {
        if (width < 0)
        {
            throw new IllegalArgumentException("Negative width: " + width);
        }
        if (height < 0)
        {
            throw new IllegalArgumentException("Negative height: " + height);
        }
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width.
     * 
     * @return a non-negative number.
     */
    public final int getWidth()
    {
        return width;
    }

    /**
     * Returns the height.
     * 
     * @return a non-negative number.
     */
    public final int getHeight()
    {
        return height;
    }

    /**
     * Returns the number of columns.
     * 
     * @return a non-negative number.
     */
    @JsonIgnore
    public final int getNumberOfColumns()
    {
        return width;
    }

    /**
     * Returns the number of rows.
     * 
     * @return a non-negative number.
     */
    @JsonIgnore
    public final int getNumberOfRows()
    {
        return height;
    }

    /**
     * Returns the dimension on the x-axis.
     * 
     * @return a non-negative number.
     */
    @JsonIgnore
    public final int getDimX()
    {
        return width;
    }

    /**
     * Returns the dimension on the y-axis.
     * 
     * @return a non-negative number.
     */
    @JsonIgnore
    public final int getDimY()
    {
        return height;
    }

    /**
     * Returns the cartesian coordinates as an <code>int[]</code>. The width is the x-dimension and the height is the y-dimension.
     */
    @JsonIgnore
    public final int[] getCartesianDimensions()
    {
        return new int[]
        { width, height };
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Geometry == false)
        {
            return false;
        }
        Geometry geometry = (Geometry) obj;
        return geometry.width == width && geometry.height == height;
    }

    @Override
    public int hashCode()
    {
        return 37 * width + height;
    }

    /**
     * Renders this in the form <code>&lt;width&gt;X&lt;height&gt;</code>.
     */
    public String toPlateGeometryStr()
    {
        return (height * width) + "_" + height + "X" + width;
    }

    /**
     * Renders this in the form <code>{&lt;width&gt;,&lt;height&gt;}</code>.
     */
    @Override
    public String toString()
    {
        return "{" + width + "," + height + "}";
    }

    //
    // JSON-RPC
    //

    private Geometry()
    {
    }

    private void setWidth(int width)
    {
        if (width < 0)
        {
            throw new IllegalArgumentException("Negative width: " + width);
        }
        this.width = width;
    }

    private void setHeight(int height)
    {
        if (height < 0)
        {
            throw new IllegalArgumentException("Negative height: " + height);
        }
        this.height = height;
    }

}
