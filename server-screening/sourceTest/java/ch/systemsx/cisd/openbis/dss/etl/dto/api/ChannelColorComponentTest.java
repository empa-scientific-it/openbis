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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Test of {@link ChannelColorComponent} and {@link ColorComponent}.
 * 
 * @author Tomasz Pylak
 */
public class ChannelColorComponentTest
{
    @Test
    /** it should be possible to translate each {@link ChannelColorComponent} to {@link ColorComponent} and vice-versa. */
    public void testEnumsSynchronized()
    {
        for (ChannelColorComponent channelColComp : ChannelColorComponent.values())
        {
            ColorComponent.valueOf(channelColComp.name());
        }
        for (ColorComponent colComp : ColorComponent.values())
        {
            ChannelColorComponent.valueOf(colComp.name());
        }

    }
}
