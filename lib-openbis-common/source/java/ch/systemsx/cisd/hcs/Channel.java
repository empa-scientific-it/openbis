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
 * A channel is composed of only one child: <code>wavelength</code>.
 * <p>
 * Each channel has its <code>counter</code> which uniquely identifies it. It implements {@link Comparable} based on the <code>wavelength</code>
 * value.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class Channel implements Comparable<Channel>
{
    static final String CHANNEL = "channel";

    static final String WAVELENGTH = "wavelength";

    private final int counter;

    private final int wavelength;

    public Channel(final int counter, final int wavelength)
    {
        assert counter > 0 : "Given counter must be > 0.";
        this.counter = counter;
        this.wavelength = wavelength;
    }

    public final int getCounter()
    {
        return counter;
    }

    public final int getWavelength()
    {
        return wavelength;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Channel == false)
        {
            return false;
        }
        final Channel channel = (Channel) obj;
        return channel.counter == counter;
    }

    @Override
    public final int hashCode()
    {
        return 17 * 37 + counter;
    }

    @Override
    public final String toString()
    {
        return CHANNEL + counter + "[" + wavelength + "=" + getWavelength() + "]";
    }

    //
    // Comparable
    //

    @Override
    public final int compareTo(final Channel o)
    {
        assert o != null : "Unspecified Channel.";
        return counter - o.counter;
    }
}
