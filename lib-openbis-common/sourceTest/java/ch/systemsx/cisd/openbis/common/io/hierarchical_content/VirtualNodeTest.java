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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.base.io.RandomAccessFileImpl;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeListMerger;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeMergerFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.VirtualNode;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Unit tests for {@link VirtualNode}
 * 
 * @author Piotr Buczek
 */
public class VirtualNodeTest extends AbstractFileSystemTestCase
{

    private List<IHierarchicalContentNode> nodes; // real nodes

    // mocks

    private Mockery context;

    private IVirtualNodeMergerFactory mergerFactory;

    private IVirtualNodeListMerger nodeListMerger;

    private IHierarchicalContentNode node1;

    private IHierarchicalContentNode node2;

    private IHierarchicalContentNode node3;

    private IHierarchicalContentNode mergedNode;

    private File dummyFile;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();

        mergerFactory = context.mock(IVirtualNodeMergerFactory.class);
        nodeListMerger = context.mock(IVirtualNodeListMerger.class);

        node1 = context.mock(IHierarchicalContentNode.class, "node1");
        node2 = context.mock(IHierarchicalContentNode.class, "node2");
        node3 = context.mock(IHierarchicalContentNode.class, "node3");
        mergedNode = context.mock(IHierarchicalContentNode.class, "mergedNode");

        nodes = Arrays.asList(node1, node2, node3);

        dummyFile = new File(workingDirectory, "mergedFile");
        dummyFile.createNewFile();
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    //
    // tests mocking IVirtualNodeMergerFactory
    //

    private IHierarchicalContentNode createVirtualNode(List<IHierarchicalContentNode> nodeList)
    {
        return new VirtualNode(mergerFactory, nodeList);
    }

    private IHierarchicalContentNode createVirtualNode()
    {
        return createVirtualNode(nodes);
    }

    @Test
    public void testFailWithNullOrEmptyNodes()
    {
        try
        {
            createVirtualNode(null);
            fail("Expected AssertionError");
        } catch (AssertionError ex)
        {
            assertEquals("Undefined nodes.", ex.getMessage());
        }

        try
        {
            createVirtualNode(new ArrayList<IHierarchicalContentNode>());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Resource doesn't exist.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testEqualsAndHashCode()
    {
        IHierarchicalContentNode virtualNode = createVirtualNode();
        IHierarchicalContentNode virtualNodeWithSameComponents = createVirtualNode();
        IHierarchicalContentNode virtualNodeWithSameComponents2 =
                createVirtualNode(new ArrayList<IHierarchicalContentNode>(nodes));
        assertEquals(virtualNode, virtualNodeWithSameComponents);
        assertEquals(virtualNode, virtualNodeWithSameComponents2);
        assertEquals(virtualNode.hashCode(), virtualNodeWithSameComponents.hashCode());
        assertEquals(virtualNode.hashCode(), virtualNodeWithSameComponents2.hashCode());

        List<IHierarchicalContentNode> subNodes = Arrays.asList(node1, node2);
        IHierarchicalContentNode virtualNodeSubComponents = createVirtualNode(subNodes);
        assertFalse(virtualNode.equals(virtualNodeSubComponents));
        assertFalse(virtualNode.hashCode() == virtualNodeSubComponents.hashCode());

        List<IHierarchicalContentNode> reorderedNodes = Arrays.asList(node1, node3, node2);
        IHierarchicalContentNode virtualNodeReorderedComponents = createVirtualNode(reorderedNodes);
        assertFalse(virtualNode.equals(virtualNodeReorderedComponents));
        assertFalse(virtualNode.hashCode() == virtualNodeReorderedComponents.hashCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testGetName()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final String nodeName = "some name";

        context.checking(new Expectations()
            {
                {
                    // first nodes name is taken
                    one(node1).getName();
                    will(returnValue(nodeName));
                }
            });

        String virtualName = virtualNode.getName();
        assertEquals(nodeName, virtualName);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetRelativePath()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final String relativePath = "relative/path";

        context.checking(new Expectations()
            {
                {
                    // first nodes path is taken
                    one(node1).getRelativePath();
                    will(returnValue(relativePath));
                }
            });

        String virtualRelativePath = virtualNode.getRelativePath();
        assertEquals(relativePath, virtualRelativePath);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetParentRelativePath()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final String parentRelativePath = "parent/relative/path";

        context.checking(new Expectations()
            {
                {
                    // first nodes path is taken
                    one(node1).getParentRelativePath();
                    will(returnValue(parentRelativePath));
                }
            });

        String virtualParentRelativePath = virtualNode.getParentRelativePath();
        assertEquals(parentRelativePath, virtualParentRelativePath);

        context.assertIsSatisfied();
    }

    @Test
    public void testExists()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        // contract: at least one node needs to exist for virtual node to exist

        // 1st case: 2nd out of 3 nodes exist, 3rd node is not asked at all
        context.checking(new Expectations()
            {
                {
                    one(node1).exists();
                    will(returnValue(false));

                    one(node2).exists();
                    will(returnValue(true));
                }
            });
        assertTrue(virtualNode.exists());

        prepareAllNodesNotExist();
        assertFalse(virtualNode.exists());

        context.assertIsSatisfied();
    }

    @Test
    public void testIsDirectory()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        context.checking(new Expectations()
            {
                {
                    // first node is asked twice giving different answer
                    one(node1).isDirectory();
                    will(returnValue(true));

                    one(node1).isDirectory();
                    will(returnValue(false));
                }
            });

        // check twice with different answer from mocked node
        assertTrue(virtualNode.isDirectory());
        assertFalse(virtualNode.isDirectory());

        context.assertIsSatisfied();
    }

    @Test
    public void testLastModified()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();
        context.checking(new Expectations()
            {
                {
                    one(node1).getLastModified();
                    will(returnValue(42L));
                }
            });

        assertEquals(42, virtualNode.getLastModified());

        context.assertIsSatisfied();
    }

