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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.hcs.Location;

/**
 * Defines the mapping from tile position to well location. Rows in the mapping string provided to the constructor are separated by semicolon (";"),
 * columns are separated by comma (","). Position marked as zero ("-1"), means that it should be empty.
 * <p>
 * Usage Example: <code>
 * TileMapper mapper = new TileMapper("1,-1,3;4,5,-1",geometry); 
 * Location loc = mapper.tryGetLocation(4);
 * </code>
 * </p>
 * 
 * @author Izabela Adamczyk
 */
class TileMapper
{

    private final Map<Integer, Location> map = new HashMap<Integer, Location>();

    public static TileMapper tryCreate(String mappingString, Geometry geometry)
    {
        if (StringUtils.isBlank(mappingString))
        {
            return null;
        } else
        {
            return new TileMapper(mappingString, geometry);
        }
    }

    private TileMapper(String mappingString, Geometry geometry)
    {
        assert StringUtils.isNotBlank(mappingString);
        assert geometry != null;

        int max = geometry.getColumns() * geometry.getRows();
        String[] rows = StringUtils.split(mappingString, ";");
        if (rows == null || rows.length != geometry.getRows())
        {
            throw new IllegalArgumentException(String.format(
                    "Mapping does not match geometry. Number of rows expected: %d, but was: %d",
                    geometry.getRows(), rows == null ? "null" : rows.length));
        }
        for (int r = 0; r < rows.length; r++)
        {
            String[] columns = StringUtils.split(rows[r], ",");
            if (columns == null || columns.length != geometry.getColumns())
            {
                throw new IllegalArgumentException(
                        String.format(
                                "Mapping does not match geometry. Number of columns expected: %s, but was: %s",
                                geometry.getColumns(), columns == null ? "null" : columns.length));
            }
            for (int c = 0; c < columns.length; c++)
            {
                int value = Integer.parseInt(columns[c]);
                if (value > max || value < -1)
                {
                    throw new IllegalArgumentException(String.format(
                            "Tile value out of range. Allowed: [-1, %s], was: %s.", max, value));
                }
                if (value != -1)
                {
                    if (map.get(value) != null)
                    {
                        throw new IllegalArgumentException(String.format(
                                "Tile mapping for '%s' defined more than once.", value));

                    }
                    map.put(value, new Location(c + 1, r + 1));
                }
            }
        }
    }

    public Location tryGetLocation(int position)
    {
        return map.get(position);
    }

}