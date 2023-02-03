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

package ch.systemsx.cisd.imagereaders;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * {@link IImageReader} can read images in a particular format (e.g. TIFF, JPG, GIF etc.). To obtain
 * the correct {@link IImageReader} instance use the API of {@link ImageReaderFactory}.
 * 
 * @author Bernd Rinn
 */
public interface IImageReader
{
    /**
     * Return the name of the library for this reader.
     */
    public String getLibraryName();

    /**
     * Return the name of the reader.
     */
    public String getName();

    /**
     * Returns a list of IDs uniquely identifying all images in the specified file.
     */
    public List<ImageID> getImageIDs(File file) throws IOExceptionUnchecked;

    /**
     * Returns a list of IDs uniquely identifying all images in the specified byte array.
     */
    public List<ImageID> getImageIDs(byte[] bytes);

    /**
     * Returns a list of IDs uniquely identifying all images in the specified handle.
     */
    public List<ImageID> getImageIDs(IRandomAccessFile handle) throws IOExceptionUnchecked;

    /**
     * Reads a {@link BufferedImage} from a {@link File}.
     * 
     * @param file the image file
     * @param imageID the ID of the image to be read
     * @param params read parameters
     */
    public BufferedImage readImage(File file, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked;

    /**
     * Reads a {@link BufferedImage} from a byte array.
     * 
     * @param bytes the image file as a byte array
     * @param imageID the ID of the image to be read
     * @param params read parameters
     */
    public BufferedImage readImage(byte[] bytes, ImageID imageID, IReadParams params);

    /**
     * Reads a {@link BufferedImage} from a handle.
     * 
     * @param handle the image file as {@link IRandomAccessFile}
     * @param imageID the ID of the image to be read
     * @param params read parameters
     */
    public BufferedImage readImage(IRandomAccessFile handle, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked;

    /**
     * Return <code>true</code> if this reader is capable of reading metadata.
     */
    public boolean isMetaDataAware();

    /**
     * Reads image metadata from {@link File}. Returns an empty map if the reader is not capable of
     * reading metadata.
     * 
     * @param file the image file
     * @param imageID the ID of the image to be read
     * @param params read parameters
     */
    public Map<String, Object> readMetaData(File file, ImageID imageID, IReadParams params)
            throws IOExceptionUnchecked;

    /**
     * Reads image metadata from byte array. Returns an empty map if the reader is not capable of
     * reading metadata.
     * 
     * @param bytes the image file as a byte array
     * @param imageID the ID of the image to be read
     * @param params read parameters
     */
    public Map<String, Object> readMetaData(byte[] bytes, ImageID imageID, IReadParams params);

    /**
     * Reads image metadata from handle. Returns an empty map if the reader is not capable of
     * reading metadata.
     * 
     * @param handle the image file as {@link IRandomAccessFile}
     * @param imageID the ID of the image to be read
     * @param params read parameters
     */
    public Map<String, Object> readMetaData(IRandomAccessFile handle, ImageID imageID,
            IReadParams params) throws IOExceptionUnchecked;

    /**
     * Reads image dimensions (width and height).
     * 
     * @param handle the image file as {@link IRandomAccessFile}
     * @param imageID the ID of the image to be read
     * @return dimensions of the image
     */
    public Dimension readDimensions(IRandomAccessFile handle, ImageID imageID);

    /**
     * Reads image color depth
     * 
     * @param handle the image file as {@link IRandomAccessFile}
     * @param imageID the ID of the image to be read
     * @return the size of the color component
     */
    public Integer readColorDepth(IRandomAccessFile handle, ImageID imageID);
    
    /**
     * Closes this reader and releases resource.
     */
    public void close();
}
