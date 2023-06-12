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

package ch.systemsx.cisd.common.io;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.unix.FileLinkType;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.testng.Assert.assertNotEquals;
import static ch.systemsx.cisd.common.io.Posix.Stat;

/**
 * Test cases for the {@link Posix} system calls.
 * 
 * @author Juan Fuentes
 */
@Friend(toClasses = Posix.class)
public class PosixTest extends AbstractFileSystemTestCase
{
    private PosixTest()
    {
        super();
    }

    private PosixTest(boolean cleanAfterMethod)
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
        Posix.setAccessMode(f.getAbsolutePath(), accessMode);
        final Stat info = Posix.getLinkInfo(f.getAbsolutePath());
        Posix.setOwner(f.getAbsolutePath(), info.getUid(), info.getGid());
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(content.length(), info.getSize());
        assertEquals(accessMode, info.getPermissions());
        assertEquals("root", Posix.tryGetUserNameForUid(0));
        assertEquals(FileLinkType.REGULAR_FILE, info.getLinkType());
        assertFalse(info.isSymbolicLink());
        assertEquals(f.lastModified()/1000, info.getLastModified());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetLinkNull() throws IOException
    {
        Posix.getLinkInfo(null);
    }

    @Test
    public void testGetLinkInfoDirectory() throws IOException
    {
        final File d = new File(workingDirectory, "someDir");
        d.mkdir();
        final Stat info = Posix.getLinkInfo(d.getAbsolutePath());
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
        Posix.createSymbolicLink(f.getAbsolutePath(), s.getAbsolutePath());
        final Stat info = Posix.getLinkInfo(s.getAbsolutePath());
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.SYMLINK, info.getLinkType());
        assertTrue(info.isSymbolicLink());
        assertEquals(f.getAbsolutePath(), info.tryGetSymbolicLink());
        assertEquals(f.getAbsolutePath(), Posix.tryReadSymbolicLink(s.getAbsolutePath()));
        assertNull(Posix.getLinkInfo(s.getAbsolutePath(), false).tryGetSymbolicLink());

        final Stat info2 = Posix.getFileInfo(s.getAbsolutePath());
        assertEquals(1, info2.getNumberOfHardLinks());
        assertEquals(content.length(), info2.getSize());
        assertEquals(FileLinkType.REGULAR_FILE, info2.getLinkType());
        assertFalse(info2.isSymbolicLink());
        assertNull(info2.tryGetSymbolicLink());
    }

