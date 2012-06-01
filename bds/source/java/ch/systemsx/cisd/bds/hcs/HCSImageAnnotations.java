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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.IAnnotations;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * The <code>IAnnotations</code> implementation for <i>HCS</i>.
 * 
 * @author Franz-Josef Elmer
 */
public final class HCSImageAnnotations implements IAnnotations
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HCSImageAnnotations.class);

    private static final Set<Format> FORMATS =
            Collections.unmodifiableSet(new HashSet<Format>(Arrays
                    .asList(HCSImageFormatV1_0.HCS_IMAGE_1_0)));

    private final Set<Channel> channels;

    public HCSImageAnnotations(final Set<Channel> channels)
    {
        this.channels = channels;
    }

    /** Returns an unmodifiable set of <code>Channels</code>. */
    public final Set<Channel> getChannels()
    {
        return Collections.unmodifiableSet(channels);
    }

    //
    // IAnnotations
    //

    @Override
    public final void assertValid(final IFormattedData formattedData) throws DataStructureException
    {
        final Format format = formattedData.getFormat();
        if (FORMATS.contains(format) == false)
        {
            throw new DataStructureException("One of the following formats expected instead of '"
                    + format + "': " + FORMATS);
        }
        Object value =
                formattedData.getFormatParameters().getValue(HCSImageFormatV1_0.NUMBER_OF_CHANNELS);
        final int channelCount = ((Integer) value).intValue();
        final int size = channels.size();
        if (channelCount != size)
        {
            // we do not throw an exception here - it should be a warning if some files are missing
            operationLog.warn(String.format("Channel counts do not match (%d != %d).",
                    channelCount, size));
        }
    }

    @Override
    public final void saveTo(final IDirectory directory)
    {
        for (final Channel channel : channels)
        {
            channel.saveTo(directory);
        }
    }
}
