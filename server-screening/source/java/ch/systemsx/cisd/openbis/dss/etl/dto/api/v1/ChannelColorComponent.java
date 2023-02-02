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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * This class is obsolete, and should not be used. Use {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent} instead
 * 
 * @author Jakub Straszewski
 * @deprecated
 */
@Deprecated
public final class ChannelColorComponent
{
    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent RED =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.RED;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent GREEN =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.GREEN;

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent BLUE =
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.BLUE;

    private ChannelColorComponent()
    {
    }

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent valueOf(String item)
    {
        return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.valueOf(item);
    }

    public static ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent[] values()
    {
        return ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent.values();
    }

    public static ColorComponent getColorComponent(
            ch.systemsx.cisd.openbis.dss.etl.dto.api.ChannelColorComponent channelColorComponent)
    {
        switch (channelColorComponent)
        {
            case BLUE:
                return ColorComponent.BLUE;
            case GREEN:
                return ColorComponent.GREEN;
            case RED:
                return ColorComponent.RED;
        }

        return null;
    }
}
