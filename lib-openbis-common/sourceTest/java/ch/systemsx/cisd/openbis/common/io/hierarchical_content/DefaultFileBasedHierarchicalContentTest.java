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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.HierarchicalStructureDuplicatorFileToHDF5;
import ch.systemsx.cisd.openbis.common.hdf5.HierarchicalStructureDuplicatorFileToHDF5.DuplicatorWriterClient;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Unit tests for {@link DefaultFileBasedHierarchicalContent}
 * 
 * @author Piotr Buczek
 */
public class DefaultFileBasedHierarchicalContentTest extends AbstractFileSystemTestCase
{

    private File rootDir;

    private File file1;

    private File file2;

    private File subDir;

    private File subFile1;

    private File subFile2;

    private File subFile3;

    private File subSubDir;

    private File subSubFile;

    // mocks

    private Mockery context;

    private IHierarchicalContentFactory hierarchicalContentFactory;

    private IDelegatedAction onCloseAction;

    @BeforeTest
    public void disableHDF5ContainerCaching()
    {
        HDF5Container.disableCaching();
    }

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();
        hierarchicalContentFactory = context.mock(IHierarchicalContentFactory.class);
        onCloseAction = context.mock(IDelegatedAction.class);

        rootDir = new File(workingDirectory, "rootDir");
        rootDir.mkdirs();
        file1 = new File(rootDir, "file1");
        file2 = new File(rootDir, "file2");

        subDir = new File(rootDir, "subDir");
        subDir.mkdirs();
        subFile1 = new File(subDir, "subFile1");
        subFile2 = new File(subDir, "subFile2");
        subFile3 = new File(subDir, "subFile3");

        subSubDir = new File(subDir, "subSubDir");
        subSubDir.mkdirs();
        subSubFile = new File(subSubDir, "subSubFile");

        for (File f : Arrays.asList(file1, file2, subFile1, subFile2, subFile3, subSubFile))
        {
            FileUtilities.writeToFile(f, f.getName() + " data");
        }
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private DefaultFileBasedHierarchicalContent createContent(File file)
    {
        return new DefaultFileBasedHierarchicalContent(hierarchicalContentFactory, file,
                onCloseAction);
    }

