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
import java.io.InputStream;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Tests for {@link AbstractHierarchicalContentNode}
 * 
 * @author Piotr Buczek
 */
public class AbstractHierarchicalContentNodeTest extends AssertJUnit
{
    @Test
    public void testFileOperationsOnFile()
    {
        final IHierarchicalContentNode fileNode = createDummyFileNode();

        // check only that no exception is thrown
        fileNode.getFileLength();
        fileNode.getChecksumCRC32();
        fileNode.getFileContent();
        fileNode.getInputStream();
    }

    @Test
    public void testDirectoryOperationsFailOnFile()
    {
        final IHierarchicalContentNode fileNode = createDummyFileNode();

        assertUnsupportedDirectoryOperationOnAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    fileNode.getChildNodes();
                }
            });
    }

    @Test
    public void testDirectoryOperationsOnDirectory()
    {
        final IHierarchicalContentNode dirNode = createDummyDirectoryNode();

        // check only that no exception is thrown
        dirNode.getChildNodes();
    }

    @Test
    public void testFileOperationsFailOnDirectory()
    {
        final IHierarchicalContentNode dirNode = createDummyDirectoryNode();

        assertUnsupportedFileOperationOnAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dirNode.getChecksumCRC32();
                }
            });
        assertUnsupportedFileOperationOnAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dirNode.getFileContent();
                }
            });
        assertUnsupportedFileOperationOnAction(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    dirNode.getInputStream();
                }
            });
    }

    private void assertUnsupportedFileOperationOnAction(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(AbstractHierarchicalContentNode.OPERATION_NOT_SUPPORTED_FOR_A_DIRECTORY,
                    ex.getMessage());
        }
    }

    private void assertUnsupportedDirectoryOperationOnAction(IDelegatedAction action)
    {
        try
        {
            action.execute();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex)
        {
            assertEquals(AbstractHierarchicalContentNode.OPERATION_SUPPORTED_ONLY_FOR_A_DIRECTORY,
                    ex.getMessage());
        }
    }

    private IHierarchicalContentNode createDummyDirectoryNode()
    {
        return new AbstractHierarchicalContentNode()
            {

                @Override
                public boolean isDirectory()
                {
                    return true;
                }

                @Override
                public String doGetRelativePath()
                {
                    return "";
                }

                @Override
                public String getName()
                {
                    return null;
                }

                @Override
                public File getFile() throws UnsupportedOperationException
                {
                    return null;
                }

                @Override
                public boolean exists()
                {
                    return false;
                }

                @Override
                public long getLastModified()
                {
                    return 0;
                }

                @Override
                protected InputStream doGetInputStream()
                {
                    return null;
                }

                @Override
                protected long doGetFileLength()
                {
                    return 0;
                }

                @Override
                protected int doGetChecksumCRC32()
                {
                    return 0;
                }

                @Override
                protected IRandomAccessFile doGetFileContent()
                {
                    return null;
                }

                @Override
                protected List<IHierarchicalContentNode> doGetChildNodes()
                {
                    return null;
                }

                @Override
                public File tryGetFile()
                {
                    return null;
                }

                @Override
                public boolean isChecksumCRC32Precalculated()
                {
                    return false;
                }
            };
    }

    private IHierarchicalContentNode createDummyFileNode()
    {
        return new AbstractHierarchicalContentNode()
            {

                @Override
                public boolean isDirectory()
                {
                    return false;
                }

                @Override
                public String doGetRelativePath()
                {
                    return "";
                }

                @Override
                public String getName()
                {
                    return null;
                }

                @Override
                public File getFile() throws UnsupportedOperationException
                {
                    return null;
                }

                @Override
                public boolean exists()
                {
                    return false;
                }

                @Override
                public long getLastModified()
                {
                    return 0;
                }

                @Override
                protected InputStream doGetInputStream()
                {
                    return null;
                }

                @Override
                protected long doGetFileLength()
                {
                    return 0;
                }

                @Override
                protected int doGetChecksumCRC32()
                {
                    return 0;
                }

                @Override
                protected IRandomAccessFile doGetFileContent()
                {
                    return null;
                }

                @Override
                protected List<IHierarchicalContentNode> doGetChildNodes()
                {
                    return null;
                }

                @Override
                public File tryGetFile()
                {
                    return null;
                }

                @Override
                public boolean isChecksumCRC32Precalculated()
                {
                    return false;
                }
            };
    }

}
