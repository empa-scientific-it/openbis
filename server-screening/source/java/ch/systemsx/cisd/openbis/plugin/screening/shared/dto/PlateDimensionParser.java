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

package ch.systemsx.cisd.openbis.plugin.screening.shared.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * Extractor and parser of the plate geometry from an array of properties.
 * 
 * @author Tomasz Pylak
 */
public class PlateDimensionParser
{
    public static final String PLATE_GEOMETRY_PROPERTY_NAME = "$PLATE_GEOMETRY";

    /**
     * Returns the plate geometry from the specified properties.
     * 
     * @throws IllegalArgumentException if either their isn't such a property or it has an invalid value.
     */
    public static PlateDimension getPlateDimension(final IEntityProperty[] properties)
    {
        final PlateDimension plateDimension = tryToGetPlateDimension(properties);
        if (plateDimension == null)
        {
            throw new IllegalArgumentException("Cannot find property "
                    + PLATE_GEOMETRY_PROPERTY_NAME);
        }
        return plateDimension;
    }

    /**
     * Tries to get the plate geometry from the specified properties.
     * 
     * @return <code>null</code> if their isn't such a property.
     * @throws IllegalArgumentException if the property for the plate geometry has an invalid value.
     */
    public static PlateDimension tryToGetPlateDimension(final IEntityProperty[] properties)
    {
        if (properties == null)
        {
            return null;
        }
        IEntityProperty plateGeometryProperty =
                EntityHelper.tryFindProperty(properties, PLATE_GEOMETRY_PROPERTY_NAME);
        if (plateGeometryProperty == null)
        {
            return null;
        }
        final String plateGeometryString = plateGeometryProperty.tryGetAsString();
        final PlateDimension dimension = tryParsePlateDimension(plateGeometryString);
        if (dimension == null)
        {
            throw new IllegalArgumentException("Cannot parse plate geometry " + plateGeometryString);
        }
        return dimension;

    }

    // parses plate geometry - takes the token after the last "_" sign and assumes that the number
    // of rows is separated
    // from number of columns by the 'X' sign, e.g. XXX_YYY_16x24
    private static PlateDimension tryParsePlateDimension(final String plateGeometryString)
    {
        final String[] tokens = plateGeometryString.split("_");
        final String sizeToken = tokens[tokens.length - 1];
        final String[] dims = sizeToken.toUpperCase().split("X");
        if (dims.length != 2)
        {
            return null;
        }
        final Integer rows = tryParseInteger(dims[0]);
        final Integer cols = tryParseInteger(dims[1]);
        if (rows == null || cols == null)
        {
            return null;
        }
        return new PlateDimension(rows, cols);
    }

    private static Integer tryParseInteger(final String value)
    {
        try
        {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e)
        {
            return null;
        }
    }

    public static Geometry getPlateGeometry(List<IEntityProperty> properties)
    {
        return getPlateDimension(properties.toArray(new IEntityProperty[0])).getPlateGeometry();
    }

}
