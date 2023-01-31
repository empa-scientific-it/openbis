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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.hcs.Geometry;
import ch.systemsx.cisd.hcs.Location;

/**
 * Helper class to set the <code>is_complete</code> flag in the <i>BDS</i> library.
 * <p>
 * All the possible combinations are computed in the constructor. This class is also able to spot images which have already been handled.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class HCSImageCheckList
{

    private final List<Map<FullLocation, Check>> list;

    public HCSImageCheckList(final int numberOfChannels, final Geometry plateGeometry,
            final Geometry wellGeometry)
    {
        if (numberOfChannels < 1)
        {
            throw new IllegalArgumentException("Number of channels smaller than one.");
        }
        if (plateGeometry == null)
        {
            throw new IllegalArgumentException("Unspecified plate geometry.");
        }
        if (wellGeometry == null)
        {
            throw new IllegalArgumentException("Unspecified well geometry.");
        }
        list = new ArrayList<Map<FullLocation, Check>>();
        for (int i = 0; i < numberOfChannels; i++)
        {
            final Map<FullLocation, Check> map = new HashMap<FullLocation, Check>();
            for (int plateX = 1; plateX <= plateGeometry.getColumns(); plateX++)
            {
                for (int plateY = 1; plateY <= plateGeometry.getRows(); plateY++)
                {
                    final Location wellLocation = new Location(plateX, plateY);
                    for (int wellX = 1; wellX <= wellGeometry.getColumns(); wellX++)
                    {
                        for (int wellY = 1; wellY <= wellGeometry.getRows(); wellY++)
                        {
                            final Location tileLocation = new Location(wellX, wellY);
                            map.put(new FullLocation(wellLocation, tileLocation), new Check());
                        }
                    }
                }
            }
            assert map.size() == plateGeometry.getColumns() * plateGeometry.getRows()
                    * wellGeometry.getColumns() * wellGeometry.getRows() : "Wrong map size";
            list.add(map);
        }
    }

    public final void checkOff(final int channel, final Location wellLocation, final Location tileLocation)
    {
        assert wellLocation != null : "Unspecified well location.";
        assert tileLocation != null : "Unspecified tile location.";
        if (channel < 1)
        {
            throw new IllegalArgumentException("Not a positive channel number: " + channel);
        }
        if (channel > list.size())
        {
            throw new IllegalArgumentException("Channel number to large: " + channel + " > "
                    + list.size());
        }
        final Map<FullLocation, Check> map = list.get(channel - 1);
        final Check check = map.get(new FullLocation(wellLocation, tileLocation));
        if (check == null)
        {
            throw new IllegalArgumentException("Invalid well/tile location: " + wellLocation);
        }
        if (check.isCheckedOff())
        {
            throw new IllegalArgumentException("Image already handle for channel" + channel
                    + ", well:" + wellLocation + " tile:" + tileLocation);
        }
        check.checkOff();
    }

    public final List<FullLocation> getCheckedOnFullLocations()
    {
        final List<FullLocation> fullLocations = new ArrayList<FullLocation>();
        for (final Map<FullLocation, Check> map : list)
        {
            for (final Map.Entry<FullLocation, Check> entry : map.entrySet())
            {
                if (entry.getValue().isCheckedOff() == false)
                {
                    fullLocations.add(entry.getKey());
                }
            }
        }
        return fullLocations;
    }

    //
    // Helper classes
    //

    private static final class Check
    {
        private boolean checkedOff;

        final void checkOff()
        {
            checkedOff = true;
        }

        final boolean isCheckedOff()
        {
            return checkedOff;
        }
    }

    public final static class FullLocation extends AbstractHashable
    {

        final Location wellLocation;

        final Location tileLocation;

        FullLocation(final Location wellLocation, final Location tileLocation)
        {
            this.wellLocation = wellLocation;
            this.tileLocation = tileLocation;
        }

        private final static String toString(final Location location, final String type)
        {
            return type + "=" + location;
        }

        //
        // AbstractHashable
        //

        @Override
        public final String toString()
        {
            return "[" + toString(wellLocation, "well") + "," + toString(tileLocation, "tile")
                    + "]";
        }
    }
}