//    @Test
//    public void testTouchSymLinkAndFileRealtimeTimer() throws IOException, InterruptedException
//    {
//        if (BuildAndEnvironmentInfo.INSTANCE.getOS().contains("2.6.32"))
//        {
//            System.out.println("  ...skipping as CentOS6 does not yet support the realtime timer.");
//            return;
//        }
////        Posix.setUseUnixRealtimeTimer(true);
//        final File f = new File(workingDirectory, "someOtherFile");
//        final String content = "someMoreText\n";
//        FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
//        final File s = new File(workingDirectory, "someLink");
//        Posix.createSymbolicLink(f.getAbsolutePath(), s.getAbsolutePath());
//        final Stat info = Posix.getLinkInfo(s.getAbsolutePath());
//        assertEquals(1, info.getNumberOfHardLinks());
//        assertEquals(FileLinkType.SYMLINK, info.getLinkType());
//        assertTrue(info.isSymbolicLink());
//        assertEquals(f.getAbsolutePath(), info.tryGetSymbolicLink());
//        assertEquals(f.getAbsolutePath(), Posix.tryReadSymbolicLink(s.getAbsolutePath()));
//        assertNull(Posix.getLinkInfo(s.getAbsolutePath(), false).tryGetSymbolicLink());
//        final long lastMicros = info.getLastModifiedTime().getMicroSecPart();
//        final long newLastModifiedLink = info.getLastModifiedTime().getSecs() - 24 * 3600;
//        Posix.setLinkTimestamps(s.getAbsolutePath(), newLastModifiedLink, lastMicros, newLastModifiedLink, lastMicros);
//
//        final long newLastModifiedFile = info.getLastModifiedTime().getSecs() - 2 * 24 * 3600;
//        Posix.setFileTimestamps(f.getAbsolutePath(), newLastModifiedFile, lastMicros, newLastModifiedFile, lastMicros);
//
//        final Stat info2l = Posix.getLinkInfo(s.getAbsolutePath(), false);
//        assertEquals(newLastModifiedLink, info2l.getLastModifiedTime().getSecs());
//        assertEquals(lastMicros, info2l.getLastModifiedTime().getMicroSecPart());
//        assertEquals(newLastModifiedLink, info2l.getLastAccessTime().getSecs());
//        assertEquals(lastMicros, info2l.getLastAccessTime().getMicroSecPart());
//
//        final Stat info2f = Posix.getFileInfo(s.getAbsolutePath());
//        final Stat info2f2 = Posix.getLinkInfo(f.getAbsolutePath());
//        assertNotEquals(info2l.getLastModifiedTime(), info2f2.getLastModifiedTime());
//        assertEquals(info2f2.getLastModifiedTime(), info2f.getLastModifiedTime());
//        assertEquals(newLastModifiedFile, info2f.getLastModifiedTime().getSecs());
//        assertEquals(lastMicros, info2f.getLastModifiedTime().getMicroSecPart());
//        assertEquals(newLastModifiedFile, info2f.getLastAccessTime().getSecs());
//        assertEquals(lastMicros, info2f.getLastAccessTime().getMicroSecPart());
//
//        Thread.sleep(10);
//
//        final Posix.Time now1 = Posix.getSystemTime();
//        assertNotEquals(0, now1.getNanoSecPart() % 1_000);
//        Posix.setLinkTimestamps(s.getAbsolutePath());
//        final Posix.Time now2 = Posix.getSystemTime();
//        final Stat info3 = Posix.getLinkInfo(s.getAbsolutePath());
//
//        assertTrue(now1.getSecs() <= info3.getLastModified() && info3.getLastModified() <= now2.getSecs());
//        assertTrue(now1.getMicroSecPart() <= info3.getLastModifiedTime().getMicroSecPart() && info.getLastModifiedTime().getMilliSecPart() <= now2.getMicroSecPart());
//        assertTrue(now1.getSecs() <= info3.getLastAccess() && info3.getLastAccess() <= now2.getSecs());
//        assertTrue(now1.getMicroSecPart() <= info3.getLastAccessTime().getMicroSecPart() && info.getLastAccessTime().getMilliSecPart() <= now2.getMicroSecPart());
//        assertNotEquals(lastMicros, info3.getLastModifiedTime().getMicroSecPart());
//        assertNotEquals(lastMicros, info3.getLastAccessTime().getMicroSecPart());
//
//    }
//
//    @Test
//    public void testTouchSymLinkAndFile() throws IOException, InterruptedException
//    {
////        Posix.setUseUnixRealtimeTimer(false);
//        final File someFile = new File(workingDirectory, "someOtherFile");
//        final String content = "someMoreText\n";
//        FileUtils.writeStringToFile(someFile, content, Charset.defaultCharset());
//        final File someSymlink = new File(workingDirectory, "someLink");
//        Posix.createSymbolicLink(someFile.getAbsolutePath(), someSymlink.getAbsolutePath());
//        final Stat infoSymlink = Posix.getLinkInfo(someSymlink.getAbsolutePath());
//        assertEquals(1, infoSymlink.getNumberOfHardLinks());
//        assertEquals(FileLinkType.SYMLINK, infoSymlink.getLinkType());
//        assertTrue(infoSymlink.isSymbolicLink());
//        assertEquals(someFile.getAbsolutePath(), infoSymlink.tryGetSymbolicLink());
//        assertEquals(someFile.getAbsolutePath(), Posix.tryReadSymbolicLink(someSymlink.getAbsolutePath()));
//        assertNull(Posix.getLinkInfo(someSymlink.getAbsolutePath(), false).tryGetSymbolicLink());
//        final long newLastModifiedLinkMicros = infoSymlink.getLastModifiedTime().getMicroSecPart();
//        final long newLastModifiedLinkSec = infoSymlink.getLastModifiedTime().getSecs() - 24 * 3600;
//        Posix.setLinkTimestamps(someSymlink.getAbsolutePath(), newLastModifiedLinkSec, newLastModifiedLinkMicros, newLastModifiedLinkSec, newLastModifiedLinkMicros);
//
//        final long newLastModifiedSecFile = infoSymlink.getLastModifiedTime().getSecs() - 2 * 24 * 3600;
//        Posix.setFileTimestamps(someFile.getAbsolutePath(), newLastModifiedSecFile, newLastModifiedLinkMicros, newLastModifiedSecFile, newLastModifiedLinkMicros);
//
//        final Stat infoSymlink2 = Posix.getLinkInfo(someSymlink.getAbsolutePath(), false);
//        assertEquals(newLastModifiedLinkSec, infoSymlink2.getLastModifiedTime().getSecs());
//        assertEquals(newLastModifiedLinkMicros, infoSymlink2.getLastModifiedTime().getMicroSecPart());
//        assertEquals(newLastModifiedLinkSec, infoSymlink2.getLastAccessTime().getSecs());
//        assertEquals(newLastModifiedLinkMicros, infoSymlink2.getLastAccessTime().getMicroSecPart());
//
//        final Stat info2f = Posix.getFileInfo(someSymlink.getAbsolutePath());
//        final Stat info2f2 = Posix.getLinkInfo(someFile.getAbsolutePath());
//        assertNotEquals(infoSymlink2.getLastModifiedTime(), info2f2.getLastModifiedTime());
//        assertEquals(info2f2.getLastModifiedTime(), info2f.getLastModifiedTime());
//        assertEquals(newLastModifiedSecFile, info2f.getLastModifiedTime().getSecs());
//        assertEquals(newLastModifiedLinkMicros, info2f.getLastModifiedTime().getMicroSecPart());
//        assertEquals(newLastModifiedSecFile, info2f.getLastAccessTime().getSecs());
//        assertEquals(newLastModifiedLinkMicros, info2f.getLastAccessTime().getMicroSecPart());
//
//
//        Thread.sleep(10);
//
//        final Posix.Time now1 = Posix.getSystemTime();
//        assertEquals(0, now1.getNanoSecPart() % 1_000);
//        Posix.setLinkTimestamps(someSymlink.getAbsolutePath()); // Modifies the link, not the linked file
//        final Posix.Time now2 = Posix.getSystemTime();
//        final Stat info3 = Posix.getLinkInfo(someSymlink.getAbsolutePath()); // Returns the linked file info
//
//        assertTrue(now1.getSecs() <= info3.getLastModified() && info3.getLastModified() <= now2.getSecs());
//        assertTrue(now1.getMicroSecPart() <= info3.getLastModifiedTime().getMicroSecPart() && infoSymlink.getLastModifiedTime().getMilliSecPart() <= now2.getMicroSecPart());
//        assertTrue(now1.getSecs() <= info3.getLastAccess() && info3.getLastAccess() <= now2.getSecs());
//        assertTrue(now1.getMicroSecPart() <= info3.getLastAccessTime().getMicroSecPart() && infoSymlink.getLastAccessTime().getMilliSecPart() <= now2.getMicroSecPart());
//        assertNotEquals(newLastModifiedLinkMicros, info3.getLastModifiedTime().getMicroSecPart());
//        assertNotEquals(newLastModifiedLinkMicros, info3.getLastAccessTime().getMicroSecPart());
//    }

    @Test
    public void testGetLinkInfoSymLinkDanglingLink() throws IOException
    {
        final File s = new File(workingDirectory, "someDanglingLink");
        Posix.createSymbolicLink("link_to_nowhere", s.getAbsolutePath());
        final Stat info = Posix.tryGetLinkInfo(s.getAbsolutePath());
        assertNotNull(info);
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.SYMLINK, info.getLinkType());
        assertTrue(info.isSymbolicLink());
        final Stat info2 = Posix.tryGetFileInfo(s.getAbsolutePath());
        assertNull(info2);
