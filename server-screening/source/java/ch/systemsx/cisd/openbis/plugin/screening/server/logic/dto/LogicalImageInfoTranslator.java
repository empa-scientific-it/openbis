/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LogicalImageInfo;

/**
 * @author pkupczyk
 */
public class LogicalImageInfoTranslator
{

    public LogicalImageInfo translate(ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo internalInfo)
    {
        if (internalInfo == null)
        {
            return null;
        }

        LogicalImageInfo apiInfo = new LogicalImageInfo();
        apiInfo.setImageDataset(new ImageDatasetEnrichedReferenceTranslator().translate(internalInfo.getImageDataset()));

        if (internalInfo.getChannelStacks() != null)
        {
            List<ImageChannelStack> apiStacks = new LinkedList<ImageChannelStack>();

            for (ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack internalStack : internalInfo.getChannelStacks())
            {
                apiStacks.add(new ImageChannelStackTranslator().translate(internalStack));
            }

            apiInfo.setChannelStacks(apiStacks);
        }

        return apiInfo;
    }

}
