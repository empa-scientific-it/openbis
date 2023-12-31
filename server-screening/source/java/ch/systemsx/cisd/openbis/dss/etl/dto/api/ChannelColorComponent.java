/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Color component of an image which constitutes one channel. Useful if the image consists of all channels merged together.
 * 
 * @author Tomasz Pylak
 */
public enum ChannelColorComponent
{
    RED, GREEN, BLUE;

    public static ColorComponent getColorComponent(ChannelColorComponent channelColorComponent)
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
