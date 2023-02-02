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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Datasets connected to a sample (microscopy) or a well (HCS): all connected logical images.
 * 
 * @author Tomasz Pylak
 */
// NOTE: this is a good place to add information about other connected plate/well/sample datasets.
public class ImageSampleContent implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<LogicalImageInfo> logicalImages;

    private List<DatasetReference> unknownDatasets;

    // GWT only
    @SuppressWarnings("unused")
    private ImageSampleContent()
    {
    }

    public ImageSampleContent(List<LogicalImageInfo> logicalImages,
            List<DatasetReference> unknownDatasets)
    {
        this.logicalImages = logicalImages;
        this.unknownDatasets = unknownDatasets;
    }

    public List<LogicalImageInfo> getLogicalImages()
    {
        return logicalImages;
    }

    public List<DatasetReference> getUnknownDatasets()
    {
        return unknownDatasets;
    }
}
