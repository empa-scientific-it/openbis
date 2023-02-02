/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ImagingChannelsCreator;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColor;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorRGB;

/**
 * Test of {@link ImagingChannelsCreator}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = ImagingChannelsCreator.class)
public class ImagingChannelsCreatorTest extends AssertJUnit
{
    @Test
    public void testFillMissingChannelColors()
    {
        List<Channel> channels =
                Arrays.asList(mkChannel(), mkChannel(ChannelColor.RED), mkChannel());
        ImagingChannelsCreator.fillMissingChannelColors(channels);

        assertEqual(new ChannelColorRGB(0, 0, 255), channels.get(0));
        assertEqual(new ChannelColorRGB(255, 0, 0), channels.get(1));
        assertEqual(new ChannelColorRGB(0, 255, 0), channels.get(2));
    }

    @Test
    public void testFillMissingChannelColorsRGB()
    {
        ChannelColorRGB redish = new ChannelColorRGB(200, 10, 10);
        List<Channel> channels = Arrays.asList(mkChannel(), mkChannel(redish), mkChannel());
        ImagingChannelsCreator.fillMissingChannelColors(channels);

        assertEqual(new ChannelColorRGB(0, 0, 255), channels.get(0));
        assertEqual(redish, channels.get(1));
        assertEqual(new ChannelColorRGB(0, 255, 0), channels.get(2));
    }

    @Test
    public void testFindNearestPlainChannelColor()
    {
        assertEquals(ChannelColor.RED, findNearestColor(30, 1, 9));
        assertEquals(ChannelColor.RED_BLUE, findNearestColor(30, 1, 30));
        assertEquals(ChannelColor.GREEN, findNearestColor(0, 200, 29));
        assertEquals(ChannelColor.RED_GREEN, findNearestColor(30, 30, 3));
        assertEquals(ChannelColor.BLUE, findNearestColor(30, 1, 255));
        assertEquals(ChannelColor.GREEN_BLUE, findNearestColor(1, 31, 31));
    }

    private static ChannelColor findNearestColor(int r, int g, int b)
    {
        return ImagingChannelsCreator.findNearestPlainChannelColor(new ChannelColorRGB(r, g, b));
    }

    private static void assertEqual(ChannelColorRGB expectedColor, Channel channel)
    {
        if (expectedColor.equals(channel.tryGetChannelColor()) == false)
        {
            fail("Expected " + expectedColor + " but got: " + channel.tryGetChannelColor());
        }
    }

    private static Channel mkChannel()
    {
        return mkChannel((ChannelColorRGB) null);
    }

    private static Channel mkChannel(ChannelColorRGB colorOrNull)
    {
        return new Channel("code", "label", colorOrNull);
    }

    private static Channel mkChannel(ChannelColor color)
    {
        return mkChannel(color.getRGB());
    }
}