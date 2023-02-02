/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.InternalImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.InternalImageTransformationInfo;

/**
 * @author pkupczyk
 */
public class InternalImageChannelTranslator
{

    public InternalImageChannel translate(ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageChannel internalChannel)
    {
        if (internalChannel == null)
        {
            return null;
        }

        InternalImageChannel apiChannel = new InternalImageChannel();
        apiChannel.setCode(internalChannel.getCode());
        apiChannel.setLabel(internalChannel.getLabel());
        apiChannel.setDescription(internalChannel.tryGetDescription());
        apiChannel.setWavelength(internalChannel.tryGetWavelength());

        if (internalChannel.getAvailableImageTransformations() != null)
        {
            List<InternalImageTransformationInfo> apiTransformations = new LinkedList<InternalImageTransformationInfo>();

            for (ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageTransformationInfo internalTransformation : internalChannel
                    .getAvailableImageTransformations())
            {
                apiTransformations.add(new InternalImageTransformationInfoTranslator().translate(internalTransformation));
            }

            apiChannel.setAvailableImageTransformations(apiTransformations);
        }

        return apiChannel;
    }

}