    @Test
    public void testFailWithNonExistentRoot()
    {
        final File fakeFile = new File(workingDirectory, "fakeFile");
        try
        {
            createContent(fakeFile);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(fakeFile.getAbsolutePath() + " doesn't exist", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testFailWithNonDirectoryRoot() throws IOException
    {
        final File rootFile = new File(workingDirectory, "rootFile");
        rootFile.createNewFile();
        try
        {
            createContent(rootFile);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(rootFile.getAbsolutePath() + " is not a directory", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testEqualsAndHashCode()
    {
        DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);
        DefaultFileBasedHierarchicalContent rootContentSameFile = createContent(rootDir);
        assertEquals(rootContent, rootContentSameFile);
        assertEquals(rootContent.hashCode(), rootContentSameFile.hashCode());

        File sameRootDir = new File(workingDirectory, "rootDir");
        DefaultFileBasedHierarchicalContent rootContentSameNewFile = createContent(sameRootDir);
        assertEquals(rootContent, rootContentSameNewFile);
        assertEquals(rootContent.hashCode(), rootContentSameNewFile.hashCode());

        File differentRootDir = new File(workingDirectory, "rootDir2");
        differentRootDir.mkdir();
        DefaultFileBasedHierarchicalContent rootContentDifferentFile =
                createContent(differentRootDir);
        assertFalse(rootContentDifferentFile.equals(rootContent));
        assertFalse(rootContent.hashCode() == rootContentDifferentFile.hashCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testClose()
    {
        DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        context.checking(new Expectations()
            {
                {
                    one(onCloseAction).execute();
                }
            });
        rootContent.close();

        context.assertIsSatisfied();
    }

    @Test
    public void testGetRootNode()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateRootNode(rootContent);

        IHierarchicalContentNode root1 = rootContent.getRootNode();
        assertSame(rootDir, root1.getFile());
        IHierarchicalContentNode root2 = rootContent.getRootNode();
        assertSame(root1, root2);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeWithBlankPath()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateRootNode(rootContent);

        IHierarchicalContentNode rootNode = rootContent.getRootNode();
        IHierarchicalContentNode nullNode = rootContent.getNode(null);
        IHierarchicalContentNode emptyNode = rootContent.getNode("");
        checkNodeMatchesFile(rootNode, rootDir);
        assertSame(rootNode, nullNode);
        assertSame(rootNode, emptyNode);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeOfExistingFile()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        final List<File> existingFiles =
                Arrays.asList(file1, file2, subDir, subFile1, subFile2, subFile3, subSubFile);

        context.checking(new Expectations()
            {
                {
                    for (File existingFile : existingFiles)
                    {
                        one(hierarchicalContentFactory).asHierarchicalContentNode(rootContent,
                                existingFile);
                        will(returnValue(createDummyFileBasedNode(rootDir, existingFile)));
                    }
                }
            });
        for (File existingFile : existingFiles)
        {
            String relativePath = FileUtilities.getRelativeFilePath(rootDir, existingFile);
            IHierarchicalContentNode fileNode = rootContent.getNode(relativePath);
            assertEquals(relativePath, fileNode.getRelativePath());
            checkNodeMatchesFile(fileNode, existingFile);
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testIsChecksumCRC32Precalculated()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        final List<File> existingFiles =
                Arrays.asList(file1, file2, subDir, subFile1, subFile2, subFile3, subSubFile);

        context.checking(new Expectations()
            {
                {
                    for (File existingFile : existingFiles)
                    {
                        one(hierarchicalContentFactory).asHierarchicalContentNode(rootContent,
                                existingFile);
                        will(returnValue(createDummyFileBasedNode(rootDir, existingFile)));
                    }
                }
            });
        for (File existingFile : existingFiles)
        {
            String relativePath = FileUtilities.getRelativeFilePath(rootDir, existingFile);
            IHierarchicalContentNode fileNode = rootContent.getNode(relativePath);
            assertFalse(fileNode.isChecksumCRC32Precalculated());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeInsideHDF5Container() throws IOExceptionUnchecked,
            UnsupportedOperationException, IOException
    {
        // NOTE: this test depends on HDF5Container and HierarchicalStructureDuplicatorFileToHdf5

        // create HDF5 container with subDir contents
        final File subContainerDir = new File(rootDir, "subDir.h5");
        createHDF5Container(subContainerDir, subDir, false);

        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateRootNode(rootContent);

        final List<File> subDirFiles = Arrays.asList(subFile1, subFile2, subFile3, subSubFile);
        for (File subDirFile : subDirFiles)
        {
            // get node of subDirFile counterpart from container
            final String relativePath = FileUtilities.getRelativeFilePath(rootDir, subDirFile);
            final String containerRelativePath =
                    getContainerRelativePath(subContainerDir, relativePath, false);
            final IHierarchicalContentNode fileNode = rootContent.getNode(containerRelativePath);
            checkHDF5ContainerFileNodeMatchesFile(fileNode, subDirFile);
            assertEquals(containerRelativePath, fileNode.getRelativePath());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeInsideHDF5ContainerWithOriginalPrepended() throws IOExceptionUnchecked,
            UnsupportedOperationException, IOException
    {
        // NOTE: this test depends on HDF5Container and HierarchicalStructureDuplicatorFileToHdf5

        // create HDF5 container with subDir contents
        final File subContainerDir = new File(rootDir, "subDir.h5");
        createHDF5Container(subContainerDir, subDir, true);

        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateRootNode(rootContent);

        final List<File> subDirFiles = Arrays.asList(subFile1, subFile2, subFile3, subSubFile);
        for (File subDirFile : subDirFiles)
        {
            // get node of subDirFile counterpart from container
            final String relativePath = FileUtilities.getRelativeFilePath(rootDir, subDirFile);
            final String containerRelativePath =
                    getContainerRelativePath(subContainerDir, relativePath, true);
            final IHierarchicalContentNode fileNode = rootContent.getNode(containerRelativePath);
            checkHDF5ContainerFileNodeMatchesFile(fileNode, subDirFile);
            assertEquals(containerRelativePath, fileNode.getRelativePath());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeOfNonExistingFileFails()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        File fakeFile = new File(rootDir, "fakeFile");
        File subFakeFile = new File(subDir, "subFakeFile");
        File subFakeDir = new File(rootDir, "subFakeDir");
        File subFakeDirFile = new File(subFakeDir, "subFakeDirFile"); // dir doesn't exist too
        final List<File> nonExistingFiles =
                Arrays.asList(fakeFile, subFakeFile, subFakeDir, subFakeDirFile);

        for (File nonExistingFile : nonExistingFiles)
        {
            String relativePath = FileUtilities.getRelativeFilePath(rootDir, nonExistingFile);
            try
            {
                rootContent.getNode(relativePath);
                fail("expected IllegalArgumentException for non existent resource: " + relativePath);
            } catch (IllegalArgumentException ex)
            {
                assertEquals("Resource '" + relativePath + "' does not exist.", ex.getMessage());
            }
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNodeAboveRootNodeFails()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        File otherDir = new File(workingDirectory, "otherDir");
        otherDir.mkdir();
        String[] incorrectPaths =
        { "../", "../../", "../fakeDir", "../otherDir", "subDir/../../otherDir" };
        for (String incorrectPath : incorrectPaths)
        {
            try
            {
                rootContent.getNode(incorrectPath);
                fail("expected IllegalArgumentException for resource above the root directory: "
                        + incorrectPath);
            } catch (IllegalArgumentException ex)
            {
                assertEquals("Resource '" + incorrectPath + "' does not exist.", ex.getMessage());
            }
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithRelativePathPattern()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateRootNode(rootContent);

        // nothing matches
        assertEquals(0, rootContent.listMatchingNodes(".*non-mathching-pattern.*").size());

        // matches in 1 level
        List<IHierarchicalContentNode> matchingNodes1 = rootContent.listMatchingNodes("file.?");
        assertEquals(2, matchingNodes1.size());
        sortNodes(matchingNodes1);
        assertEquals(file1, matchingNodes1.get(0).getFile());
        assertEquals(file2, matchingNodes1.get(1).getFile());

        // matches only in 2 level
        List<IHierarchicalContentNode> matchingNodes2 =
                rootContent.listMatchingNodes("subDir/subFile.?");
        assertEquals(3, matchingNodes2.size());
        sortNodes(matchingNodes2);
        checkNodeMatchesFile(matchingNodes2.get(0), subFile1);
        checkNodeMatchesFile(matchingNodes2.get(1), subFile2);
        checkNodeMatchesFile(matchingNodes2.get(2), subFile3);

        // matches in 3 levels
        List<IHierarchicalContentNode> matchingNodes3 =
                rootContent.listMatchingNodes(".*[fF]ile.?");
        assertEquals(6, matchingNodes3.size());
        sortNodes(matchingNodes3);
        checkNodeMatchesFile(matchingNodes3.get(0), file1);
        checkNodeMatchesFile(matchingNodes3.get(1), file2);
        checkNodeMatchesFile(matchingNodes3.get(2), subFile1);
        checkNodeMatchesFile(matchingNodes3.get(3), subFile2);
        checkNodeMatchesFile(matchingNodes3.get(4), subFile3);
        checkNodeMatchesFile(matchingNodes3.get(5), subSubFile);

        // matches in levels > 1
        List<IHierarchicalContentNode> matchingSubDirFiles =
                rootContent.listMatchingNodes("subDir/.*");
        assertEquals(5, matchingSubDirFiles.size());
        sortNodes(matchingSubDirFiles);
        checkNodeMatchesFile(matchingSubDirFiles.get(0), subSubDir);
        checkNodeMatchesFile(matchingSubDirFiles.get(1), subFile1);
        checkNodeMatchesFile(matchingSubDirFiles.get(2), subFile2);
        checkNodeMatchesFile(matchingSubDirFiles.get(3), subFile3);
        checkNodeMatchesFile(matchingSubDirFiles.get(4), subSubFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithStartingPath()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);
        prepareCreateNode(rootContent, subDir);

        List<IHierarchicalContentNode> matchingNodes =
                rootContent.listMatchingNodes("subDir", ".*[fF]ile.*");
        assertEquals(4, matchingNodes.size());
        sortNodes(matchingNodes);
        checkNodeMatchesFile(matchingNodes.get(0), subFile1);
        checkNodeMatchesFile(matchingNodes.get(1), subFile2);
        checkNodeMatchesFile(matchingNodes.get(2), subFile3);
        checkNodeMatchesFile(matchingNodes.get(3), subSubFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithStartingPathInsideHDF5Container()
            throws IOExceptionUnchecked, UnsupportedOperationException, IOException
    {
        // create HDF5 container with subDir contents
        final File subContainerDir = new File(rootDir, "subDir.h5");
        createHDF5Container(subContainerDir, subDir, false);

        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        final String relativePath = FileUtilities.getRelativeFilePath(rootDir, subSubDir);
        final String containerRelativePath =
                getContainerRelativePath(subContainerDir, relativePath, false);
        final List<IHierarchicalContentNode> matchingNodes =
                rootContent.listMatchingNodes(containerRelativePath, ".*[fF]ile.*");
        assertEquals(1, matchingNodes.size());
        checkHDF5ContainerFileNodeMatchesFile(matchingNodes.get(0), subSubFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithStartingPathInsideHDF5ContainerWithOriginalPrepended()
            throws IOExceptionUnchecked, UnsupportedOperationException, IOException
    {
        // create HDF5 container with subDir contents
        final File subContainerDir = new File(rootDir, "subDir.h5");
        createHDF5Container(subContainerDir, subDir, true);

        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        final String relativePath = FileUtilities.getRelativeFilePath(rootDir, subSubDir);
        final String containerRelativePath =
                getContainerRelativePath(subContainerDir, relativePath, true);
        final List<IHierarchicalContentNode> matchingNodes =
                rootContent.listMatchingNodes(containerRelativePath, ".*[fF]ile.*");
        assertEquals(1, matchingNodes.size());
        checkHDF5ContainerFileNodeMatchesFile(matchingNodes.get(0), subSubFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testListMatchingNodesWithFakeStartingPathFails()
    {
        final DefaultFileBasedHierarchicalContent rootContent = createContent(rootDir);

        final String fakePath = "fake/path";
        try
        {
            rootContent.listMatchingNodes(fakePath, "some-pattern");
            fail("IllegalArgumentException expected for not existend resource: " + fakePath);
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Resource '" + fakePath + "' does not exist.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    private void prepareCreateNode(final DefaultFileBasedHierarchicalContent rootContent,
            final File file)
    {
        context.checking(new Expectations()
            {
                {
                    // root node should be created only once even though we access it many times
                    one(hierarchicalContentFactory).asHierarchicalContentNode(rootContent, file);
                    will(returnValue(createDummyFileBasedNode(rootDir, file)));
                }
            });
    }

    private void prepareCreateRootNode(final DefaultFileBasedHierarchicalContent rootContent)
    {
        context.checking(new Expectations()
            {
                {
                    // root node should be created only once even though we access it many times
                    one(hierarchicalContentFactory).asHierarchicalContentNode(rootContent, rootDir);
                    will(returnValue(createDummyFileBasedRootNode(rootDir)));
                }
            });
    }

    private static String METHOD_NOT_IMPLEMENTED = "method not implemented in dummy node";

    private static void checkNodeMatchesFile(IHierarchicalContentNode node, File expectedFile)
    {
        assertEquals(expectedFile, node.getFile());
    }

    private static void sortNodes(List<IHierarchicalContentNode> nodes)
    {
        HierarchicalContentUtils.sortNodes(nodes);
    }

    private static void checkHDF5ContainerFileNodeMatchesFile(IHierarchicalContentNode fileNode,
            File expectedFile) throws IOExceptionUnchecked, UnsupportedOperationException,
            IOException
    {
        // sanity checks
        assertTrue(fileNode.exists());
        assertFalse(fileNode.isDirectory());
        // direct file access is not possible
        try
        {
            fileNode.getFile();
            fail("Expected UnsupportedOperationException for file access of HDF5 container file node");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals("This is not a normal file node.", ex.getMessage());
        }
        // file info access
        assertEquals("File: " + expectedFile, expectedFile.getName(), fileNode.getName());
        assertEquals("File: " + expectedFile, expectedFile.length(), fileNode.getFileLength());
        long expectedChecksum = IOUtilities.getChecksumCRC32(new FileInputStream(expectedFile));
        assertEquals("File: " + expectedFile, expectedChecksum, fileNode.getChecksumCRC32());
        assertTrue("File: " + expectedFile,
                (fileNode.getLastModified() / 1000) >= (expectedFile.lastModified() / 1000));
        assertTrue("File: " + expectedFile,
                (fileNode.getLastModified() / 1000) - (expectedFile.lastModified() / 1000) <= 2);

        final String expectedFileData = expectedFile.getName() + " data";
        // check random access to file content
        IRandomAccessFile fileContent = fileNode.getFileContent();
        assertEquals(expectedFileData, fileContent.readLine());
        assertEquals(null, fileContent.readLine());
        fileContent.seek(expectedFile.getName().length());
        assertEquals(" data", fileContent.readLine());
        // check sequential input stream read
        assertEquals("[" + expectedFileData + "]", IOUtils.readLines(fileNode.getInputStream())
                .toString());
    }

    /** creates HDF5 container file with <var>containedDir</var> content */
    private static void createHDF5Container(File containerFile, File containedDir,
            boolean prependOriginal)
    {
        HDF5Container container = new HDF5Container(containerFile);
        final DuplicatorWriterClient writerClient =
                (prependOriginal) ? new HierarchicalStructureDuplicatorFileToHDF5.DuplicatorWriterClient(
                        containedDir, "/original/")
                        : new HierarchicalStructureDuplicatorFileToHDF5.DuplicatorWriterClient(
                                containedDir);
        container.runWriterClient(true, writerClient);
    }

    private String getContainerRelativePath(final File subContainerDir, final String relativePath,
            boolean prependOriginal)
    {
        return (prependOriginal) ? relativePath.replace(subDir.getName(), subContainerDir.getName()
                + "/original") : relativePath.replace(subDir.getName(), subContainerDir.getName());
    }

    private static IHierarchicalContentNode createDummyFileBasedRootNode(final File root)
    {
        return createDummyFileBasedNode(root, root);
    }

    private static IHierarchicalContentNode createDummyFileBasedNode(final File root,
            final File file)
    {
        return new IHierarchicalContentNode()
            {

                @Override
                public boolean isDirectory()
                {
                    return file.isDirectory();
                }

                @Override
                public String getRelativePath()
                {
                    return FileUtilities.getRelativeFilePath(root, file);
                }

                @Override
                public String getParentRelativePath()
                {
                    return FileUtilities.getParentRelativePath(getRelativePath());
                }

                @Override
                public String getName()
                {
                    return file.getName();
                }

                @Override
                public File getFile() throws UnsupportedOperationException
                {
                    return file;
                }

                @Override
                public File tryGetFile()
                {
                    return file;
                }

                @Override
                public List<IHierarchicalContentNode> getChildNodes()
                        throws UnsupportedOperationException
                {
                    File[] files = file.listFiles();
                    List<IHierarchicalContentNode> result =
                            new ArrayList<IHierarchicalContentNode>();
                    if (files != null)
                    {
                        for (File aFile : files)
                        {
                            result.add(createDummyFileBasedNode(root, aFile));
                        }
                    }
                    return result;
                }

                @Override
                public boolean exists()
                {
                    return file.exists();
                }

                @Override
                public long getLastModified()
                {
                    return file.lastModified();
                }

                @Override
                public long getFileLength() throws UnsupportedOperationException
                {
                    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
                }

                @Override
                public IRandomAccessFile getFileContent() throws UnsupportedOperationException,
                        IOExceptionUnchecked
                {
                    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
                }

                @Override
                public String getChecksum() throws UnsupportedOperationException
                {
                    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
                }

                @Override
                public int getChecksumCRC32() throws UnsupportedOperationException
                {
                    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
                }

                @Override
                public InputStream getInputStream() throws UnsupportedOperationException,
                        IOExceptionUnchecked
                {
                    throw new UnsupportedOperationException(METHOD_NOT_IMPLEMENTED);
                }

                @Override
                public boolean isChecksumCRC32Precalculated()
                {
                    return false;
                }
            };
    }

}
