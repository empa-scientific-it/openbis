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
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Tests for {@link DefaultFileBasedHierarchicalContentFactory}
 * 
 * @author Piotr Buczek
 */
public class DefaultFileBasedHierarchicalContentFactoryTest extends AbstractFileSystemTestCase
{

    private File rootDir;

    private IHierarchicalContentFactory factory;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        factory = new DefaultFileBasedHierarchicalContentFactory();

        rootDir = new File(workingDirectory, "rootDir");
        rootDir.mkdirs();
    }

    @Test
    public void testCreateHierarchicalContent()
    {
        final IHierarchicalContent rootContent = createRootContent();

        assertEquals(defaultRootContentAsString(), rootContent.toString());
    }

    @Test
    public void testCreateFileNode()
    {
        final IHierarchicalContent rootContent = createRootContent();

        final File aFile = new File(rootDir, "aFile");
        IHierarchicalContentNode fileNode = factory.asHierarchicalContentNode(rootContent, aFile);
        assertEquals("DefaultFileBasedHierarchicalContentNode [root="
                + defaultRootContentAsString() + ", file=" + aFile.toString() + "]",
                fileNode.toString());
    }

    @Test
    public void testCreateHDF5ContainerNode()
    {
        final IHierarchicalContent rootContent = createRootContent();

        final File hdf5Container = createFile("container.h5");
        IHierarchicalContentNode hdf5ContainerNode =
                factory.asHierarchicalContentNode(rootContent, hdf5Container);
        assertEquals("HDF5ContainerBasedHierarchicalContentNode [root="
                + defaultRootContentAsString() + ", container=" + hdf5Container.toString() + "]",
                hdf5ContainerNode.toString());

        final File hdf5Archive = createFile("container.h5ar");
        IHierarchicalContentNode hdf5ArchiveNode =
                factory.asHierarchicalContentNode(rootContent, hdf5Archive);
        assertEquals("HDF5ContainerBasedHierarchicalContentNode [root="
                + defaultRootContentAsString() + ", container=" + hdf5Archive.toString() + "]",
                hdf5ArchiveNode.toString());
    }

    private IHierarchicalContent createRootContent()
    {
        return factory.asHierarchicalContent(rootDir, null);
    }

    private String defaultRootContentAsString()
    {
        return "DefaultFileBasedHierarchicalContent [root=" + rootDir.toString() + "]";
    }

    private File createFile(String name)
    {
        File result = new File(rootDir, name);
        try
        {
            result.createNewFile();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return result;
    }
}