//        assertEquals("No such file or directory", Posix.getLastError());
    }

    @Test
    public void testGetLinkInfoNonExistent() throws IOException
    {
        final File s = new File(workingDirectory, "nonExistent");
        final Stat info = Posix.tryGetLinkInfo(s.getAbsolutePath());
        assertNull(info);
//        assertEquals("No such file or directory", Posix.getLastError());
        final Stat info2 = Posix.tryGetFileInfo(s.getAbsolutePath());
        assertNull(info2);
//        assertEquals("No such file or directory", Posix.getLastError());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCreateSymbolicLinkNull() throws IOException
    {
        Posix.createSymbolicLink(null, null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCreateHardLinkNull() throws IOException
    {
        Posix.createHardLink(null, null);
    }

    @Test
    public void testGetLinkInfoHardLink() throws IOException
    {
        final File f = new File(workingDirectory, "someOtherFile");
        f.createNewFile();
        final File s = new File(workingDirectory, "someLink");
        Posix.createHardLink(f.getAbsolutePath(), s.getAbsolutePath());
        final Stat info = Posix.getLinkInfo(s.getAbsolutePath());
        assertEquals(2, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.REGULAR_FILE, info.getLinkType());
        assertFalse(info.isSymbolicLink());
        assertNull(info.tryGetSymbolicLink());
    }

    @Test
    public void testGetUid()
    {
        assertTrue(Posix.getUid() >= 0);
    }

    @Test
    public void testGetEuid()
    {
        assertTrue(Posix.getEuid() >= 0);
        assertEquals(Posix.getUid(), Posix.getEuid());
    }

    @Test
    public void testGetGid()
    {
        assertTrue(Posix.getGid() >= 0);
    }

    @Test
    public void testGetEgid()
    {
        assertTrue(Posix.getEgid() >= 0);
        assertEquals(Posix.getGid(), Posix.getEgid());
    }

    @Test
    public void testGetUidForUserName()
    {
        assertEquals(0, Posix.getUidForUserName("root"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetUidForUserNameNull() throws IOException
    {
        Posix.getUidForUserName(null);
    }

    @Test
    public void testGetGidForGroupName()
    {
        final String rootGroup = Posix.tryGetGroupNameForGid(0);
        assertTrue(rootGroup, "root".equals(rootGroup) || "wheel".equals(rootGroup));
        assertEquals(0, Posix.getGidForGroupName(rootGroup));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetGidForGroupNameNull() throws IOException
    {
        Posix.getGidForGroupName(null);
    }
//
//    @Test(groups =
//        { "requires_unix" })
//    public void testTryGetGroupByName()
//    {
//        final String rootGroup = Posix.tryGetGroupNameForGid(0);
//        final Group group = Posix.tryGetGroupByName(rootGroup);
//        assertNotNull(group);
//        assertEquals(rootGroup, group.getGroupName());
//        assertEquals(0, group.getGid());
//        assertNotNull(group.getGroupMembers());
//    }
//
//    @Test(groups =
//        { "requires_unix" }, expectedExceptions = NullPointerException.class)
//    public void testTryGetGroupByNameNull() throws IOException
//    {
//        Posix.tryGetGroupByName(null);
//    }
//
//    @Test(groups =
//        { "requires_unix" })
//    public void testTryGetGroupByGid()
//    {
//        final Group group = Posix.tryGetGroupByGid(0);
//        assertNotNull(group);
//        final String rootGroup = group.getGroupName();
//        assertTrue(rootGroup, "root".equals(rootGroup) || "wheel".equals(rootGroup));
//        assertEquals(0, group.getGid());
//        assertNotNull(group.getGroupMembers());
//    }
//
//    @Test(groups =
//        { "requires_unix" })
//    public void testTryGetUserByName()
//    {
//        final Password user = Posix.tryGetUserByName("root");
//        assertNotNull(user);
//        assertEquals("root", user.getUserName());
//        assertEquals(0, user.getUid());
//        assertEquals(0, user.getGid());
//        assertNotNull(user.getUserFullName());
//        assertNotNull(user.getHomeDirectory());
//        assertNotNull(user.getShell());
//        assertTrue(user.getShell().startsWith("/"));
//    }
//
//    @Test(groups =
//        { "requires_unix" }, expectedExceptions = NullPointerException.class)
//    public void testTryGetUserByNameNull() throws IOException
//    {
//        Posix.tryGetUserByName(null);
//    }
//
//    @Test(groups =
//        { "requires_unix" })
//    public void testTryGetUserByUid()
//    {
//        final Password user = Posix.tryGetUserByUid(0);
//        assertNotNull(user);
//        assertEquals("root", user.getUserName());
//        assertEquals(0, user.getUid());
//        assertEquals(0, user.getGid());
//        assertNotNull(user.getUserFullName());
//        assertNotNull(user.getHomeDirectory());
//        assertNotNull(user.getShell());
//        assertTrue(user.getShell().startsWith("/"));
//    }
//
//    @Test(groups =
//        { "requires_unix" })
//    public void testDetectProcess()
//    {
//        assertTrue(Posix.canDetectProcesses());
//        assertTrue(Posix.isProcessRunningPS(Posix.getPid()));
//    }
//
//    public static void main(String[] args) throws Throwable
//    {
//        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
//        System.out.println("Test class: " + UnixTests.class.getSimpleName());
//        System.out.println();
//        if (Posix.isOperational() == false)
//        {
//            System.err.println("No unix library found.");
//            System.exit(1);
//        }
//        boolean stopOnError = args.length > 0 && "stopOnError".equalsIgnoreCase(args[0]);
//        int failed = 0;
//        final UnixTests test = new UnixTests();
//        try
//        {
//            for (Method m : UnixTests.class.getMethods())
//            {
//                final Test testAnnotation = m.getAnnotation(Test.class);
//                if (testAnnotation == null)
//                {
//                    continue;
//                }
//                System.out.println("Running " + m.getName());
//                test.setUp();
//                try
//                {
//                    m.invoke(test);
//                } catch (InvocationTargetException wrapperThrowable)
//                {
//                    final Throwable th = wrapperThrowable.getCause();
//                    boolean exceptionFound = false;
//                    for (Class<?> expectedExClazz : testAnnotation.expectedExceptions())
//                    {
//                        if (expectedExClazz == th.getClass())
//                        {
//                            exceptionFound = true;
//                            break;
//                        }
//                    }
//                    if (exceptionFound == false)
//                    {
//                        ++failed;
//                        System.out.println("Caught exception in method " + m.getName());
//                        th.printStackTrace();
//                        if (stopOnError)
//                        {
//                            System.exit(1);
//                        }
//                    }
//                }
//            }
//            if (failed == 0)
//            {
//                System.out.println("Tests OK!");
//            } else
//            {
//                System.out.printf("%d tests FAILED!\n", failed);
//            }
//        } finally
//        {
//            if (failed == 0)
//            {
//                test.afterClass();
//            }
//        }
//    }

}
