/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.parser.filter.ExcludeEmptyAndCommentLineFilter;

/**
 * Test cases for the {@link FileUtilities}.
 * 
 * @author Bernd Rinn
 */
public final class FileUtilitiesTest extends AbstractFileSystemTestCase
{

    @Test
    public void testFailedConstructionNonExistent()
    {
        final File nonExistentFile = new File(workingDirectory, "non-existent");
        nonExistentFile.delete();
        String errorMsg = FileUtilities.checkDirectoryFullyAccessible(nonExistentFile, "test");
        assertNotNull(errorMsg);
    }

    @Test
    public void testFailedConstructionFileInsteadOfDirectory() throws IOException
    {
        final File file = new File(workingDirectory, "existent_file");
        file.delete();
        file.deleteOnExit();
        file.createNewFile();
        String errorMsg = FileUtilities.checkDirectoryFullyAccessible(file, "test");
        assertNotNull(errorMsg);
    }

    @Test(groups =
    { "requires_unix" })
    public void testFailedConstructionReadOnly() throws IOException, InterruptedException
    {
        final File readOnlyDirectory = new File(workingDirectory, "read_only_directory");
        readOnlyDirectory.delete();
        readOnlyDirectory.mkdir();
        readOnlyDirectory.deleteOnExit();
        assert readOnlyDirectory.setReadOnly();

        String errorMsg = FileUtilities.checkDirectoryFullyAccessible(readOnlyDirectory, "test");

        // --- clean before checking results
        // Unfortunately, with JDK 5 there is no portable way to set a file or directory read/write,
        // once
        // it has been set read-only, thus this test 'requires_unix' for the time being.
        Runtime.getRuntime().exec(String.format("/bin/chmod u+w %s", readOnlyDirectory.getPath()))
                .waitFor();
        if (readOnlyDirectory.canWrite() == false)
        {
            // Can't use assert here since we expect an AssertationError
            throw new IllegalStateException();
        }
        assertNotNull(errorMsg);
    }

    @Test
    public void testWriteToStringFile() throws Exception
    {
        File file = new File(workingDirectory, "testWriteToStringFile.txt");
        FileUtilities.writeToFile(file, "Hello world\nhow are you?");
        assertEquals("Hello world\nhow are you?\n", FileUtilities.loadToString(file));
    }

