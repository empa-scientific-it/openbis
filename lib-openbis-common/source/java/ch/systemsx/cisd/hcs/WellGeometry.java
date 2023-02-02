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
 * An <code>AbstractGeometry</code> implementation suitable for <i>Well</i>.
 * 
 * @author Christian Ribeaud
 */
public final class WellGeometry extends Geometry
{

    /**
     * Directory name which contains the well geometry.
     * <p>
     */
    public static final String WELL_GEOMETRY = "well_geometry";

    public WellGeometry(final Geometry geometry)
    {
        this(geometry.getRows(), geometry.getColumns());
    }

    public WellGeometry(final int rows, final int columns)
    {
        super(rows, columns);
    }

    /**
     * Creates a new <code>WellGeometry</code> from given <var>toString</var>.
     */
    public final static Geometry createFromString(final String toString)
    {
        return new WellGeometry(Geometry.createFromString(toString));
    }

    //
    // Geometry
    //

    @Override
    protected final String getGeometryDirectoryName()
    {
        return WELL_GEOMETRY;
    }

}
