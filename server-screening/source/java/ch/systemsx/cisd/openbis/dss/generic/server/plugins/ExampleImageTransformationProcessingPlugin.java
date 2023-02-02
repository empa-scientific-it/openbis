/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ExampleImageTransformerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;

/**
 * A processing plugin that uses the example image transformation.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ExampleImageTransformationProcessingPlugin extends
        AbstractSpotImagesTransformerProcessingPlugin implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final class ExampleImageTransformationFactoryProvider implements
            IImageTransformerFactoryProvider, Serializable
    {

        private static final long serialVersionUID = 1L;

        @Override
        public IImageTransformerFactory tryGetTransformationFactory(ImgImageEnrichedDTO image)
        {
            return new ExampleImageTransformerFactory(colorPattern, brightnessDelta);
        }
    }

    private final String colorPattern;

    private final int brightnessDelta;

    private final ExampleImageTransformationFactoryProvider factoryProvider;

    public ExampleImageTransformationProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        colorPattern = PropertyUtils.getProperty(properties, "color-pattern", "rgb");
        brightnessDelta = PropertyUtils.getInt(properties, "brightness-delta", 0);
        factoryProvider = new ExampleImageTransformationFactoryProvider();
    }

    @Override
    protected IImageTransformerFactoryProvider getTransformationProvider(
            List<ImgImageEnrichedDTO> spotImages, IHierarchicalContent hierarchicalContent)
    {
        return factoryProvider;
    }
}
