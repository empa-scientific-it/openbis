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

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImagesReference;

/**
 * @author pkupczyk
 */
public class DatasetImagesReferenceTranslator
{

    public DatasetImagesReference translate(ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference internalReference)
    {
        if (internalReference == null)
        {
            return null;
        }

        DatasetImagesReference apiReference = new DatasetImagesReference();
        apiReference.setImageParameters(new ImageDatasetParametersTranslator().translate(internalReference.getImageParameters()));
        return apiReference;
    }
}
