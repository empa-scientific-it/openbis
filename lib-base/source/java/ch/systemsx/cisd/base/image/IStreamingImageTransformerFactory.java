/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.image;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.base.image.IImageTransformerFactory;

/**
 * Factory creating an {@link IStreamingImageTransformer}. The parameters of the transformer should
 * be stored as serializable attributes of concrete implementations of this interface.
 * 
 * @author Bernd Rinn
 */
@JsonObject(value="IStreamingImageTransformerFactory")
public interface IStreamingImageTransformerFactory extends IImageTransformerFactory
{
    /**
     * Creates a transformer object based on the attributes of the factory.
     */
    @Override
    public IStreamingImageTransformer createTransformer();
}
