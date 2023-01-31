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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;

/**
 * Test cases for {@link AutoResolveUtils}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = AutoResolveUtils.class)
public class AutoResolveUtilsTest extends AssertJUnit
{
    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    private Mockery context = new Mockery();

    // f1
    // - f2
    // - - f3
    // - - - e.txt
    private static final File TEST_FOLDER = new File("targets/unit-test/store");

    private static final File EXAMPLE_FOLDER_1 = new File(TEST_FOLDER, "f1");

    private static final File EXAMPLE_FOLDER_2 = new File(EXAMPLE_FOLDER_1, "f2");

    private static final File EXAMPLE_FOLDER_3 = new File(EXAMPLE_FOLDER_2, "f3");

    private static final File EXAMPLE_FILE_TXT = new File(EXAMPLE_FOLDER_3, "e.txt");

    private final IHierarchicalContent MOCK_TEST_FOLDER = context.mock(IHierarchicalContent.class,
            "MOCK_TEST_FOLDER");

    private final IHierarchicalContentNode MOCK_TEST_FOLDER_NODE = context.mock(
            IHierarchicalContentNode.class, "MOCK_TEST_FOLDER_NODE");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FOLDER_1 = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FOLDER_1");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FOLDER_2 = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FOLDER_2");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FOLDER_3 = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FOLDER_3");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FILE_TXT = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FILE_TXT");

    // f4
    // - f5
    // - - f6
    // - - e.abc
    private static final File EXAMPLE_FOLDER_4 = new File(TEST_FOLDER, "f4");

    private static final File EXAMPLE_FOLDER_5 = new File(EXAMPLE_FOLDER_4, "f5");

    private static final File EXAMPLE_FOLDER_6 = new File(EXAMPLE_FOLDER_5, "f6");

    private static final File EXAMPLE_FILE_ABC = new File(EXAMPLE_FOLDER_5, "e.abc");

    private static final File EXAMPLE_FILE_6A = new File(EXAMPLE_FOLDER_6, "a.txt");

    private static final File EXAMPLE_FILE_6B = new File(EXAMPLE_FOLDER_6, "b.jpg");

    private static final String EXAMPLE_FILE_CONTENT = "Hello world!";

    private final IHierarchicalContentNode MOCK_EXAMPLE_FOLDER_4 = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FOLDER_4");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FOLDER_5 = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FOLDER_5");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FOLDER_6 = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FOLDER_6");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FILE_ABC = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FILE_ABC");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FILE_6A = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FILE_6A");

    private final IHierarchicalContentNode MOCK_EXAMPLE_FILE_6B = context.mock(
            IHierarchicalContentNode.class, "MOCK_EXAMPLE_FILE_6B");

    {
        context.checking(new Expectations()
            {
                {
                    /* MOCK_TEST_FOLDER */
                    allowing(MOCK_TEST_FOLDER).getRootNode();
                    will(returnValue(MOCK_TEST_FOLDER_NODE));
                    IHierarchicalContentNode nonExisting =
                            context.mock(IHierarchicalContentNode.class, "NON_EXISTING");
                    allowing(nonExisting).exists();
                    will(returnValue(false));
                    allowing(MOCK_TEST_FOLDER).getNode("f1/f2");
                    will(returnValue(MOCK_EXAMPLE_FOLDER_2));
                    allowing(MOCK_TEST_FOLDER).getNode("/f1/f2");
                    will(returnValue(MOCK_EXAMPLE_FOLDER_2));
                    allowing(MOCK_TEST_FOLDER).getNode("/f1/f2/");
                    will(returnValue(MOCK_EXAMPLE_FOLDER_2));
                    allowing(MOCK_TEST_FOLDER).getNode("f1/f2/f3/e.txt");
                    will(returnValue(MOCK_EXAMPLE_FILE_TXT));
                    allowing(MOCK_TEST_FOLDER).listMatchingNodes(".*");
                    List<IHierarchicalContentNode> matchingNodes =
                            new ArrayList<IHierarchicalContentNode>();
                    matchingNodes.add(MOCK_EXAMPLE_FILE_TXT);
                    matchingNodes.add(MOCK_EXAMPLE_FILE_ABC);
                    matchingNodes.add(MOCK_EXAMPLE_FILE_6A);
                    matchingNodes.add(MOCK_EXAMPLE_FILE_6B);
                    will(returnValue(matchingNodes));

                    allowing(MOCK_TEST_FOLDER).listMatchingNodes("nonexistent/no/no/.*");
                    will(returnValue(new ArrayList<IHierarchicalContentNode>()));

                    allowing(MOCK_TEST_FOLDER).listMatchingNodes(".*\\.abc");
                    List<IHierarchicalContentNode> matchingNodesAbc =
                            new ArrayList<IHierarchicalContentNode>();
                    matchingNodesAbc.add(MOCK_EXAMPLE_FILE_ABC);
                    will(returnValue(matchingNodesAbc));
                    allowing(MOCK_TEST_FOLDER).getNode("f4");
                    will(returnValue(MOCK_EXAMPLE_FOLDER_4));
                    allowing(MOCK_TEST_FOLDER)
                            .listMatchingNodes("f4" + File.separator + ".*\\.abc");
                    will(returnValue(matchingNodesAbc));

                    /* MOCK_TEST_FOLDER_NODE */
                    allowing(MOCK_TEST_FOLDER_NODE).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_TEST_FOLDER_NODE).getChildNodes();
                    List<IHierarchicalContentNode> rootChildren =
                            new ArrayList<IHierarchicalContentNode>();
                    rootChildren.add(MOCK_EXAMPLE_FOLDER_1);
                    rootChildren.add(MOCK_EXAMPLE_FOLDER_4);
                    will(returnValue(rootChildren));
                    allowing(MOCK_TEST_FOLDER_NODE).getRelativePath();
                    will(returnValue(""));

                    /* Example directory 1 */
                    allowing(MOCK_EXAMPLE_FOLDER_1).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_1).getChildNodes();
                    List<IHierarchicalContentNode> children1 =
                            new ArrayList<IHierarchicalContentNode>();
                    children1.add(MOCK_EXAMPLE_FOLDER_2);
                    will(returnValue(children1));

                    /* Example directory 2 */
                    allowing(MOCK_EXAMPLE_FOLDER_2).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_2).getChildNodes();
                    List<IHierarchicalContentNode> children2 =
                            new ArrayList<IHierarchicalContentNode>();
                    children2.add(MOCK_EXAMPLE_FOLDER_3);
                    will(returnValue(children2));
                    allowing(MOCK_EXAMPLE_FOLDER_2).exists();
                    will(returnValue(true));

                    /* Example directory 3 */
                    allowing(MOCK_EXAMPLE_FOLDER_3).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_3).getChildNodes();
                    List<IHierarchicalContentNode> children3 =
                            new ArrayList<IHierarchicalContentNode>();
                    children3.add(MOCK_EXAMPLE_FILE_TXT);
                    will(returnValue(children3));

                    /* Example directory 4 */
                    allowing(MOCK_EXAMPLE_FOLDER_4).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_4).getChildNodes();
                    List<IHierarchicalContentNode> children4 =
                            new ArrayList<IHierarchicalContentNode>();
                    children4.add(MOCK_EXAMPLE_FOLDER_5);
                    will(returnValue(children4));
                    allowing(MOCK_EXAMPLE_FOLDER_4).exists();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_4).getRelativePath();
                    will(returnValue("f4"));

                    /* Example directory 5 */
                    allowing(MOCK_EXAMPLE_FOLDER_5).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_5).getChildNodes();
                    List<IHierarchicalContentNode> children5 =
                            new ArrayList<IHierarchicalContentNode>();
                    children5.add(MOCK_EXAMPLE_FOLDER_6);
                    children5.add(MOCK_EXAMPLE_FILE_ABC);
                    will(returnValue(children5));

                    /* Example directory 6 */
                    allowing(MOCK_EXAMPLE_FOLDER_6).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_6).isDirectory();
                    will(returnValue(true));
                    allowing(MOCK_EXAMPLE_FOLDER_6).getChildNodes();
                    List<IHierarchicalContentNode> children6 =
                            new ArrayList<IHierarchicalContentNode>();
                    children6.add(MOCK_EXAMPLE_FILE_6A);
                    children6.add(MOCK_EXAMPLE_FILE_6B);
                    will(returnValue(children6));

                    /* Example file TXT */
                    allowing(MOCK_EXAMPLE_FILE_TXT).isDirectory();
                    will(returnValue(false));
                    allowing(MOCK_EXAMPLE_FILE_TXT).getRelativePath();
                    will(returnValue("f1/f2/f3/e.txt"));
                    allowing(MOCK_EXAMPLE_FILE_TXT).exists();
                    will(returnValue(true));

                    /* Example file ABC */
                    allowing(MOCK_EXAMPLE_FILE_ABC).isDirectory();
                    will(returnValue(false));
                    allowing(MOCK_EXAMPLE_FILE_ABC).getRelativePath();
                    will(returnValue("f4/f5/e.abc"));

                    /* Example file 6A */
                    allowing(MOCK_EXAMPLE_FILE_6A).isDirectory();
                    will(returnValue(false));
                    allowing(MOCK_EXAMPLE_FILE_ABC).getRelativePath();
                    will(returnValue("f4/f5/f6/a.txt"));

                    /* Example file 6B */
                    allowing(MOCK_EXAMPLE_FILE_6B).isDirectory();
                    will(returnValue(false));
                    allowing(MOCK_EXAMPLE_FILE_ABC).getRelativePath();
                    will(returnValue("f4/f5/f6/b.jpg"));
                }
            });
    }

    @BeforeMethod
    public void setUp()
    {
        TEST_FOLDER.mkdirs();
        EXAMPLE_FOLDER_3.mkdirs();
        FileUtilities.writeToFile(EXAMPLE_FILE_TXT, EXAMPLE_FILE_CONTENT);
        EXAMPLE_FOLDER_5.mkdirs();
        EXAMPLE_FOLDER_6.mkdirs();
        FileUtilities.writeToFile(EXAMPLE_FILE_ABC, EXAMPLE_FILE_CONTENT);
        FileUtilities.writeToFile(EXAMPLE_FILE_6A, EXAMPLE_FILE_CONTENT);
        FileUtilities.writeToFile(EXAMPLE_FILE_6B, EXAMPLE_FILE_CONTENT);

        context = new Mockery();
    }

    @AfterMethod
    public void tearDown()
    {
        FileUtilities.deleteRecursively(TEST_FOLDER);
    }

    @Test
    public void testContinueAutoResolving() throws Exception
    {
        assertTrue(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_1));
        assertTrue(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_2));
        assertFalse(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_3));

        assertTrue(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*abc", EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*\\.jpg", EXAMPLE_FOLDER_3));
        assertTrue(AutoResolveUtils.continueAutoResolving(".*\\.txt", EXAMPLE_FOLDER_3));

        assertTrue(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_4));
        assertTrue(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_4));

        assertFalse(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_5));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_5));

        assertFalse(AutoResolveUtils.continueAutoResolving(null, EXAMPLE_FOLDER_6));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*", EXAMPLE_FOLDER_6));

        /* ----- */
        assertTrue(AutoResolveUtils.continueAutoResolving(null, MOCK_EXAMPLE_FOLDER_1));
        assertTrue(AutoResolveUtils.continueAutoResolving(null, MOCK_EXAMPLE_FOLDER_2));
        assertFalse(AutoResolveUtils.continueAutoResolving(null, MOCK_EXAMPLE_FOLDER_3));

        assertTrue(AutoResolveUtils.continueAutoResolving(".*", MOCK_EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*abc", MOCK_EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*\\.jpg", MOCK_EXAMPLE_FOLDER_3));
        assertTrue(AutoResolveUtils.continueAutoResolving(".*\\.txt", MOCK_EXAMPLE_FOLDER_3));

        assertTrue(AutoResolveUtils.continueAutoResolving(null, MOCK_EXAMPLE_FOLDER_4));
        assertTrue(AutoResolveUtils.continueAutoResolving(".*", MOCK_EXAMPLE_FOLDER_4));

        assertFalse(AutoResolveUtils.continueAutoResolving(null, MOCK_EXAMPLE_FOLDER_5));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*", MOCK_EXAMPLE_FOLDER_5));

        assertFalse(AutoResolveUtils.continueAutoResolving(null, MOCK_EXAMPLE_FOLDER_6));
        assertFalse(AutoResolveUtils.continueAutoResolving(".*", MOCK_EXAMPLE_FOLDER_6));
    }

    @Test
    public void testAcceptFile() throws Exception
    {
        assertTrue(AutoResolveUtils.acceptFile(".*", EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile(".*txt", EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile(".*\\.txt", EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile("txt", EXAMPLE_FILE_TXT));
        // match path
        assertTrue(AutoResolveUtils.acceptFile("f3", EXAMPLE_FILE_TXT));
        // empty pattern
        assertFalse(AutoResolveUtils.acceptFile("", EXAMPLE_FILE_TXT));
        assertFalse(AutoResolveUtils.acceptFile(null, EXAMPLE_FILE_TXT));
        // folder
        assertFalse(AutoResolveUtils.acceptFile(".*", EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.acceptFile(null, EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.acceptFile("f3", EXAMPLE_FOLDER_3));

        /* IHierarchicalContent abstraction */

        assertTrue(AutoResolveUtils.acceptFile(".*", MOCK_EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile(".*txt", MOCK_EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile(".*\\.txt", MOCK_EXAMPLE_FILE_TXT));
        assertTrue(AutoResolveUtils.acceptFile("txt", MOCK_EXAMPLE_FILE_TXT));
        // match path
        assertTrue(AutoResolveUtils.acceptFile("f3", MOCK_EXAMPLE_FILE_TXT));
        // empty pattern
        assertFalse(AutoResolveUtils.acceptFile("", MOCK_EXAMPLE_FILE_TXT));
        assertFalse(AutoResolveUtils.acceptFile(null, MOCK_EXAMPLE_FILE_TXT));
        // folder
        assertFalse(AutoResolveUtils.acceptFile(".*", MOCK_EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.acceptFile(null, MOCK_EXAMPLE_FOLDER_3));
        assertFalse(AutoResolveUtils.acceptFile("f3", MOCK_EXAMPLE_FOLDER_3));
    }

    @Test
    public void testCreateStartingPoint() throws Exception
    {
        // empty path
        assertEquals(TEST_FOLDER, AutoResolveUtils.createStartingPoint(TEST_FOLDER, ""));
        assertEquals(TEST_FOLDER, AutoResolveUtils.createStartingPoint(TEST_FOLDER, null));
        // nonexistent path
        assertEquals(TEST_FOLDER,
                AutoResolveUtils.createStartingPoint(TEST_FOLDER, "nonexistent/no/no"));
        // correct path
        assertEquals(EXAMPLE_FOLDER_2, AutoResolveUtils.createStartingPoint(TEST_FOLDER, "f1/f2"));
        assertEquals(EXAMPLE_FOLDER_2, AutoResolveUtils.createStartingPoint(TEST_FOLDER, "/f1/f2"));
        assertEquals(EXAMPLE_FOLDER_2, AutoResolveUtils.createStartingPoint(TEST_FOLDER, "/f1/f2/"));
        // path is a file
        assertEquals(TEST_FOLDER,
                AutoResolveUtils.createStartingPoint(TEST_FOLDER, "f1/f2/f3/e.txt"));
    }

    @Test
    public void testFindSomeMatchingFiles() throws Exception
    {
        // empty pattern
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(TEST_FOLDER, null, null).isEmpty());
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(TEST_FOLDER, null, "").isEmpty());

        // one matching file
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_1, null, ".*").size());
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_1, null, ".*\\.txt")
                .size());
        // nonexistent path
        assertEquals(1,
                AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_1, "nonexistent/no/no", ".*")
                        .size());
        // no path specified, many files, only one matches pattern
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_4, null, ".*\\.abc")
                .size());
        // more matching files
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_4, null, ".*").size() > 1);
        // path specified, many files, only one matches pattern
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(EXAMPLE_FOLDER_4, "f5/f6", ".*txt")
                .size());

        /* IHierarchicalContent abstraction */

        // empty pattern
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(MOCK_TEST_FOLDER, null, null).isEmpty());
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(MOCK_TEST_FOLDER, null, "").isEmpty());

        // nonexistent path, all files matches
        assertTrue(AutoResolveUtils.findSomeMatchingFiles(MOCK_TEST_FOLDER, "nonexistent/no/no",
                ".*").isEmpty());
        // no path specified, many files, only one matches pattern
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(MOCK_TEST_FOLDER, null, ".*\\.abc")
                .size());
        // path specified, many files, only one matches pattern
        assertEquals(1, AutoResolveUtils.findSomeMatchingFiles(MOCK_TEST_FOLDER, "f4", ".*\\.abc")
                .size());
    }
}
