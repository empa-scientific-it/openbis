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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeListMerger;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeMerger;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.IVirtualNodeMergerFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.VirtualNodeListMerger;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.VirtualHierarchicalContent.VirtualNodeMerger;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Unit tests for {@link VirtualNodeMerger} and {@link VirtualNodeListMerger}.
 * 
 * @author Piotr Buczek
 */
public class VirtualNodeMergingTest extends AssertJUnit
{

    // mocks

    private Mockery context;

    private IVirtualNodeMergerFactory mergerFactory;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();

        mergerFactory = context.mock(IVirtualNodeMergerFactory.class);

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

    private IVirtualNodeMerger createNodeMerger()
    {
        return new VirtualNodeMerger(mergerFactory);
    }

    private IVirtualNodeListMerger createNodeListMerger()
    {
        return new VirtualNodeListMerger(mergerFactory);
    }

    private IVirtualNodeMerger createNodeMergerMock(String mockName)
    {
        return context.mock(IVirtualNodeMerger.class, mockName);
    }

    private IHierarchicalContentNode createNodeMock(String mockName)
    {
        return context.mock(IHierarchicalContentNode.class, mockName);
    }

    @Test
    public void testCreateEmptyNodeFails()
    {
        final IVirtualNodeMerger merger = createNodeMerger();

        assertNull(merger.tryCreateMergedNode());

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateNode()
    {
        final IVirtualNodeMerger merger = createNodeMerger();

        merger.addNode(createNodeMock("node1"));
        merger.addNode(createNodeMock("node2"));
        merger.addNode(createNodeMock("node3"));

        IHierarchicalContentNode mergedNode = merger.tryCreateMergedNode();
        // virtual node has the nodes in reversed order to make the first one the most important
        assertEquals("VirtualNode [nodes=[node3, node2, node1]]", mergedNode.toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateNodeList()
    {
        final IVirtualNodeListMerger listMerger = createNodeListMerger();

        // add list1 with 2 nodes (with new relative paths)
        final String path1 = "some/path1";
        final String path2 = "some/path2";
        final IHierarchicalContentNode node1Path1 = createNodeMock("node1Path1");
        final IHierarchicalContentNode node1Path2 = createNodeMock("node1Path2");
        final IVirtualNodeMerger nodeMergerPath1 = createNodeMergerMock("mergerPath1");
        final IVirtualNodeMerger nodeMergerPath2 = createNodeMergerMock("mergerPath2");
        prepareAddNodeWithNewPath(node1Path1, path1, nodeMergerPath1);
        prepareAddNodeWithNewPath(node1Path2, path2, nodeMergerPath2);
        listMerger.addNodes(Arrays.asList(node1Path1, node1Path2));

        // add list2 with 2 nodes with new relative paths
        final String path3 = "some/path3";
        final String path4 = "some/path4";
        final IHierarchicalContentNode node2Path3 = createNodeMock("node2Path3");
        final IHierarchicalContentNode node2Path4 = createNodeMock("node2Path4");
        final IVirtualNodeMerger nodeMergerPath3 = createNodeMergerMock("mergerPath3");
        final IVirtualNodeMerger nodeMergerPath4 = createNodeMergerMock("mergerPath4");
        prepareAddNodeWithNewPath(node2Path3, path3, nodeMergerPath3);
        prepareAddNodeWithNewPath(node2Path4, path4, nodeMergerPath4);
        listMerger.addNodes(Arrays.asList(node2Path3, node2Path4));

        // add list3 with 3 nodes, 2 with same relative paths as in previous lists and 1 new path
        final String path5 = "some/path5";
        final IHierarchicalContentNode node3Path1 = createNodeMock("node3Path1");
        final IHierarchicalContentNode node3Path3 = createNodeMock("node3Path3");
        final IHierarchicalContentNode node3Path5 = createNodeMock("node3Path5");
        final IVirtualNodeMerger nodeMergerPath5 = createNodeMergerMock("mergerPath5");
        // path1 used already in list1
        prepareAddNodeWithExistingPath(node3Path1, path1, nodeMergerPath1);
        // path3 used already in list2
        prepareAddNodeWithExistingPath(node3Path3, path3, nodeMergerPath3);
        // path5 wasn't yet used - new merger should be created
        prepareAddNodeWithNewPath(node3Path5, path5, nodeMergerPath5);
        listMerger.addNodes(Arrays.asList(node3Path1, node3Path3, node3Path5));

        // no work is expected when adding an empty list
        listMerger.addNodes(Collections.<IHierarchicalContentNode> emptyList());

        context.checking(new Expectations()
            {
                {
                    createMergedNode(nodeMergerPath1, "mergedNode1");
                    createMergedNode(nodeMergerPath2, "mergedNode2");
                    createMergedNode(nodeMergerPath3, "mergedNode3");
                    createMergedNode(nodeMergerPath4, "mergedNode4");
                    createMergedNode(nodeMergerPath5, "mergedNode5");
                }

                private void createMergedNode(final IVirtualNodeMerger merger, String nodeName)
                {
                    final IHierarchicalContentNode mergedNode = createNodeMock(nodeName);
                    one(merger).tryCreateMergedNode();
                    will(returnValue(mergedNode));
                }
            });
        List<IHierarchicalContentNode> mergedNode = listMerger.createMergedNodeList();
        assertEquals("[mergedNode1, mergedNode2, mergedNode3, mergedNode4, mergedNode5]",
                mergedNode.toString());

        context.assertIsSatisfied();
    }

    private void prepareAddNodeWithNewPath(final IHierarchicalContentNode node, final String path,
            final IVirtualNodeMerger merger)
    {
        context.checking(new Expectations()
            {
                {
                    one(node).getRelativePath();
                    will(returnValue(path));
                    one(mergerFactory).createNodeMerger();
                    will(returnValue(merger));
                    one(merger).addNode(node);
                }
            });
    }

    private void prepareAddNodeWithExistingPath(final IHierarchicalContentNode node,
            final String path, final IVirtualNodeMerger merger)
    {
        context.checking(new Expectations()
            {
                {
                    one(node).getRelativePath();
                    will(returnValue(path));
                    one(merger).addNode(node);
                }
            });
    }
}
