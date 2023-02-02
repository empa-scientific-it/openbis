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

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.InternalImageChannel;

/**
 * @author pkupczyk
 */
public class ImageDatasetParametersTranslator
{

    public ImageDatasetParameters translate(ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters internalParameters)
    {
        if (internalParameters == null)
        {
            return null;
        }

        ImageDatasetParameters apiParameters = new ImageDatasetParameters();
        apiParameters.setDatasetCode(internalParameters.getDatasetCode());
        apiParameters.setRowsNumOrNull(internalParameters.tryGetRowsNum());
        apiParameters.setColsNumOrNull(internalParameters.tryGetColsNum());
        apiParameters.setTileRowsNum(internalParameters.getTileRowsNum());
        apiParameters.setTileColsNum(internalParameters.getTileColsNum());
        apiParameters.setMultidimensional(internalParameters.isMultidimensional());
        apiParameters.setMergedChannelTransformerFactorySignatureOrNull(internalParameters.tryGetMergedChannelTransformerFactorySignature());

        if (internalParameters.getChannels() != null)
        {
            List<InternalImageChannel> apiChannels = new LinkedList<InternalImageChannel>();

            for (ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.InternalImageChannel internalChannel : internalParameters.getChannels())
            {
                apiChannels.add(new InternalImageChannelTranslator().translate(internalChannel));
            }

            apiParameters.setChannels(apiChannels);
        }

        return apiParameters;
    }
}