    @Test
    public void testGetChildNodes()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        // contents of these lists is not significant in the test
        final List<IHierarchicalContentNode> children1 = Arrays.asList(node1, node2);
        final List<IHierarchicalContentNode> children2 = Arrays.asList(node3);
        final List<IHierarchicalContentNode> children3 = Arrays.asList();
        final List<IHierarchicalContentNode> mergedChildren = Arrays.asList(mergedNode);

        context.checking(new Expectations()
            {
                {
                    one(mergerFactory).createNodeListMerger();
                    will(returnValue(nodeListMerger));

                    one(node1).getChildNodes();
                    will(returnValue(children1));
                    one(node2).getChildNodes();
                    will(returnValue(children2));
                    one(node3).getChildNodes();
                    will(returnValue(children3));

                    one(nodeListMerger).addNodes(children1);
                    one(nodeListMerger).addNodes(children2);
                    one(nodeListMerger).addNodes(children3);
                    one(nodeListMerger).createMergedNodeList();
                    will(returnValue(mergedChildren));

                }
            });

        List<IHierarchicalContentNode> virtualChildren = virtualNode.getChildNodes();
        assertEquals(mergedChildren, virtualChildren);

        context.assertIsSatisfied();
    }

    //
    // contract: file (and its content) from last non-virtual child that exists is taken
    // NOTE: the order of nodes in VirtualNode is reversed (first becomes last)
    //

    @Test
    public void testGetFile()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        // case: 2nd out of 3 nodes exist, 3rd node is not asked at all
        context.checking(new Expectations()
            {
                {
                    one(node1).exists();
                    will(returnValue(false));

                    one(node2).exists();
                    will(returnValue(true));

                    one(node2).getFile();
                    will(returnValue(dummyFile));
                }
            });

        File virtualFile = virtualNode.getFile();
        assertSame(dummyFile, virtualFile);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetNormalFileLength()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final long mergedFileSize = 100;

        // case: 2nd out of 3 nodes exist, 3rd node is not asked at all
        // take lenght of the 2nd file
        context.checking(new Expectations()
            {
                {
                    one(node1).isDirectory();
                    will(returnValue(false)); // normal file

                    one(node1).exists();
                    will(returnValue(false));

                    one(node2).exists();
                    will(returnValue(true));

                    one(node2).getFileLength();
                    will(returnValue(mergedFileSize));
                }
            });

        long virtualLength = virtualNode.getFileLength();
        assertSame(mergedFileSize, virtualLength);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetDirectoryFileLength()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final long nodeLength1 = 11;
        final long nodeLength2 = 22;
        final long nodeLength3 = 33;

        context.checking(new Expectations()
            {
                {
                    one(node1).isDirectory();
                    will(returnValue(true)); // directory file

                    one(node1).getFileLength();
                    will(returnValue(nodeLength1));

                    one(node2).getFileLength();
                    will(returnValue(nodeLength2));

                    one(node3).getFileLength();
                    will(returnValue(nodeLength3));
                }
            });

        long virtualLength = virtualNode.getFileLength();
        // contract: for directories return estimated length == sum of all node lengths
        assertSame(nodeLength1 + nodeLength2 + nodeLength3, virtualLength);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetInputStream() throws IOException
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final InputStream dummyInputStream = new FileInputStream(dummyFile);

        // case: 2nd out of 3 nodes exist, 3rd node is not asked at all
        context.checking(new Expectations()
            {
                {
                    one(node1).exists();
                    will(returnValue(false));

                    one(node2).exists();
                    will(returnValue(true));

                    one(node2).getInputStream();
                    will(returnValue(dummyInputStream));
                }
            });

        InputStream virtualInputStream = virtualNode.getInputStream();
        assertSame(dummyInputStream, virtualInputStream);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetFileContent() throws IOException
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        final IRandomAccessFile dummyRandomAccessFile = new RandomAccessFileImpl(dummyFile, "r");

        // case: 2nd out of 3 nodes exist, 3rd node is not asked at all
        context.checking(new Expectations()
            {
                {
                    one(node1).exists();
                    will(returnValue(false));

                    one(node2).exists();
                    will(returnValue(true));

                    one(node2).getFileContent();
                    will(returnValue(dummyRandomAccessFile));
                }
            });

        IRandomAccessFile virtualRandomAccessFile = virtualNode.getFileContent();
        assertSame(dummyRandomAccessFile, virtualRandomAccessFile);

        context.assertIsSatisfied();
    }

    //
    // contract: if all nodes don't exist an exception should be thrown when one tries to access
    // file or its content
    //

    @Test
    public void testGetFileFailsWithResourceUnavailable()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        prepareAllNodesNotExist();
        expectResouceUnavailableException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    virtualNode.getFile();
                }
            });

        context.assertIsSatisfied();
    }

    @Test
    public void testGetFileContentFailsWithResourceUnavailable()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        prepareAllNodesNotExist();
        expectResouceUnavailableException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    virtualNode.getFileContent();
                }
            });

        context.assertIsSatisfied();
    }

    @Test
    public void testGetInputStreamFailsWithResourceUnavailable()
    {
        final IHierarchicalContentNode virtualNode = createVirtualNode();

        prepareAllNodesNotExist();
        expectResouceUnavailableException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    virtualNode.getInputStream();
                }
            });

        context.assertIsSatisfied();
    }

    //
    // helper functions
    //

    private void prepareAllNodesNotExist()
    {
        context.checking(new Expectations()
            {
                {
                    for (IHierarchicalContentNode node : nodes)
                    {
                        one(node).exists();
                        will(returnValue(false));
                    }
                }
            });
    }

    private static void expectResouceUnavailableException(final IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ex)
        {
            assertEquals("Resource is currently unavailable. It might be in an archive.",
                    ex.getMessage());
        }
    }

}
