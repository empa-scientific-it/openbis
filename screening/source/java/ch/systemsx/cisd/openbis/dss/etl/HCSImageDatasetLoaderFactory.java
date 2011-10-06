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

import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;

/**
 * @author Tomasz Pylak
 */
public class HCSImageDatasetLoaderFactory
{
    /** the loader has to be closed when it is not used any more to free database resources! */
    public static final IImagingDatasetLoader create(IHierarchicalContent content,
            String datasetCode)
    {
        return new ImagingDatasetLoader(DssScreeningUtils.getQuery(), datasetCode, content);
    }
}
