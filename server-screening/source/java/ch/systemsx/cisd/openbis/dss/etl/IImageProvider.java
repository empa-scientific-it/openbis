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
import java.util.List;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.ImageIdentifier;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;

/**
 * Provider for images and some meta data specified by a {@link IHierarchicalContentNode}, an image ID and an image library.
 *
 * @author Franz-Josef Elmer
 */
public interface IImageProvider
{
    public BufferedImage getImage(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull);

    public Size getImageSize(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull);

    public int getImageColorDepth(IHierarchicalContentNode contentNode,
            String imageIdOrNull, ImageLibraryInfo imageLibraryOrNull);

    public List<ImageIdentifier> getImageIdentifiers(IImageReader imageReaderOrNull, File file);
}
