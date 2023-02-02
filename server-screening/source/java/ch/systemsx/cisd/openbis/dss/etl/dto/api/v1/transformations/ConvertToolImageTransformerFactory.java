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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.base.image.IStreamingImageTransformerFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformer;

/**
 * This class is obsolete, and should not be used. Use
 * {@link ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory} instead
 * 
 * @author Jakub Straszewski
 */
@JsonObject(value = "ConvertToolImageTransformerFactory_obsolete")
public class ConvertToolImageTransformerFactory implements IStreamingImageTransformerFactory
{
    private static final long serialVersionUID = 1L;

    private final String convertCliArguments;

    private final ToolChoice choice;

    /**
     * An enum to choose which of the two tools, ImageMagick or GraphicsMagick, to prefer or to enforce.
     */
    public enum ToolChoice
    {
        ENFORCE_IMAGEMAGICK(
                ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory.ToolChoice.ENFORCE_IMAGEMAGICK),
        ENFORCE_GRAPHICSMAGICK(
                ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory.ToolChoice.ENFORCE_GRAPHICSMAGICK),
        PREFER_IMAGEMAGICK(
                ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory.ToolChoice.PREFER_IMAGEMAGICK),
        PREFER_GRAPHICSMAGICK(
                ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory.ToolChoice.PREFER_GRAPHICSMAGICK);

        public final ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory.ToolChoice choice;

        private ToolChoice(
                ch.systemsx.cisd.openbis.dss.etl.dto.api.transformations.ConvertToolImageTransformerFactory.ToolChoice choice)
        {
            this.choice = choice;
        }
    }

    /**
     * Constructs the factory with {@link ToolChoice#PREFER_IMAGEMAGICK}.
     */
    public ConvertToolImageTransformerFactory(String convertCliArguments)
    {
        this(convertCliArguments, ToolChoice.ENFORCE_IMAGEMAGICK);
    }

    public ConvertToolImageTransformerFactory(String convertCliArguments, ToolChoice choice)
    {
        this.convertCliArguments = convertCliArguments;
        this.choice = choice;
    }

    @Override
    public ConvertToolImageTransformer createTransformer()
    {
        return new ConvertToolImageTransformer(convertCliArguments, choice.choice);
    }
}
