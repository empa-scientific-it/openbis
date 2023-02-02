/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.ReferenceMap;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageIdentifier;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;

/**
 * An image cache based on {@link ReferenceMap}. All entries with soft value references are removed when free heap space is needed.
 *
 * @author Franz-Josef Elmer
 */
public class ImageCache implements IImageProvider
{
    private final Map<String, BufferedImage> images = new ReferenceMap<String, BufferedImage>();

    private final Map<String, Size> imageSizes = new ReferenceMap<String, Size>();

    private final Map<String, Integer> imageColorDepths = new ReferenceMap<String, Integer>();

    private final Map<String, List<ImageIdentifier>> imageIdentifiers = new ReferenceMap<String, List<ImageIdentifier>>();

    /**
     * Returns or loads the image specified by file name, identifier and loading library.
     */
    @Override
    public synchronized BufferedImage getImage(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        String key = createKey(contentNode, imageIdOrNull, imageLibraryOrNull);
        BufferedImage image = images.get(key);
        if (image == null)
        {
            image = Utils.loadUnchangedImage(contentNode, imageIdOrNull, imageLibraryOrNull);
            images.put(key, image);
        }
        return image;
    }

    @Override
    public Size getImageSize(IHierarchicalContentNode contentNode, String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        String key = createKey(contentNode, imageIdOrNull, imageLibraryOrNull);
        Size size = imageSizes.get(key);
        if (size == null)
        {
            size = Utils.loadUnchangedImageSize(contentNode, imageIdOrNull, imageLibraryOrNull);
            imageSizes.put(key, size);
        }
        return size;
    }

    @Override
    public int getImageColorDepth(IHierarchicalContentNode contentNode, String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        String key = createKey(contentNode, imageIdOrNull, imageLibraryOrNull);
        Integer colorDepth = imageColorDepths.get(key);
        if (colorDepth == null)
        {
            colorDepth = Utils.loadUnchangedImageColorDepth(contentNode, imageIdOrNull, imageLibraryOrNull);
            imageColorDepths.put(key, colorDepth);
        }
        return colorDepth;
    }

    @Override
    public List<ImageIdentifier> getImageIdentifiers(IImageReader imageReaderOrNull, File file)
    {
        String key = (imageReaderOrNull == null ? "" : imageReaderOrNull.getName() + ":") + file;
        List<ImageIdentifier> identifiers = imageIdentifiers.get(key);
        if (identifiers == null)
        {
            identifiers = readImageIdentifiers(imageReaderOrNull, file);
            imageIdentifiers.put(key, identifiers);
        }
        return identifiers;
    }

    public int size()
    {
        return images.size();
    }

    private String createKey(IHierarchicalContentNode contentNode, String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull)
    {
        return contentNode.getName() + ":" + imageIdOrNull + " [" + imageLibraryOrNull + "]";
    }

    private static List<ImageIdentifier> readImageIdentifiers(IImageReader readerOrNull,
            File imageFile)
    {
        List<ImageIdentifier> ids = new ArrayList<ImageIdentifier>();
        if (readerOrNull == null)
        {
            ids.add(ImageIdentifier.NULL);
        } else
        {
            List<ImageID> imageIDs = readerOrNull.getImageIDs(imageFile);
            for (ImageID imageID : imageIDs)
            {
                ids.add(new ImageIdentifier(imageID.getSeriesIndex(), imageID.getTimeSeriesIndex(),
                        imageID.getFocalPlaneIndex(), imageID.getColorChannelIndex()));
            }
        }
        Collections.sort(ids);
        return Collections.unmodifiableList(ids);
    }

}
