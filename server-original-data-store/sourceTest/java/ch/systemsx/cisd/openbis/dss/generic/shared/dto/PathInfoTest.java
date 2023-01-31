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

package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.io.File;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.PathInfo;

/**
 * @author Franz-Josef Elmer
 */
public class PathInfoTest extends AbstractFileSystemTestCase
{
    @Test
    public void testWithChecksum()
    {
        File d1 = new File(workingDirectory, "d1");
        d1.mkdirs();
        File d2 = new File(d1, "d2");
        d2.mkdirs();
        FileUtilities.writeToFile(new File(d2, "hello.txt"), "hello world");
        FileUtilities.writeToFile(new File(d2, "table.tsv"), "id\tname\n1\tworld\n");
        File d3 = new File(workingDirectory, "d3");
        d3.mkdirs();
        new File(d3, "d4").mkdirs();
        FileUtilities.writeToFile(new File(d3, "read.me"), "nothing to read");

        PathInfo root = PathInfo.createPathInfo(create(workingDirectory), true, "SHA-256");

        assertEquals(null, root.getParent());
        List<PathInfo> children = root.getChildren();
        PathInfo c0 = children.get(0);
        PathInfo c00 = c0.getChildren().get(0);
        PathInfo c1 = children.get(1);
        check(getClass().getName(), true, 42, null, null, 2, root);
        check("d1", true, 27, null, null, 1, c0);
        check("d2", true, 27, null, null, 2, c00);
        check("hello.txt", false, 11, 222957957, "SHA-256:b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", 0, c00.getChildren().get(0));
        check("table.tsv", false, 16, -1596806677, "SHA-256:cc5456c06a120d753eca4ac8b08046b07c9b45cf2f060075944b78910fa16648", 0, c00.getChildren().get(1));
        check("d3", true, 15, null, null, 2, c1);
        check("d4", true, 0, null, null, 0, c1.getChildren().get(0));
        check("read.me", false, 15, 1246790599, "SHA-256:956700c3e603dcd94bc2e189ac7a07b92e2e5e0f84323f538688e510183c5914", 0, c1.getChildren().get(1));
    }

    @Test
    public void testWithoutChecksum()
    {
        File d1 = new File(workingDirectory, "d1");
        d1.mkdirs();
        FileUtilities.writeToFile(new File(d1, "hello.txt"), "hello world");

        PathInfo root = PathInfo.createPathInfo(create(workingDirectory), false, null);

        assertEquals(null, root.getParent());
        List<PathInfo> children = root.getChildren();
        PathInfo c0 = children.get(0);
        check(getClass().getName(), true, 11, null, null, 1, root);
        check("d1", true, 11, null, null, 1, c0);
        check("hello.txt", false, 11, null, null, 0, c0.getChildren().get(0));
    }

    private void check(String expectedName, boolean expectedIsDirectory, long expectedSize,
            Integer expectedChecksumCRC32, String expectedChecksum, int expectedNumberOfChildren, PathInfo pathInfo)
    {
        assertEquals(expectedName, pathInfo.getFileName());
        assertEquals(expectedIsDirectory, pathInfo.isDirectory());
        assertEquals(expectedSize, pathInfo.getSizeInBytes());
        assertEquals(expectedChecksumCRC32, pathInfo.getChecksumCRC32());
        assertEquals(expectedChecksum, pathInfo.getChecksum());
        List<PathInfo> children = pathInfo.getChildren();
        for (PathInfo child : children)
        {
            assertSame(pathInfo, child.getParent());
        }
        assertEquals(expectedNumberOfChildren, children.size());
        try
        {
            children.add(pathInfo);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ex)
        {
            // ignored
        }
    }

    private IHierarchicalContentNode create(File file)
    {
        DefaultFileBasedHierarchicalContentFactory factory = new DefaultFileBasedHierarchicalContentFactory();
        IHierarchicalContent content =
                factory.asHierarchicalContent(file, IDelegatedAction.DO_NOTHING);
        return factory.asHierarchicalContentNode(content, file);
    }
}
