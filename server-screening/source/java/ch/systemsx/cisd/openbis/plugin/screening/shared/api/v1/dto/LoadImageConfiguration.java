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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;

/**
 * A DTO for transmitting information about how the images should be loaded. The parameters that can be controlled include image size, format (PNG or
 * original), and the application of image transformations stored in openBIS.
 * <p>
 * The default configuration is to retrieve images in their original size and format (i.e., not converted to PNG), without the openBIS image
 * transformation applied.
 * <p>
 * Providing a non-null desired size will instruct the image loader to return images that maintain the aspect ratio of the original and fit in the
 * desired size.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@JsonObject("LoadImageConfiguration")
public class LoadImageConfiguration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private ImageSize desiredImageSizeOrNull = null;

    private boolean desiredImageFormatPng = false;

    private boolean openBisImageTransformationApplied = false;

    private String singleChannelImageTransformationCodeOrNull = null;

    /**
     * The desired size of the image. Null if the original size is requested.
     * <p>
     * The actual size of the returned image will have the same aspect ratio as the original, and thus might not be exactly the same as the desired
     * size, but will fit in the desired size.
     * 
     * @return The desired size of the image or null if the original size is to be returned.
     */
    public ImageSize getDesiredImageSize()
    {
        return desiredImageSizeOrNull;
    }

    /**
     * Set the desired size of the image.
     * <p>
     * The actual size of the returned image will have the same aspect ratio as the original, and thus might not be exactly the same as the desired
     * size, but will fit in the desired size.
     * 
     * @param desiredImageSizeOrNull Pass in the desired size or null if you want the image to have its original size.
     */
    public void setDesiredImageSize(ImageSize desiredImageSizeOrNull)
    {
        this.desiredImageSizeOrNull = desiredImageSizeOrNull;
    }

    /**
     * Should the image be converted from its original format to PNG?
     * 
     * @return True if the image should be converted to PNG; false if it should be left in its original format.
     */
    public boolean isDesiredImageFormatPng()
    {
        return desiredImageFormatPng;
    }

    /**
     * Set whether the image should be converted from its original format to PNG.
     * 
     * @param desiredImageFormatPng Pass in true if the image should be converted to PNG; false if it should be left in its original format.
     */
    public void setDesiredImageFormatPng(boolean desiredImageFormatPng)
    {
        this.desiredImageFormatPng = desiredImageFormatPng;
    }

    /**
     * Should the image transformation stored in openBIS for a channel or merged channel be applied to the image?
     * 
     * @return True if the image transformation should be applied; false if the original image should be returned.
     */
    public boolean isOpenBisImageTransformationApplied()
    {
        return openBisImageTransformationApplied;
    }

    /**
     * Set whether the image transformation stored in openBIS for a channel or merged channel should be applied.
     * 
     * @param openBisImageTransformationApplied Pass in true if the transformation should be applied; false otherwise.
     */
    public void setOpenBisImageTransformationApplied(boolean openBisImageTransformationApplied)
    {
        this.openBisImageTransformationApplied = openBisImageTransformationApplied;
        if (openBisImageTransformationApplied && getSingleChannelImageTransformationCode() == null)
        {
            // for backward compatibility - apply Color Adjustment tool transformation
            setSingleChannelImageTransformationCode("_CUSTOM");
        }
    }

    /** Code of the transformation, which should be applied to the image. Can return null. */
    public String getSingleChannelImageTransformationCode()
    {
        return singleChannelImageTransformationCodeOrNull;
    }

    /**
     * Sets the code of the transformation, which should be applied to the image. This parameter will be ignored if
     * {@link #isOpenBisImageTransformationApplied()} is false. To keep backward compatibility if {@link #isOpenBisImageTransformationApplied()} is
     * true and no image transformation code is specified, then the transformation defined by the Color Adjustment tool will be applied.
     * <p>
     * Transformation has to be assigned (usually during dataset registration) to the channel which will be fetched. Note that such transformations
     * can be channel-specific. If merged channels image will be requested or the transformation does not exist then untransformed image will be
     * returned.
     */
    public void setSingleChannelImageTransformationCode(String transformationCodeOrNull)
    {
        this.singleChannelImageTransformationCodeOrNull = transformationCodeOrNull;
    }

    @Override
    public String toString()
    {
        return "LoadImageConfiguration [desiredImageSizeOrNull=" + desiredImageSizeOrNull
                + ", desiredImageFormatPng=" + desiredImageFormatPng
                + ", openBisImageTransformationApplied=" + openBisImageTransformationApplied + "]";
    }

}
