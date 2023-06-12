/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.unix;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static ch.systemsx.cisd.base.unix.Unix.Stat;

/**
 * Test cases for the {@link Unix} system calls.
 *
 * @author Juan Fuentes
 */
@Friend(toClasses = Unix.class)
public class UnixTests extends AbstractFileSystemTestCase
{
    private UnixTests()
    {
        super();
    }

    private UnixTests(boolean cleanAfterMethod)
    {
        super(cleanAfterMethod);
    }

    @Test
    public void testGetLinkInfoRegularFile() throws IOException
    {
        final short accessMode = (short) 0777;
        final String content = "someText\n";
        final File f = new File(workingDirectory, "someFile");
        FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
        Unix.setAccessMode(f.getAbsolutePath(), accessMode);
        final Stat info = Unix.getLinkInfo(f.getAbsolutePath());
        Unix.setOwner(f.getAbsolutePath(), info.getUid(), info.getGid());
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(content.length(), info.getSize());
        assertEquals(accessMode, info.getPermissions());
        assertEquals("root", Unix.tryGetUserNameForUid(0));
        assertEquals(FileLinkType.REGULAR_FILE, info.getLinkType());
        assertFalse(info.isSymbolicLink());
        assertEquals(f.lastModified()/1000, info.getLastModified());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetLinkNull() throws IOException
    {
        Unix.getLinkInfo(null);
    }

    @Test
    public void testGetLinkInfoDirectory() throws IOException
    {
        final File d = new File(workingDirectory, "someDir");
        d.mkdir();
        final Stat info = Unix.getLinkInfo(d.getAbsolutePath());
        assertEquals(2, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.DIRECTORY, info.getLinkType());
        assertFalse(info.isSymbolicLink());
    }

    @Test
    public void testGetLinkInfoSymLink() throws IOException
    {
        final File f = new File(workingDirectory, "someOtherFile");
        final String content = "someMoreText\n";
        FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
        final File s = new File(workingDirectory, "someLink");
        Unix.createSymbolicLink(f.getAbsolutePath(), s.getAbsolutePath());
        final Stat info = Unix.getLinkInfo(s.getAbsolutePath());
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.SYMLINK, info.getLinkType());
        assertTrue(info.isSymbolicLink());
        assertEquals(f.getAbsolutePath(), info.tryGetSymbolicLink());
        assertEquals(f.getAbsolutePath(), Unix.tryReadSymbolicLink(s.getAbsolutePath()));
        assertNull(Unix.getLinkInfo(s.getAbsolutePath(), false).tryGetSymbolicLink());

        final Stat info2 = Unix.getFileInfo(s.getAbsolutePath());
        assertEquals(1, info2.getNumberOfHardLinks());
        assertEquals(content.length(), info2.getSize());
        assertEquals(FileLinkType.REGULAR_FILE, info2.getLinkType());
        assertFalse(info2.isSymbolicLink());
        assertNull(info2.tryGetSymbolicLink());
    }

    @Test
    public void testGetLinkInfoSymLinkDanglingLink() throws IOException
    {
        final File s = new File(workingDirectory, "someDanglingLink");
        Unix.createSymbolicLink("link_to_nowhere", s.getAbsolutePath());
        final Stat info = Unix.tryGetLinkInfo(s.getAbsolutePath());
        assertNotNull(info);
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.SYMLINK, info.getLinkType());
        assertTrue(info.isSymbolicLink());
        final Stat info2 = Unix.tryGetFileInfo(s.getAbsolutePath());
        assertNull(info2);
        //        assertEquals("No such file or directory", Unix.getLastError());
    }

    @Test
    public void testGetLinkInfoNonExistent() throws IOException
    {
        final File s = new File(workingDirectory, "nonExistent");
        final Stat info = Unix.tryGetLinkInfo(s.getAbsolutePath());
        assertNull(info);
        //        assertEquals("No such file or directory", Unix.getLastError());
        final Stat info2 = Unix.tryGetFileInfo(s.getAbsolutePath());
        assertNull(info2);
        //        assertEquals("No such file or directory", Unix.getLastError());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCreateSymbolicLinkNull() throws IOException
    {
        Unix.createSymbolicLink(null, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCreateHardLinkNull() throws IOException
    {
        Unix.createHardLink(null, null);
    }

    @Test
    public void testGetLinkInfoHardLink() throws IOException
    {
        final File f = new File(workingDirectory, "someOtherFile");
        f.createNewFile();
        final File s = new File(workingDirectory, "someLink");
        Unix.createHardLink(f.getAbsolutePath(), s.getAbsolutePath());
        final Stat info = Unix.getLinkInfo(s.getAbsolutePath());
        assertEquals(2, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.REGULAR_FILE, info.getLinkType());
        assertFalse(info.isSymbolicLink());
        assertNull(info.tryGetSymbolicLink());
    }

    @Test
    public void testGetUid()
    {
        assertTrue(Unix.getUid() >= 0);
    }

    @Test
    public void testGetEuid()
    {
        assertTrue(Unix.getEuid() >= 0);
        assertEquals(Unix.getUid(), Unix.getEuid());
    }

    @Test
    public void testGetGid()
    {
        assertTrue(Unix.getGid() >= 0);
    }

    @Test
    public void testGetEgid()
    {
        assertTrue(Unix.getEgid() >= 0);
        assertEquals(Unix.getGid(), Unix.getEgid());
    }

    @Test
    public void testGetUidForUserName()
    {
        assertEquals(0, Unix.getUidForUserName("root"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetUidForUserNameNull() throws IOException
    {
        Unix.getUidForUserName(null);
    }

    @Test
    public void testGetGidForGroupName()
    {
        final String rootGroup = Unix.tryGetGroupNameForGid(0);
        assertTrue(rootGroup, "root".equals(rootGroup) || "wheel".equals(rootGroup));
        assertEquals(0, Unix.getGidForGroupName(rootGroup));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetGidForGroupNameNull() throws IOException
    {
        Unix.getGidForGroupName(null);
    }
}
