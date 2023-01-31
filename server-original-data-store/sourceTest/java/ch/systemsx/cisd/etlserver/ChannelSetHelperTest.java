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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link ChannelSetHelper} class.
 * 
 * @author Christian Ribeaud
 */
public final class ChannelSetHelperTest
{

    @Test
    public final void testAddWavelength()
    {
        final int wavelength = 123;
        final ChannelSetHelper helper = new ChannelSetHelper();
        helper.addWavelength(wavelength);
        assertEquals(wavelength, helper.getChannelSet().iterator().next().getWavelength());
        try
        {
            helper.addWavelength(456);
            fail("ChannelSetHelper is locked.");
        } catch (final AssertionError e)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testGetChannelForWavelength()
    {
        final ChannelSetHelper helper = new ChannelSetHelper();
        helper.addWavelength(456);
        helper.addWavelength(893);
        helper.addWavelength(1);
        assertEquals(1, helper.getChannelForWavelength(1).getCounter());
        assertEquals(2, helper.getChannelForWavelength(456).getCounter());
        assertEquals(3, helper.getChannelForWavelength(893).getCounter());
        try
        {
            helper.getChannelForWavelength(2);
            fail("Given wavelength unknown.");
        } catch (final AssertionError e)
        {
            // Nothing to do here.
        }
    }
}
