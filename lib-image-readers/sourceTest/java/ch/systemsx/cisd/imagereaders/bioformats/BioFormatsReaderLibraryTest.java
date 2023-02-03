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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class BioFormatsReaderLibraryTest extends ImageReaderTestCase
{
    @Test
    public void testTIFF() throws FileNotFoundException
    {
        BioFormatsReaderLibrary library = new BioFormatsReaderLibrary();
        String libraryName = library.getName();
        File imageFile = getImageFileForLibrary(libraryName, "multi-page.tif");
        IImageReader reader = library.tryGetReaderForFile(imageFile.getPath());

        List<ImageID> imageIDs = reader.getImageIDs(imageFile);
        assertEquals("[0-0-0-0, 0-1-0-0]", imageIDs.toString());

        BufferedImage image0 = reader.readImage(imageFile, imageIDs.get(0), null);
        assertEquals(1356, image0.getWidth());
        assertEquals(547, image0.getHeight());

        BufferedImage image1 = reader.readImage(imageFile, imageIDs.get(1), null);
        assertEquals(1356, image1.getWidth());
        assertEquals(547, image1.getHeight());

        int depth =
                reader.readColorDepth(
                        new RandomAccessFileImpl(new RandomAccessFile(imageFile, "r")),
                        imageIDs.get(0));

        assertEquals(8, depth);

        depth =
                reader.readColorDepth(
                        new RandomAccessFileImpl(new RandomAccessFile(imageFile, "r")),
                        imageIDs.get(1));

        assertEquals(8, depth);
    }
}
