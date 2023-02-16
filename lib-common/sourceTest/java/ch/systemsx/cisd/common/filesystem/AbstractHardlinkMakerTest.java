/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.filesystem;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.io.CollectionIO;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.RetryTen;
import ch.systemsx.cisd.common.test.TestReportCleaner;

/**
 * The abstract superclass of tests for the various hardlink maker implementations.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Listeners(TestReportCleaner.class)
public abstract class AbstractHardlinkMakerTest
{
    protected static final File unitTestRootDirectory = new File("targets" + File.separator
            + "unit-test-wd");

    protected File workingDirectory;

    protected File outputDir;

    protected static File createFile(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.createNewFile();
        assert file.isFile();
        CollectionIO
                .writeIterable(file, Arrays.asList("test line 1", "test line 2", "test line 3"));
        file.deleteOnExit();
        return file;
    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp()
    {
        workingDirectory = new File(unitTestRootDirectory, getClass().getSimpleName());
        outputDir = new File(workingDirectory, "output");
        FileUtilities.deleteRecursively(workingDirectory);
        outputDir.mkdirs();
    }

    @AfterClass
    public void clean()
    {
        FileUtilities.deleteRecursively(outputDir);
    }

    private static File createDirectory(File directory, String name) throws IOException
    {
        final File file = new File(directory, name);
        file.mkdir();
        assert file.isDirectory();
        file.deleteOnExit();
        return file;
    }

    private static File mkPath(File parent, String... subdirs)
    {
        File file = parent;
        for (String subdir : subdirs)
        {
            file = new File(file, subdir);
        }
        return file;
    }

    private static void assertFileExists(File file)
    {
        assert file.isFile();
    }

    private static void assertStructureExists(File destinationDir)
    {
        assertFileExists(mkPath(destinationDir, "dir1", "dir1a", "file1a"));
        assertFileExists(mkPath(destinationDir, "dir1", "dir1b", "file1b"));
        assertFileExists(mkPath(destinationDir, "dir2", "file4"));
        assertFileExists(mkPath(destinationDir, "file2"));
        assertFileExists(mkPath(destinationDir, "file3"));
    }

    private void createStructure(File inputDir) throws IOException
    {
        final File dir1 = createDirectory(inputDir, "dir1");
        final File dir1a = createDirectory(dir1, "dir1a");
        final File dir1b = createDirectory(dir1, "dir1b");
        final File dir2 = createDirectory(inputDir, "dir2");
        createFile(dir1a, "file1a");
        createFile(dir1b, "file1b");
        createFile(inputDir, "file2");
        createFile(inputDir, "file3");
        createFile(dir2, "file4");
    }

    @Test(groups =
    { "requires_unix" }, retryAnalyzer = RetryTen.class)
    public void testCopyWithHardLinks() throws IOException
    {
        File inputDir = createDirectory(workingDirectory, "resource-to-copy");
        createStructure(inputDir);
        assertTrue(createHardLinkCopier().copyImmutably(inputDir, outputDir, null).isOK());
        File newInput = new File(outputDir, inputDir.getName());

        assertStructureExists(newInput);
        boolean deleted = FileUtilities.deleteRecursively(inputDir);
        assert deleted;
        assertStructureExists(newInput);
    }

    private static void assertFilesIdentical(File file1, File file2)
    {
        List<String> list1 = CollectionIO.readList(file1);
        List<String> list2 = CollectionIO.readList(file2);
        assertEquals(list1, list2);
    }

    @Test(groups =
    { "requires_unix" }, retryAnalyzer = RetryTen.class)
    public void testCopyFile() throws IOException
    {
        File src = createFile(workingDirectory, "fileXXX");
        assertFileExists(src);

        assertTrue(createHardLinkCopier().copyImmutably(src, outputDir, null).isOK());
        File dest = new File(outputDir, src.getName());
        assertFileExists(dest);

        modifyDest(dest);
        assertFilesIdentical(src, dest);
    }

    private static void modifyDest(File file)
    {
        List<String> list = Arrays.asList("new line 1", "new line 2");
        CollectionIO.writeIterable(file, list);
    }

    /**
     *
     *
     */
    public AbstractHardlinkMakerTest()
    {
        super();
    }

    @Test(groups =
    { "requires_unix" }, retryAnalyzer = RetryTen.class)
    public void testDeleteWhileCopying() throws IOException
    {
        TestBigStructureCreator creator =
                createBigStructureCreator(new File(workingDirectory, "big-structure"));
        final File src = creator.createBigStructure();
        assertTrue(creator.verifyStructure());
        MessageChannel channel = new MessageChannel();
        
        creator.deleteBigStructureAsync(channel);
        IImmutableCopier copier =
                new AssertionCatchingImmutableCopierWrapper(createHardLinkCopier());
        creator.waitUntilDeletionStarted(channel);
        Status status = copier.copyImmutably(src, outputDir, null);
        assertFalse(status.isOK());
        File dest = new File(outputDir, src.getName());

        TestBigStructureCreator structureCopy = new TestBigStructureCreator(dest);
        assertFalse("Big structure was partially copied", structureCopy.verifyStructure());
        assertFalse("Original was not partially deleted", creator.verifyStructure());

    }

    /**
     * Construct a TestBigStructureCreator. Subclasses may override.
     */
    protected TestBigStructureCreator createBigStructureCreator(File root)
    {
        return new TestBigStructureCreator(root);
    }

    protected abstract IImmutableCopier createHardLinkCopier();
}