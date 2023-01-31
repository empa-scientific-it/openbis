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

package ch.systemsx.cisd.openbis.dss.generic.server;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageResolutionKind;

/**
 * Interface of the plugin deliering an image for dataset overview.
 * 
 * @author Piotr Buczek
 */
public interface IDatasetImageOverviewPlugin
{

    /**
     * @param datasetRoot directory in the store where dataset can be found.
     * @return {@link ResponseContentStream} with an image in given resolution for specified dataset
     */
    ResponseContentStream createImageOverview(String datasetCode, String datasetTypeCode,
            IHierarchicalContent datasetRoot, ImageResolutionKind resolution);

}
