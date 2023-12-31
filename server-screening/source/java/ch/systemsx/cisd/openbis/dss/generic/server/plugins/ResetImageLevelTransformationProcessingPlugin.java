/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;

/**
 * Removes all image-level transformations.
 * 
 * @author Tomasz Pylak
 */
public class ResetImageLevelTransformationProcessingPlugin extends
        AbstractSpotImagesTransformerProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    public ResetImageLevelTransformationProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    protected IImageTransformerFactoryProvider getTransformationProvider(
            List<ImgImageEnrichedDTO> spotImages, IHierarchicalContent hierarchicalContent)
    {
        return NO_TRANSFORMATION_PROVIDER;
    }
}
