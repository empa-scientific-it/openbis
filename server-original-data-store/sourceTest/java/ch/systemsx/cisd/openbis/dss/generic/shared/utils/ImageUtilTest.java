/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.ByteBufferRandomAccessFile;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.common.image.IntensityRescaling.Channel;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReadersTestHelper;
import ch.systemsx.cisd.openbis.common.io.FileBasedContentNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ImageUtil.class)
public class ImageUtilTest extends AssertJUnit
{
    private static class MockIHierarchicalContentNode implements IHierarchicalContentNode
    {
        final MockRandomAccessFile is = new MockRandomAccessFile();

        @Override
        public boolean exists()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream getInputStream()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRelativePath()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getParentRelativePath()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDirectory()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getLastModified()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<IHierarchicalContentNode> getChildNodes() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public File getFile() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getFileLength() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getChecksum() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getChecksumCRC32() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isChecksumCRC32Precalculated()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
                IOExceptionUnchecked
        {
            return is;
        }

        @Override
        public File tryGetFile()
        {
            return null;
        }

    }

    private static class MockRandomAccessFile extends ByteBufferRandomAccessFile
    {
        public MockRandomAccessFile()
        {
            super(1);
        }

        boolean closeInvoked;

        @Override
        public void close()
        {
            closeInvoked = true;
        }
    }

    private File dir;

    @BeforeMethod
    public void setUp() throws Exception
    {
        dir = new File("../datastore_server/resource/test-data/ImageUtilTest");
        ImageReadersTestHelper.setUpLibraries(ImageReaderConstants.IMAGEIO_LIBRARY,
                ImageReaderConstants.BIOFORMATS_LIBRARY, ImageReaderConstants.IMAGEJ_LIBRARY);

    }

    @Test
    public void testGifImage()
    {
        assertImageSize(805, 1023, loadImageByFile("gif-example.gif"));
        assertImageSize(805, 1023, loadImageByInputStream("gif-example.gif"));
    }

    @Test
    public void testJpegImage()
    {
        assertImageSize(805, 1023, loadImageByFile("jpeg-example.jpg"));
        assertImageSize(805, 1023, loadImageByInputStream("jpeg-example.jpg"));
    }

    @Test
    public void testPngImage()
    {
        assertImageSize(805, 1023, loadImageByFile("png-example.png"));
        assertImageSize(805, 1023, loadImageByInputStream("png-example.png"));
    }

    @Test(groups = "slow")
    public void testTiffImage()
    {
        assertImageSize(805, 1023, loadImageByFile("tiff-example.tiff"));
        assertImageSize(805, 1023, loadImageByInputStream("tiff-example.tiff"));
    }

    @Test
    public void testCreateThumbnail()
    {
        BufferedImage image = loadImageByFile("gif-example.gif");
        assertImageSize(79, 100, ImageUtil.createThumbnailForDisplay(image, 200, 100, null));
        assertImageSize(100, 127, ImageUtil.createThumbnailForDisplay(image, 100, 200, null));
    }

    @Test
    public void testInputStreamAutomaticallyClosed()
    {
        MockIHierarchicalContentNode contentNode = new MockIHierarchicalContentNode();
        try
        {
            ImageUtil.loadImage(contentNode);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("File type of an image input stream couldn't be determined.",
                    ex.getMessage());
        }

        assertEquals(true, contentNode.is.closeInvoked);
    }

    private BufferedImage loadImageByFile(String fileName)
    {
        File file = new File(dir, fileName);
        BufferedImage image = ImageUtil.loadImage(file);
        return image;
    }

    private BufferedImage loadImageByInputStream(String fileName)
    {
        File file = new File(dir, fileName);
        return ImageUtil.loadImage(new FileBasedContentNode(file));
    }

    private void assertImageSize(int expectedWith, int expectedHeight, BufferedImage image)
    {
        assertEquals(expectedWith, image.getWidth());
        assertEquals(expectedHeight, image.getHeight());
    }

    public static BufferedImage loadImage(File imageFile)
    {
        return loadImage(new FileBasedContentNode(imageFile));
    }

    public static BufferedImage loadImage(IHierarchicalContentNode content)
    {
        return ImageUtil.loadImage(content);
    }

    @Test
    public void testGif() throws Exception
    {
        assertFileType("gif", "gif-example.gif");
    }

    @Test
    public void testJpg() throws Exception
    {
        assertFileType("jpg", "jpeg-example.jpg");
    }

    @Test
    public void testPng() throws Exception
    {
        assertFileType("png", "png-example.png");
    }

    @Test
    public void testTiff() throws Exception
    {
        assertFileType("tif", "tiff-example.tiff");
    }

    @Test
    public void testFileContainingOnlyOneUmlaut() throws Exception
    {
        assertFileType(null, "one-umlaut.txt");
    }

    @Test
    public void testMarkUnsupportedInputStream()
    {
        try
        {
            ImageUtil.tryToFigureOutFileTypeOf(new ByteBufferRandomAccessFile(1)
                {
                    @Override
                    public boolean markSupported()
                    {
                        return false;
                    }
                });
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Input stream does not support marking. "
                    + "Wrap input stream with a BufferedInputStream to solve the problem.",
                    ex.getMessage());
        }
    }

    @Test
    public void testInputStreamAtTheBeginning() throws Exception
    {
        byte[] bytes = "hello world".getBytes();
        ByteBufferRandomAccessFile buffer = new ByteBufferRandomAccessFile(bytes);

        ImageUtil.tryToFigureOutFileTypeOf(buffer);

        assertEquals(0, buffer.getFilePointer());
    }

    @Test
    public void testGetRepresentaticChannelIfBlack()
    {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);

        Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);

        assertEquals(Channel.RED, channel);
    }

    @Test
    public void testGetRepresentaticChannelIfReallyGray()
    {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(100, 100, 100));
        graphics.drawOval(0, 0, 5, 7);

        Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);

        assertEquals(Channel.RED, channel);
    }

    @Test
    public void testGetRepresentaticChannelIfColoredGrayGreen()
    {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(0, 255, 0));
        graphics.drawOval(0, 0, 5, 7);

        Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);

        assertEquals(Channel.GREEN, channel);
    }

    @Test
    public void testGetRepresentaticChannelIfColoredGrayGreenAndBlue()
    {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(0, 255, 255));
        graphics.drawOval(0, 0, 5, 7);

        Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);

        assertEquals(Channel.GREEN, channel);
    }

    @Test
    public void testGetRepresentaticChannelIfColored()
    {
        BufferedImage image = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.GREEN);
        graphics.drawOval(0, 0, 5, 7);
        graphics.setColor(Color.BLUE);
        graphics.fillOval(3, 3, 3, 4);

        Channel channel = ImageUtil.getRepresentativeChannelIfEffectiveGray(image);

        assertEquals(null, channel);
    }

    private void assertFileType(String expectedFileType, String fileName) throws Exception
    {
        RandomAccessFileImpl handle = null;
        try
        {
            handle = new RandomAccessFileImpl(new File(dir, fileName), "r");
            String type = ImageUtil.tryToFigureOutFileTypeOf(handle);

            assertEquals(expectedFileType, type);
        } finally
        {
            closeQuetly(handle);
        }

    }

    private void closeQuetly(RandomAccessFileImpl handle)
    {
        try
        {
            handle.close();
        } catch (Exception ex)
        {
            // keep quiet
        }

    }

}
