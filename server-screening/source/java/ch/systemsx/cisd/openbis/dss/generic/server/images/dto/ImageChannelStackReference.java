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

package ch.systemsx.cisd.openbis.dss.generic.server.images.dto;

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.hcs.Location;

/**
 * Identifies one channel stack in a dataset, by well and tile location in HCS, tile location in microscopy or by the technical id.
 * 
 * @author Tomasz Pylak
 */
public class ImageChannelStackReference extends AbstractHashable
{
    /**
     * Addresses exactly one channel stack of HCS images for a given well and tile, when there are no image series.<br>
     * Note that if image series are present for a given well and tile then this object addresses nothing.
     */
    public static final class HCSChannelStackByLocationReference extends AbstractHashable
    {
        protected Location wellLocation;

        protected Location tileLocation;

        public HCSChannelStackByLocationReference(Location wellLocation, Location tileLocation)
        {
            this.wellLocation = wellLocation;
            this.tileLocation = tileLocation;
        }

        public Location getWellLocation()
        {
            return wellLocation;
        }

        public Location getTileLocation()
        {
            return tileLocation;
        }

        @Override
        public String toString()
        {
            return "[well=" + wellLocation + ", tile=" + tileLocation + "]";
        }
    }

    /**
     * Addresses exactly one channel stack of microscopy images for a given tile, when there are no image series.<br>
     * Note that if image series are present for a given well and tile then this object addresses nothing.
     */
    public static final class MicroscopyChannelStackByLocationReference extends AbstractHashable
    {
        protected Location tileLocation;

        public MicroscopyChannelStackByLocationReference(Location tileLocation)
        {
            this.tileLocation = tileLocation;
        }

        public Location getTileLocation()
        {
            return tileLocation;
        }

        @Override
        public String toString()
        {
            return "[tile=" + tileLocation + "]";
        }
    }

    public static final ImageChannelStackReference createHCSFromLocations(Location wellLocation,
            Location tileLocation)
    {
        return new ImageChannelStackReference(new HCSChannelStackByLocationReference(wellLocation,
                tileLocation), null, null);
    }

    public static final ImageChannelStackReference createMicroscopyFromLocations(
            Location tileLocation)
    {
        return new ImageChannelStackReference(null, new MicroscopyChannelStackByLocationReference(
                tileLocation), null);
    }

    public static final ImageChannelStackReference createFromId(long channelStackId)
    {
        return new ImageChannelStackReference(null, null, channelStackId);
    }

    private final HCSChannelStackByLocationReference hcsRefOrNull;

    private final MicroscopyChannelStackByLocationReference microscopyRefOrNull;

    private final Long idRefOrNull;

    private ImageChannelStackReference(HCSChannelStackByLocationReference hcsRefOrNull,
            MicroscopyChannelStackByLocationReference microscopyRefOrNull, Long idRefOrNull)
    {
        int notNulls =
                (hcsRefOrNull == null ? 0 : 1) + (microscopyRefOrNull == null ? 0 : 1)
                        + (idRefOrNull == null ? 0 : 1);
        assert notNulls == 1 : "exactly one channel stack reference should be not null: "
                + hcsRefOrNull + ", " + microscopyRefOrNull + ", " + idRefOrNull;
        this.hcsRefOrNull = hcsRefOrNull;
        this.microscopyRefOrNull = microscopyRefOrNull;
        this.idRefOrNull = idRefOrNull;
    }

    public Long tryGetChannelStackId()
    {
        return idRefOrNull;
    }

    public HCSChannelStackByLocationReference tryGetHCSChannelStack()
    {
        return hcsRefOrNull;
    }

    public MicroscopyChannelStackByLocationReference tryGetMicroscopyChannelStack()
    {
        return microscopyRefOrNull;
    }

    @Override
    public String toString()
    {
        if (idRefOrNull != null)
        {
            return "StackReference{id=" + idRefOrNull + "}";
        } else if (microscopyRefOrNull != null)
        {
            return "StackReference{micStack=" + microscopyRefOrNull + "}";
        } else
        {
            return "StackReference{hcsStack=" + hcsRefOrNull + "}";
        }
    }

}
