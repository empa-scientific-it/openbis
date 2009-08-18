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

package ch.systemsx.cisd.etlserver;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

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
     * @throws IllegalArgumentException if either their isn't such a property or it has an invalid
     *             value.
     */
    public static PlateDimension getPlateDimension(final EntityPropertyPE[] properties)
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
    public static PlateDimension tryToGetPlateDimension(final EntityPropertyPE[] properties)
    {
        assert properties != null : "Unspecified properties";
        final String plateGeometryString =
                tryFindProperty(properties, PLATE_GEOMETRY_PROPERTY_NAME);
        if (plateGeometryString == null)
        {
            return null;
        }
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
        final String[] dims = sizeToken.split("X");
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

    private static String tryFindProperty(final EntityPropertyPE[] properties,
            final String propertyCode)
    {
        for (final EntityPropertyPE property : properties)
        {
            final PropertyTypePE propertyType =
                    property.getEntityTypePropertyType().getPropertyType();
            if (propertyType.getCode().equals(propertyCode))
            {
                return property.tryGetUntypedValue();
            }
        }
        return null;
    }


}
