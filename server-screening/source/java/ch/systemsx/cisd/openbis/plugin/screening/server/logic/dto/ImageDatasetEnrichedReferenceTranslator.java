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

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetOverlayImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetEnrichedReference;

/**
 * @author pkupczyk
 */
public class ImageDatasetEnrichedReferenceTranslator
{

    public ImageDatasetEnrichedReference translate(
            ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference internalReference)
    {
        if (internalReference == null)
        {
            return null;
        }

        ImageDatasetEnrichedReference apiReference = new ImageDatasetEnrichedReference();
        apiReference.setImageDataset(new DatasetImagesReferenceTranslator().translate(internalReference.getImageDataset()));

        if (internalReference.getOverlayDatasets() != null)
        {
            List<DatasetOverlayImagesReference> apiOverlays = new LinkedList<DatasetOverlayImagesReference>();

            for (ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetOverlayImagesReference internalOverlay : internalReference
                    .getOverlayDatasets())
            {
                apiOverlays.add(new DatasetOverlayImagesReferenceTranslator().translate(internalOverlay));
            }

            apiReference.setOverlayDatasets(apiOverlays);
        }

        return apiReference;
    }
}
