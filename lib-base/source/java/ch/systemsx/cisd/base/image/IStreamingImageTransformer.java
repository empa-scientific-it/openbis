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
import java.io.InputStream;
import java.io.OutputStream;

import ch.systemsx.cisd.base.image.IImageTransformer;

/**
 * Interface for classes which can transform images in a streaming mode. The transformation depends
 * only on some parameters and an image as stream of bytes.
 * 
 * @author Bernd Rinn
 */
public interface IStreamingImageTransformer extends IImageTransformer
{

    /**
     * Transforms the image provided as the <var>input</var> stream.
     * 
     * @return The transformed image.
     */
    public BufferedImage transform(InputStream input);

    /**
     * Transforms the image provided as the <var>input</var> stream.
     * 
     * @return The transformed image as a byte array that constitutes a PNG file.
     */
    public byte[] transformToPNG(InputStream input);

    /**
     * Transforms the image provided as the <var>input</var> stream. Writes the transformed PNG file
     * to the <var>output</var> stream.
     */
    public void transformToPNGStream(InputStream input, OutputStream output);
}
