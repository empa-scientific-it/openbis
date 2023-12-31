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
package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;

/**
 * Configuration of how images should be stored. By default:
 * <UL>
 * <LI>no thumbnails are generated</LI>
 * <LI>original data are stored as they come (without additional compression).</LI>
 * </UL>
 * 
 * @author Tomasz Pylak
 */
public class ImageStorageConfiguraton extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** Returns the default configuration. */
    public static ImageStorageConfiguraton createDefault()
    {
        return new ImageStorageConfiguraton();
    }

    // --- State ----------

    /** No thumbnails are generated by default. */
    private List<ThumbnailsStorageFormat> thumbnailsStorageFormatsList =
            new ArrayList<ThumbnailsStorageFormat>();

    private OriginalDataStorageFormat originalDataStorageFormat =
            OriginalDataStorageFormat.UNCHANGED;

    /** No preferences by default, each storage processor decides by its own if it is not set. */
    private Boolean storeChannelsOnExperimentLevelOrNull = null;

    /**
     * an image transformation to be applied before the image is stored.
     */
    private IImageTransformerFactory imageTransformerFactoryOrNull;

    /**
     * null by default, in this case some heuristics are used to find the right library to read the images, but it is slower and not all libraries are
     * tried.
     */
    private ImageLibraryInfo imageLibraryOrNull = null;

    // --- Getters & Setters ----------

    /** @return null if no thumbnails should be generated */
    public List<ThumbnailsStorageFormat> getThumbnailsStorageFormat()
    {
        return thumbnailsStorageFormatsList;
    }

    /** Set to null if no thumbnails should be generated. Overrides previous thumbnails settings. */
    public void setThumbnailsStorageFormat(ThumbnailsStorageFormat thumbnailsStorageFormatOrNull)
    {
        thumbnailsStorageFormatsList.clear();
        addThumbnailsStorageFormat(thumbnailsStorageFormatOrNull);

    }

    /**
     * Adds new thumbnails setting to the list
     */
    public void addThumbnailsStorageFormat(ThumbnailsStorageFormat thumbnailsStorageFormatOrNull)
    {
        if (thumbnailsStorageFormatOrNull != null)
        {
            thumbnailsStorageFormatsList.add(thumbnailsStorageFormatOrNull);
        }
    }

    /**
     * Convenience method to switch on thumbnails generation with default settings. Overrides the results of
     * {@link #setThumbnailsStorageFormat(ThumbnailsStorageFormat)}!.
     */
    public void switchOnThumbnailsGeneration()
    {
        thumbnailsStorageFormatsList.clear();
        thumbnailsStorageFormatsList.add(new ThumbnailsStorageFormat());
    }

    public OriginalDataStorageFormat getOriginalDataStorageFormat()
    {
        return originalDataStorageFormat;
    }

    public void setOriginalDataStorageFormat(OriginalDataStorageFormat originalDataStorageFormat)
    {
        this.originalDataStorageFormat = originalDataStorageFormat;
    }

    public void setOriginalDataStorageFormat(
            ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.OriginalDataStorageFormat originalDataStorageFormat)
    {
        this.originalDataStorageFormat =
                originalDataStorageFormat.getIndependentOriginalDataStorageFormat();
    }

    /**
     * Signalizes that the channels should be saved on experiment level rather than dataset level. Will be ignored in case of microscopy where all
     * channels are always saved at dataset level.
     */
    public void setStoreChannelsOnExperimentLevel(boolean storeChannelsOnExperimentLevel)
    {
        this.storeChannelsOnExperimentLevelOrNull = storeChannelsOnExperimentLevel;
    }

    public Boolean getStoreChannelsOnExperimentLevel()
    {
        return storeChannelsOnExperimentLevelOrNull;
    }

    public IImageTransformerFactory getImageTransformerFactory()
    {
        return imageTransformerFactoryOrNull;
    }

    /**
     * Allows for applying an image transformation on the fly when an image is fetched.
     */
    public void setImageTransformerFactory(IImageTransformerFactory transformerFactory)
    {
        this.imageTransformerFactoryOrNull = transformerFactory;
    }

    /** Sets the library which should be used to read the images. */
    public void setImageLibrary(ImageLibraryInfo imageLibrary)
    {
        this.imageLibraryOrNull = imageLibrary;
    }

    /**
     * @return library which should be used to read the images or null if the library is not specified.
     */
    public ImageLibraryInfo tryGetImageLibrary()
    {
        return imageLibraryOrNull;
    }

    @Override
    public String toString()
    {
        final StringBuilder buffer = new StringBuilder(super.toString());
        if (originalDataStorageFormat != OriginalDataStorageFormat.UNCHANGED)
        {
            appendNameAndObject(buffer, "original data storage format",
                    originalDataStorageFormat.toString());
        }
        if (thumbnailsStorageFormatsList.size() > 0)
        {
            for (ThumbnailsStorageFormat thumbnailsStorageFormat : thumbnailsStorageFormatsList)
            {
                appendNameAndObject(buffer, "thumbnails", thumbnailsStorageFormat.toString());
            }
        }
        if (storeChannelsOnExperimentLevelOrNull != null)
        {
            appendNameAndObject(buffer, "store channels on experiment level",
                    storeChannelsOnExperimentLevelOrNull);
        }
        if (imageTransformerFactoryOrNull != null)
        {
            appendNameAndObject(buffer, "image transformation", "present");
        }
        if (imageLibraryOrNull != null)
        {
            appendNameAndObject(buffer, "image library", imageLibraryOrNull.toString());
        }
        return buffer.toString();
    }

    protected static final void appendNameAndObject(final StringBuilder buffer, final String name,
            final Object object)
    {
        if (object != null)
        {
            buffer.append(name).append("::").append(object).append(";");
        }
    }
}
