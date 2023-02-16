/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author pkupczyk
 */
public abstract class AbstractPackageBasedHierarchicalContentTest extends AbstractFileSystemTestCase
{

    private static final File TEST_HDF5_CONTAINER = new File(
            "../lib-openbis-common/resource/test-data/HDF5ContainerBasedHierarchicalContentNodeTest/thumbnails.h5");

    protected void init()
    {
    }

    protected abstract IHierarchicalContent createPackage(File packageFile, File dataDir)
            throws Exception;

    @BeforeMethod
    public void beforeMethod()
    {
        init();
    }

    @Test
    public final void test() throws Exception
    {
        IHierarchicalContent content = null;
        try
        {
            File packageFile = new File(workingDirectory, "package");
            File dataDir = prepareDataForPackage();
            content = createPackage(packageFile, dataDir);
            assertDataInPackage(content);
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }

    protected File prepareDataForPackage() throws Exception
    {
        File dataRoot = new File(workingDirectory, "data");
        File alpha = new File(dataRoot, "alpha");
        File beta = new File(alpha, "beta");
        beta.mkdirs();
        new File(alpha, "empty-dir").mkdir();
        FileUtilities.writeToFile(new File(beta, "hello.txt"), "hello world!");
        FileUtilities.writeToFile(new File(alpha, "read.me"), "don't read me!");
        FileUtilities.writeToFile(new File(dataRoot, "change-log.txt"), "nothing really changed.");
        File originalHdf5ContainerFile = new File(dataRoot, "my-container.h5");
        FileUtils.copyFile(TEST_HDF5_CONTAINER, originalHdf5ContainerFile, false);
        return dataRoot;
    }

    protected void assertDataInPackage(IHierarchicalContent content) throws Exception
    {
        IHierarchicalContentNode rootNode = content.getRootNode();
        assertDirectoryNode("", "", rootNode);
        List<IHierarchicalContentNode> childNodes = rootNode.getChildNodes();
        assertDirectoryNode("alpha", "alpha", childNodes.get(0));
        List<IHierarchicalContentNode> grandChildNodes = childNodes.get(0).getChildNodes();
        assertDirectoryNode("alpha/beta", "beta", grandChildNodes.get(0));
        assertDirectoryNode("alpha/empty-dir", "empty-dir", grandChildNodes.get(1));
        List<IHierarchicalContentNode> grandGrandChildNodes =
                grandChildNodes.get(0).getChildNodes();
        assertFileNode("alpha/beta/hello.txt", "hello.txt", "hello world!",
                grandGrandChildNodes.get(0));
        assertEquals(1, grandGrandChildNodes.size());
        assertFileNode("alpha/read.me", "read.me", "don't read me!", grandChildNodes.get(2));
        assertEquals(3, grandChildNodes.size());
        assertFileNode("change-log.txt", "change-log.txt", "nothing really changed.",
                childNodes.get(1));
        IHierarchicalContentNode hdf5Node = childNodes.get(2);
        assertEquals(true, hdf5Node.isDirectory());
        assertEquals(-2098219814, hdf5Node.getChecksumCRC32());
        assertEquals(537641, hdf5Node.getFileLength());

        // allow 2 second difference as de.schlichtherle.util.zip.ZipEntry.setTime()
        // method may cause such a side effect due to some OS compatibility conversion
        File dataRoot = new File(workingDirectory, "data");
        File originalHdf5ContainerFile = new File(dataRoot, "my-container.h5");
        assertTrue(Math.abs(
                originalHdf5ContainerFile.lastModified() - hdf5Node.getLastModified()) <= 2000);
    }

    private void assertDirectoryNode(String expectedPath, String expectedName,
            IHierarchicalContentNode node)
    {
        assertPathAndName(expectedPath, expectedName, node);
        assertEquals(true, node.isDirectory());
    }

    private void assertFileNode(String expectedPath, String expectedName, String expectedContent,
            IHierarchicalContentNode node) throws Exception
    {
        assertPathAndName(expectedPath, expectedName, node);
        assertEquals(false, node.isDirectory());
        assertEquals(expectedContent.length(), node.getFileLength());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOUtils.copy(node.getInputStream(), output);
        assertEquals(expectedContent, output.toString());
        CRC32 checksum = new CRC32();
        CheckedInputStream in =
                new CheckedInputStream(new ByteArrayInputStream(expectedContent.getBytes()),
                        checksum);
        IOUtils.copy(in, new NullOutputStream());
        assertEquals(checksum.getValue(), node.getChecksumCRC32());

    }

    private void assertPathAndName(String expectedPath, String expectedName,
            IHierarchicalContentNode node)
    {
        assertEquals(expectedPath, node.getRelativePath());
        assertEquals(expectedName, node.getName());
        String parentRelativePath = node.getParentRelativePath();
        if (expectedPath.equals(""))
        {
            assertEquals(null, parentRelativePath);
        } else
        {
            assertEquals(expectedPath,
                    (StringUtils.isBlank(parentRelativePath) ?
                            "" :
                            parentRelativePath + "/") + node.getName());
        }
    }

}
