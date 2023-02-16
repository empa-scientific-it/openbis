/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;

/**
 * Allows to get and set the channels chosen by default when images are shown in a specific context.
 * 
 * @author Piotr Buczek
 */
public interface IDefaultChannelState
{
    public List<String> tryGetDefaultChannels();

    public void setDefaultChannels(List<String> channels);

    public void setDefaultTransformation(String channel, String code);

    public String tryGetDefaultTransformation(String channel);

    public ImageResolution tryGetDefaultResolution(String windowId);

    public void setDefaultResolution(ImageResolution resolution, String windowId);

    public void setIntensityRange(String channel, IntensityRange intensityRange);

    public IntensityRange tryGetIntensityRange(String channel);
}