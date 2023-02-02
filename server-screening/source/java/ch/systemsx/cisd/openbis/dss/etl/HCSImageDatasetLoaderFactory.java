/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;

/**
 * @author Tomasz Pylak
 */
public class HCSImageDatasetLoaderFactory
{
    /**
     * the loader has to be closed when it is not used any more to free database resources!
     * 
     * @return null if the dataset is not found in the imaging database
     */
    public static final IImagingDatasetLoader tryCreate(IHierarchicalContent content,
            String datasetCode)
    {
        return ImagingDatasetLoader.tryCreate(DssScreeningUtils.getQuery(), datasetCode, content);
    }

    public static IImagingDatasetLoader create(IHierarchicalContent content, String datasetCode)
    {
        IImagingDatasetLoader loader = tryCreate(content, datasetCode);
        if (loader == null)
        {
            throw new IllegalStateException(String.format(
                    "Dataset '%s' not found in the imaging database.", datasetCode));
        }
        return loader;
    }
}
