/*
 * Copyright 2012 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * @author Franz-Josef Elmer
 */
public class HDF5ContainerBasedHierarchicalContentNodeTest extends AssertJUnit
{
    private static final File TEST_DATA_FOLDER = new File(
            "resource/test-data/HDF5ContainerBasedHierarchicalContentNodeTest");

    @Test
    public void testH5File()
    {
        IHierarchicalContent content =
                new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(
                        TEST_DATA_FOLDER, null);
        IHierarchicalContentNode node = content.getNode("thumbnails.h5");

        assertH5ExampleContent(node);
    }

    public static void assertH5ExampleContent(IHierarchicalContentNode node)
    {
        List<IHierarchicalContentNode> nodes = getSortedChildren(node);

        assertEquals("3361fd20 24242 PLATE1_A01_01_Cy3.png\n" +
                "609f3183 29353 PLATE1_A01_01_DAPI.png\n" +
                "b68f97cf 27211 PLATE1_A01_01_GFP.png\n" +
                "b312b087 31570 PLATE1_A01_02_Cy3.png\n" +
                "e7082b23 28267 PLATE1_A01_02_DAPI.png\n" +
                "fb7f320e 26972 PLATE1_A01_02_GFP.png\n" +
                "a97cff4e 28916 PLATE1_A01_03_Cy3.png\n" +
                "6f0abf6f 30079 PLATE1_A01_03_DAPI.png\n" +
                "5ba6ae39 28072 PLATE1_A01_03_GFP.png\n" +
                "e2c7c34f 28279 PLATE1_A01_04_Cy3.png\n" +
                "1bf73b61 22246 PLATE1_A01_04_DAPI.png\n" +
                "58e14da9 22227 PLATE1_A01_04_GFP.png\n" +
                "d367dd9d 34420 PLATE1_A01_05_Cy3.png\n" +
                "15e1f3b0 28070 PLATE1_A01_05_DAPI.png\n" +
                "34bcde32 27185 PLATE1_A01_05_GFP.png\n" +
                "f8d4cfc7 26367 PLATE1_A01_06_Cy3.png\n" +
                "aeb12b1a 25086 PLATE1_A01_06_DAPI.png\n" +
                "ced4332a 22199 PLATE1_A01_06_GFP.png", getNamesChecksumsAndSizes(nodes));
        assertEquals(537641, node.getFileLength());
        assertEquals(-2098219814, node.getChecksumCRC32());
    }

    @Test
    public void testLegacyH5arFile()
    {
        IHierarchicalContent content =
                new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(
                        TEST_DATA_FOLDER, null);
        IHierarchicalContentNode node = content.getNode("thumbnails_old.h5ar");
        assertH5arExampleContent(node);
    }

    @Test
    public void testH5arFile()
    {
        IHierarchicalContent content =
                new DefaultFileBasedHierarchicalContentFactory().asHierarchicalContent(
                        TEST_DATA_FOLDER, null);
        IHierarchicalContentNode node = content.getNode("thumbnails_new.h5ar");
        assertH5arExampleContent(node);
    }

    private void assertH5arExampleContent(IHierarchicalContentNode node)
    {
        List<IHierarchicalContentNode> nodes = getSortedChildren(node);

        assertEquals("e15e2b61 36424 wA1_d1-1_cCy3.png\n" +
                "6e584065 43322 wA1_d1-1_cDAPI.png\n" +
                "9f5d677c 40505 wA1_d1-1_cGFP.png\n" +
                "3456d921 43096 wA1_d1-2_cCy3.png\n" +
                "771adbb5 32444 wA1_d1-2_cDAPI.png\n" +
                "e5e10f4c 32961 wA1_d1-2_cGFP.png\n" +
                "3c137a44 48062 wA1_d2-1_cCy3.png\n" +
                "e301bfc9 41913 wA1_d2-1_cDAPI.png\n" +
                "9286ab95 40327 wA1_d2-1_cGFP.png\n" +
                "4ec4697d 51564 wA1_d2-2_cCy3.png\n" +
                "92563064 42019 wA1_d2-2_cDAPI.png\n" +
                "c21f2b93 41219 wA1_d2-2_cGFP.png\n" +
                "7b32fe5a 43449 wA1_d3-1_cCy3.png\n" +
                "cca44db2 45041 wA1_d3-1_cDAPI.png\n" +
                "2a1a67d 42168 wA1_d3-1_cGFP.png\n" +
                "9e2b87e5 39980 wA1_d3-2_cCy3.png\n" +
                "38aea6fe 37051 wA1_d3-2_cDAPI.png\n" +
                "bb59344e 32815 wA1_d3-2_cGFP.png", getNamesChecksumsAndSizes(nodes));
    }

    private static String getNamesChecksumsAndSizes(List<IHierarchicalContentNode> nodes)
    {
        StringBuilder builder = new StringBuilder();
        for (IHierarchicalContentNode node : nodes)
        {
            if (builder.length() > 0)
            {
                builder.append("\n");
            }
            builder.append(Integer.toHexString(node.getChecksumCRC32()));
            builder.append(" ").append(node.getFileLength());
            builder.append(" ").append(node.getName());
        }
        return builder.toString();
    }

    private static List<IHierarchicalContentNode> getSortedChildren(IHierarchicalContentNode node)
    {
        List<IHierarchicalContentNode> nodes = node.getChildNodes();
        Collections.sort(nodes, new Comparator<IHierarchicalContentNode>()
            {
                @Override
                public int compare(IHierarchicalContentNode n1, IHierarchicalContentNode n2)
                {
                    return n1.getName().compareTo(n2.getName());
                }
            });
        return nodes;
    }

}
