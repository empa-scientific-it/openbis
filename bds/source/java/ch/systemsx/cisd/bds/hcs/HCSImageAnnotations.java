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

package ch.systemsx.cisd.bds.hcs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.IAnnotations;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * The <code>IAnnotations</code> implementation for <i>HCS</i>.
 * 
 * @author Franz-Josef Elmer
 */
public final class HCSImageAnnotations implements IAnnotations
{
    private static final String DEVICE_ID = "device_id";

    private static final Set<Format> FORMATS =
            Collections.unmodifiableSet(new HashSet<Format>(Arrays
                    .asList(HCSImageFormatV1_0.HCS_IMAGE_1_0)));

    private Set<Channel> channels;

    private String deviceID;

    public final String getDeviceID()
    {
        return deviceID;
    }

    public final void setDeviceID(final String deviceID)
    {
        this.deviceID = deviceID;
    }

    public final Set<Channel> getChannels()
    {
        return Collections.unmodifiableSet(channels);
    }

    public final void setChannels(final Set<Channel> channels)
    {
        this.channels = channels;
    }

    //
    // IAnnotations
    //

    public final void assertValid(final IFormattedData formattedData) throws DataStructureException
    {
        final Format format = formattedData.getFormat();
        if (FORMATS.contains(format) == false)
        {
            throw new DataStructureException("One of the following formats expected instead of '"
                    + format + "': " + FORMATS);
        }
        // final int channelCount =
        // ((Integer) formattedData.getFormatParameters().getValue(
        // HCSImageFormatV1_0.NUMBER_OF_CHANNELS)).intValue();
        // if (channelCount != channels.size())
        // {
        // throw new DataStructureException("Channel counts do not match.");
        // }
    }

    public final void saveTo(final IDirectory directory)
    {
        if (deviceID != null)
        {
            directory.addKeyValuePair(DEVICE_ID, deviceID);
        }
        for (final Channel channel : channels)
        {
            channel.saveTo(directory);
        }
    }
}
