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

import java.awt.image.BufferedImage;

/**
 * Interface for classes which can transform images. The transformation depends only on some
 * parameters and an image as input.
 * 
 * @author Franz-Josef Elmer
 */
public interface IImageTransformer
{
    /**
     * Transforms the specified image. The transformation must <i>not</i> change the input image.
     * 
     * @return The transformed image.
     */
    public BufferedImage transform(BufferedImage image);
}