    @Test
    public void testWriteToBinaryFile() throws Exception
    {
        File file = new File(workingDirectory, "testWriteToBinaryFile.txt");
        FileUtilities.writeToFile(file, new byte[] { 1, 2, 3 });
        assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, FileUtilities.loadToByteArray(file)));
    }

    @Test
    public void testWriteObjectToFile() throws Exception
    {
        final Date now = new Date();
        File file = new File(workingDirectory, "testWriteObjectToFile.txt");
        FileUtilities.writeToFile(file, now);
        assertEquals(now, FileUtilities.loadToObject(file, Date.class));
    }

    @Test
    public void testWriteToExistingFile() throws Exception
    {
        File file = new File(workingDirectory, "testWriteToFile.txt");
        FileUtilities.writeToFile(file, "Hello");
        FileUtilities.writeToFile(file, "Hello world");
        assertEquals("Hello world\n", FileUtilities.loadToString(file));
    }

    @Test
    public void testWriteToExistingDirectory() throws Exception
    {
        File dir = new File(workingDirectory, "dir");
        assert dir.mkdir();
        try
        {
            FileUtilities.writeToFile(dir, "Hello world");
            fail("CheckedExceptionTunnel expected.");
        } catch (CheckedExceptionTunnel e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IOException);
            String message = cause.getMessage();
            assertTrue("Exception message not as expected: " + message, cause.getMessage()
                    .startsWith(dir.toString()));
        }
    }

    @Test
    public void testWriteToExistingReadOnlyFile() throws Exception
    {
        File file = new File(workingDirectory, "testWriteToFile.txt");
        FileUtilities.writeToFile(file, "Hello");
        assert file.setReadOnly();
        try
        {
            FileUtilities.writeToFile(file, "Hello world");
            fail("CheckedExceptionTunnel expected.");
        } catch (CheckedExceptionTunnel e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof IOException);
            String message = cause.getMessage();
            assertTrue("Exception message not as expected: " + message, cause.getMessage()
                    .startsWith(file.toString()));
        } finally
        {
            assert file.delete();
        }
    }

    @Test
    public void testLoadToStringFile() throws Exception
    {
        File file = new File(workingDirectory, "test.txt");
        FileWriter writer = new FileWriter(file);
        try
        {
            writer.write("Hello\nWorld!");
        } finally
        {
            writer.close();
        }

        String text = FileUtilities.loadToString(file);
        assert file.delete();
        assertEquals("Hello\nWorld!\n", text);
    }

    @Test
    public void testLoadToStringResource() throws Exception
    {
        final String resourceName =
                "/" + getClass().getCanonicalName().replaceAll("\\.", "/") + ".class";
        final String thisFile = FileUtilities.loadToString(getClass(), resourceName);
        assert thisFile != null;
        assert thisFile.indexOf("FileUtilitiesTest") >= 0;
    }

    @Test
    public void testLoadToStringMissingResource() throws Exception
    {
        final String thisFile = FileUtilities.loadToString(getClass(), "/some/missing/resource");
        assert thisFile == null;
    }

    @Test
    public void testLoadToStringListFile() throws Exception
    {
        File file = new File(workingDirectory, "test.txt");
        FileWriter writer = new FileWriter(file);
        try
        {
            writer.write("Hello\nWorld!");
        } finally
        {
            writer.close();
        }

        final List<String> text = FileUtilities.loadToStringList(file);
        assert file.delete();
        assertEquals(Arrays.asList("Hello", "World!"), text);
    }

    @Test
    public final void testLoadToStringListFileWithLineFilter() throws Exception
    {
        final File file = new File(workingDirectory, "test.txt");
        final FileWriter writer = new FileWriter(file);
        try
        {
            writer.write("\nHello\nWorld!\n\n");
        } finally
        {
            writer.close();
        }
        // With no line filter.
        List<String> text = FileUtilities.loadToStringList(file);
        assertEquals(4, text.size());
        assertEquals(Arrays.asList("", "Hello", "World!", ""), text);
        // With null line filter.
        text = FileUtilities.loadToStringList(file, null);
        assertEquals(4, text.size());
        assertEquals(Arrays.asList("", "Hello", "World!", ""), text);
        // With a correct line filter.
        text = FileUtilities.loadToStringList(file, ExcludeEmptyAndCommentLineFilter.INSTANCE);
        assertEquals(2, text.size());
        assertEquals(Arrays.asList("Hello", "World!"), text);
    }

    @Test
    public final void testRemovePrefixFromFileName()
    {
        File file = new File("/tmp/dir/x.txt");
        boolean exceptionThrown = false;
        try
        {
            FileUtilities.removePrefixFromFileName(null, FileConstants.IS_FINISHED_PREFIX);

        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Given file can not be null.", exceptionThrown);
        assertEquals(file, FileUtilities.removePrefixFromFileName(file, null));
        assertEquals(file,
                FileUtilities.removePrefixFromFileName(file, FileConstants.IS_FINISHED_PREFIX));
        file = new File("/tmp/dir/" + FileConstants.IS_FINISHED_PREFIX + "x.txt");
        assertEquals("/tmp/dir/x.txt",
                FileUtilities.removePrefixFromFileName(file, FileConstants.IS_FINISHED_PREFIX)
                        .getPath());
    }

    @Test
    public final void testCreateNextNumberedFile() throws IOException
    {
        File file = new File(workingDirectory, "abc_[12]");
        // File does not exist
        assert file.exists() == false;
        Pattern pattern = Pattern.compile("_\\[(\\d+)\\]");
        File newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc_[12]"), newFile);
        FileUtils.touch(file);
        assert file.exists();
        // File exists
        newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc_[13]"), newFile);
        // File not containing a number in it
        file = new File(workingDirectory, "abc");
        assert file.exists() == false;
        newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc"), newFile);
        FileUtils.touch(file);
        assert file.exists();
        newFile = FileUtilities.createNextNumberedFile(file, pattern, null);
        assertEquals(new File(workingDirectory, "abc1"), newFile);
        boolean exceptionThrown = false;
        try
        {
            FileUtilities.createNextNumberedFile(null, pattern, null);

        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Null value for file not allowed.", exceptionThrown);
        String defaultFileName = "abc_[1]";
        exceptionThrown = false;
        try
        {
            FileUtilities.createNextNumberedFile(file, Pattern.compile("dummyPattern"),
                    defaultFileName);

        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Must contain either '(\\d+)' or ([0-9]+).", exceptionThrown);
        // File already exists but default one does not
        assertEquals(file.getName(), "abc");
        assert file.exists();
        newFile = FileUtilities.createNextNumberedFile(file, pattern, defaultFileName);
        File defaultFile = new File(workingDirectory, defaultFileName);
        assertEquals(defaultFile, newFile);
        // File already exists and default one also
        FileUtils.touch(defaultFile);
        assert defaultFile.exists();
        newFile = FileUtilities.createNextNumberedFile(file, pattern, defaultFileName);
        assertEquals(new File(workingDirectory, "abc_[2]"), newFile);
        // With no pattern (using the default one)
        file = new File(workingDirectory, "a0bc1");
        FileUtils.touch(file);
        assert file.exists();
        newFile = FileUtilities.createNextNumberedFile(file, null);
        assertEquals(new File(workingDirectory, "a0bc2"), newFile);
        // More examples
        file = new File(workingDirectory, "12abc_[12]");
        newFile = FileUtilities.createNextNumberedFile(file, pattern, "12abc_[1]");
        assertEquals(new File(workingDirectory, "12abc_[12]"), newFile);
        FileUtils.touch(file);
        newFile = FileUtilities.createNextNumberedFile(file, pattern, "12abc_[1]");
        assertEquals(new File(workingDirectory, "12abc_[13]"), newFile);
        newFile =
                FileUtilities.createNextNumberedFile(file, Pattern.compile("xxx(\\d+)xxx"),
                        "12abc_[1]");
        assertEquals(new File(workingDirectory, "12abc_[1]"), newFile);
    }

    @Test
    public final void testGetRelativeFilePath()
    {
        boolean exceptionThrown = false;
        try
        {
            FileUtilities.getRelativeFilePath(null, null);
        } catch (AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Given root and file can not be null.", exceptionThrown);
        File file = new File(workingDirectory, "hello");
        assertEquals(workingDirectory.getAbsolutePath() + File.separator + "hello",
                file.getAbsolutePath());
        // If the given string is the empty string, then the result is the empty abstract pathname.
        final File rootFile = new File("");
        assertEquals(TARGETS_DIRECTORY + File.separator + UNIT_TEST_WORKING_DIRECTORY
                + File.separator + workingDirectory.getName() + File.separator + "hello",
                FileUtilities.getRelativeFilePath(rootFile, file));
        String root = "/temp";
        String relativeFile = FileUtilities.getRelativeFilePath(new File(root), file);
        assertNull(relativeFile);
        root = workingDirectory.getAbsolutePath();
        relativeFile = FileUtilities.getRelativeFilePath(new File(root), file);
        assertEquals("hello", relativeFile);
        relativeFile = FileUtilities.getRelativeFilePath(new File(root), new File(root));
        assertEquals("", relativeFile);
    }

    @DataProvider(name = "parentRelativePaths")
    public Object[][] getParentRelativePathsData()
    {
        return new Object[][] {
                { "/test/a", "/test" },
                { "/test/a/", "/test" },
                { "/", null },
                { "test/a", "test" },
                { "test/a/", "test" },
                { "test", "" },
                { "", null },
        };
    }

    @Test(dataProvider = "parentRelativePaths")
    public void testGetParentRelativePath(String input, String output)
    {
        assertEquals(output, FileUtilities.getParentRelativePath(input));
    }

    private class CountingActivityObserver implements IActivityObserver
    {
        int count = 0;

        @Override
        public void update()
        {
            ++count;
        }
    }

    @Test
    public void testListFiles() throws IOException
    {
        final File dir = new File(workingDirectory, "listFiles");
        dir.mkdir();
        final File nonExistentDir = new File(dir, "nonExistent");
        assertTrue(FileUtilities.listFiles(dir, (FileFilter) null, true, null, null).isEmpty());
        try
        {
            FileUtilities.listFiles(nonExistentDir, (FileFilter) null, true, null, null);
        } catch (EnvironmentFailureException ex)
        {
            assertTrue(ex.getMessage().endsWith("not a directory."));
        }
        final File subDir = new File(dir, "subdir");
        subDir.mkdir();
        assertTrue(FileUtilities.listFiles(dir, (FileFilter) null, true, null, null).isEmpty());
        assertEquals("subdir", FileUtilities.listDirectories(dir, true, null).get(0).getName());
        final File f1 = new File(dir, "f1.dat");
        f1.createNewFile();
        final File f2 = new File(dir, "f2.bla");
        f2.createNewFile();
        final File f3 = new File(subDir, "f3");
        f3.createNewFile();
        final File f4 = new File(subDir, "f4.dat");
        f4.createNewFile();
        final CountingActivityObserver observer = new CountingActivityObserver();

        final List<File> list1 = FileUtilities.listFiles(dir, (FileFilter) null, true, null, null);
        assertEquals(4, list1.size());
        assertEquals(new HashSet<File>(Arrays.asList(f1, f2, f3, f4)), new HashSet<File>(list1));

        final List<File> list2 =
                FileUtilities.listFiles(dir, (FileFilter) null, true, observer, null);
        assertEquals(list1, list2);
        assertTrue("" + observer.count, observer.count >= list2.size());

        observer.count = 0;
        final List<File> list3 = FileUtilities.listFiles(dir, new String[]
        { "dat" }, true, observer);
        assertEquals(2, list3.size());
        assertEquals(new HashSet<File>(Arrays.asList(f1, f4)), new HashSet<File>(list3));
        assertTrue("" + observer.count, observer.count >= list3.size());

        final File subDir2 = new File(subDir, "subDir2");
        subDir2.mkdir();
        final File f5 = new File(subDir2, "f5.ttt");
        f5.createNewFile();

        final List<File> list4 = FileUtilities.listFiles(dir, (FileFilter) null, true, null, null);
        assertEquals(5, list4.size());
        assertEquals(new HashSet<File>(Arrays.asList(f1, f2, f3, f4, f5)), new HashSet<File>(list4));

        final File subDir3 = new File(dir, "subDir3");
        subDir3.mkdir();
        observer.count = 0;
        final List<File> list5 = FileUtilities.listDirectories(dir, true, observer);
        assertEquals(3, list5.size());
        assertEquals(new HashSet<File>(Arrays.asList(subDir, subDir2, subDir3)), new HashSet<File>(
                list5));
        assertTrue("" + observer.count, observer.count >= list5.size());

        observer.count = 0;
        final List<File> list6 = FileUtilities.listDirectories(dir, false, observer);
        assertEquals(2, list6.size());
        assertEquals(new HashSet<File>(Arrays.asList(subDir, subDir3)), new HashSet<File>(list6));
        assertTrue("" + observer.count, observer.count >= list6.size());
    }

    @Test
    public final void testNormalizeFile() throws IOException
    {
        boolean exceptionThrown = false;
        try
        {
            FileUtilities.normalizeFile(null);
        } catch (AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Given file can not be null.", exceptionThrown);
        final File file = new File(workingDirectory, "../dir");
        final String canonicalPath = file.getCanonicalPath();
        assertFalse(canonicalPath.equals(file.getAbsolutePath()));
        assertEquals(canonicalPath, FileUtilities.normalizeFile(file).getAbsolutePath());
    }

    @Test
    public final void testByteCountToDisplaySize()
    {
        assertEquals("0 bytes", FileUtilities.byteCountToDisplaySize(0));
        assertEquals("1 byte", FileUtilities.byteCountToDisplaySize(1));
        assertEquals("2 bytes", FileUtilities.byteCountToDisplaySize(2));
        assertEquals("1.00 KB", FileUtilities.byteCountToDisplaySize(1024));
        assertEquals("1.01 KB", FileUtilities.byteCountToDisplaySize(1034));
        assertEquals("1.00 MB", FileUtilities.byteCountToDisplaySize(1024 * 1024));
    }

    @Test(groups = "requires_unix")
    public void testIsSymbolicLink() throws Exception
    {
        File original = new File(workingDirectory, "org-file");
        assertTrue(original.createNewFile());

        File linkFile = new File(workingDirectory, "link-file");
        boolean ok = SoftLinkMaker.createSymbolicLink(original.getAbsoluteFile(), linkFile);
        assertTrue("cannot create a symbolic link", ok);

        assertTrue(FileUtilities.isSymbolicLink(linkFile));

        File originalRelative =
                new File(workingDirectory.getAbsoluteFile() + "/../" + workingDirectory.getName()
                        + "/" + original.getName());
        assertFalse(FileUtilities.isSymbolicLink(originalRelative));

        File linkRelative =
                new File(workingDirectory.getAbsoluteFile() + "/../" + workingDirectory.getName()
                        + "/" + linkFile.getName());
        assertTrue(FileUtilities.isSymbolicLink(linkRelative));
    }

    @Test
    public void testValidFileName()
    {
        assertTrue(FileUtilities.isValidFileName("valid-name1"));
        assertTrue(FileUtilities.isValidFileName("valid with space and tab\t"));
        assertTrue(FileUtilities.isValidFileName("valid.dot"));
        assertFalse(FileUtilities.isValidFileName(""));
        assertFalse(FileUtilities.isValidFileName("a/b"));
    }

    @Test
    public void testGetSizeOf()
    {
        File testFolder = new File(workingDirectory, "test-folder");
        File subFolder = new File(testFolder, "sub-folder");
        subFolder.mkdirs();
        File file1 = new File(testFolder, "text1.txt");
        FileUtilities.writeToFile(file1, createDummyStringOfSize(4500));
        File file2 = new File(subFolder, "text2.txt");
        FileUtilities.writeToFile(file2, createDummyStringOfSize(300));
        File file3 = new File(subFolder, "text3.txt");
        FileUtilities.writeToFile(file3, createDummyStringOfSize(2900));

        assertEquals(4500, FileUtilities.getSizeOf(file1));
        assertEquals(300, FileUtilities.getSizeOf(file2));
        assertEquals(2900, FileUtilities.getSizeOf(file3));
        if (OSUtilities.isMacOS())
        {
            assertEquals(2 * 4096, FileUtilities.getSizeOf(subFolder));
            assertEquals(4 * 4096, FileUtilities.getSizeOf(testFolder));
        } else if (OSUtilities.isUnix())
        {
            assertEquals(3 * 4096, FileUtilities.getSizeOf(subFolder));
            assertEquals(6 * 4096, FileUtilities.getSizeOf(testFolder));
        }
    }

    private String createDummyStringOfSize(long size)
    {
        StringBuilder builder = new StringBuilder();
        for (long i = 0; i < size; i++)
        {
            builder.append('x');
        }
        return builder.toString();
    }
}
