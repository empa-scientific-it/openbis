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

import static ch.systemsx.cisd.imagereaders.ImageReaderConstants.BIOFORMATS_LIBRARY;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * Unit test for {@link ImageReaderFactory}.
 * 
 * @author Kaloyan Enimanev
 */
public class ImageReaderFactoryTest extends ImageReaderTestCase
{
    private static final FileFilter IGNORE_SVN = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return (false == pathname.getName().equalsIgnoreCase(".svn"));
            }
        };

    @DataProvider(name = "libraries")
    public Object[][] librariesToTest()
    {
        return new Object[][]
            {
                { ImageReaderConstants.IMAGEIO_LIBRARY.toLowerCase() },
                { ImageReaderConstants.IMAGEJ_LIBRARY.toLowerCase() },
                { ImageReaderConstants.BIOFORMATS_LIBRARY.toLowerCase() } };
    }

    @Test(dataProvider = "libraries")
    public void testLibrary(String library) throws Exception
    {
        ImageReadersTestHelper.setUpLibrariesFromManifest(library);
        for (File file : listValidImages(library))
        {
            assertImageReadable(library, file);
        }
        for (File file : listInvalidImages(library))
        {
            assertNoReaderFor(library, file);
        }
    }

    @Test
    public void testFindWithoutLibraryName() throws Exception
    {
        String tiffFileName = "./demo.tiff";
        ImageReadersTestHelper.setUpLibraries(ImageReaderConstants.BIOFORMATS_LIBRARY);

        assertNotNull("BioFormats library should return TIFF readers",
                ImageReaderFactory.tryGetReaderForFile(tiffFileName));

    }

    public void testGetReaderFromInvalidLibrary()
    {
        IImageReader reader = ImageReaderFactory.tryGetReaderForFile("invalid_library", null);
        assertNull(reader);
    }

    @Test(expectedExceptions = IOExceptionUnchecked.class)
    public void testReadNonExistingFile() throws Exception
    {
        final String invalidName = "invalid_file_path.tif";
        final File invalidFile = new File(invalidName);

        ImageReadersTestHelper.setUpLibraries(ImageReaderConstants.BIOFORMATS_LIBRARY);
        IImageReader reader =
                ImageReaderFactory.tryGetReaderForFile(BIOFORMATS_LIBRARY, invalidName);
        assertNotNull(reader);
        reader.readImage(invalidFile, ImageID.NULL, null);
    }

    private File[] listValidImages(String library)
    {
        return getValidImagesDir(library).listFiles(IGNORE_SVN);
    }

    private File[] listInvalidImages(String library)
    {
        return getInvalidImagesDir(library).listFiles(IGNORE_SVN);
    }

    private void assertImageReadable(String libraryName, File file)
    {
        IImageReader reader =
                ImageReaderFactory.tryGetReaderForFile(libraryName, file.getAbsolutePath());
        String error =
                String.format("Cannot find appropriate reader for file '%s' " + "in library '%s'",
                        file.getAbsolutePath(), libraryName);
        assertNotNull(error, reader);
        assertNotNull("Reader should not have NULL name", reader.getName());
        assertEquals(libraryName.toLowerCase(), reader.getLibraryName().toLowerCase());

        BufferedImage image = reader.readImage(file, ImageID.NULL, null);
        assertNotNull("Read image should not be null", image);
        assertTrue("Image should have non-negative height", image.getHeight() > 0);
        assertTrue("Image should have non-negative width", image.getWidth() > 0);
    }

    private void assertNoReaderFor(String library, File file)
    {
        IImageReader reader =
                ImageReaderFactory.tryGetReaderForFile(library, file.getAbsolutePath());
        String error =
                String.format("Library file '%s' " + " cannot read file '%s',"
                        + " but returns a non-null image reader.", library, file.getAbsolutePath());
        assertNull(error, reader);
    }

}
