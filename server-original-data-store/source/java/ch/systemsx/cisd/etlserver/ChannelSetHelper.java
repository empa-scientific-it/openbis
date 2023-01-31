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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.systemsx.cisd.hcs.Channel;

/**
 * Helps to construct a sorted set of {@link Channel}.
 * 
 * @author Christian Ribeaud
 */
public final class ChannelSetHelper
{
    private final SortedSet<Integer> wavelengths;

    private Set<Channel> channels;

    private boolean locked = false;

    private Map<Integer, Channel> channelsByWavelength;

    public ChannelSetHelper()
    {
        wavelengths = new TreeSet<Integer>();
    }

    /**
     * Adds given <var>wavelength</var> to the internal set of wavelengths.
     * <p>
     * Wavelengths are ensured to be unique and are internally sorted.
     * </p>
     */
    public final void addWavelength(final int wavelength)
    {
        assert locked == false : "You can no longer change the state of this class.";
        wavelengths.add(wavelength);
    }

    /**
     * Returns an unmodifiable sorted (based on {@link Channel#getCounter()}) set of channels.
     * <p>
     * This is typically called after having added all the wavelengths.
     * </p>
     */
    public final Set<Channel> getChannelSet()
    {
        locked = true;
        if (channels == null)
        {
            final Set<Channel> set = new TreeSet<Channel>();
            final Iterator<Integer> iter = wavelengths.iterator();
            for (int i = 0; iter.hasNext(); i++)
            {
                set.add(new Channel(i + 1, iter.next()));
            }
            channels = Collections.unmodifiableSet(set);
        }
        return channels;
    }

    /**
     * For given wavelength returns corresponding <code>Channel</code>.
     * <p>
     * Never returns <code>null</code> and prefers to throw an exception if given <var>wavelength</var> can not be found.
     * </p>
     */
    public final Channel getChannelForWavelength(final int wavelength)
    {
        locked = true;
        if (channelsByWavelength == null)
        {
            final Map<Integer, Channel> map = new HashMap<Integer, Channel>();
            for (final Channel channel : getChannelSet())
            {
                map.put(channel.getWavelength(), channel);
            }
            channelsByWavelength = Collections.unmodifiableMap(map);
        }
        final Channel channel = channelsByWavelength.get(wavelength);
        assert channel != null : String.format("Given wavelength %d can not be found.", wavelength);
        return channel;
    }
}