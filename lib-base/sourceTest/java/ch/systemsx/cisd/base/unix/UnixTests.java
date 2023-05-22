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

import static org.testng.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.unix.Unix.Group;
import ch.systemsx.cisd.base.unix.Unix.Password;
import ch.systemsx.cisd.base.unix.Unix.Stat;

/**
 * Test cases for the {@link Unix} system calls.
 * 
 * @author Bernd Rinn
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

    @Test(groups =
        { "requires_unix" })
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

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testGetLinkNull() throws IOException
    {
        Unix.getLinkInfo(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoDirectory() throws IOException
    {
        final File d = new File(workingDirectory, "someDir");
        d.mkdir();
        final Stat info = Unix.getLinkInfo(d.getAbsolutePath());
        assertEquals(2, info.getNumberOfHardLinks());
        assertEquals(FileLinkType.DIRECTORY, info.getLinkType());
        assertFalse(info.isSymbolicLink());
    }

    @Test(groups =
        { "requires_unix" })
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

    @Test(groups =
        { "requires_unix" })
    public void testTouchSymLinkAndFileRealtimeTimer() throws IOException, InterruptedException
    {
        if (BuildAndEnvironmentInfo.INSTANCE.getOS().contains("2.6.32"))
        {
            System.out.println("  ...skipping as CentOS6 does not yet support the realtime timer.");
            return;
        }
        Unix.setUseUnixRealtimeTimer(true);
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
        final long lastMicros = info.getLastModifiedTime().getMicroSecPart();
        final long newLastModifiedLink = info.getLastModifiedTime().getSecs() - 24 * 3600;
        Unix.setLinkTimestamps(s.getAbsolutePath(), newLastModifiedLink, lastMicros, newLastModifiedLink, lastMicros);

        final long newLastModifiedFile = info.getLastModifiedTime().getSecs() - 2 * 24 * 3600;
        Unix.setFileTimestamps(f.getAbsolutePath(), newLastModifiedFile, lastMicros, newLastModifiedFile, lastMicros);

        final Stat info2l = Unix.getLinkInfo(s.getAbsolutePath(), false);
        assertEquals(newLastModifiedLink, info2l.getLastModifiedTime().getSecs());
        assertEquals(lastMicros, info2l.getLastModifiedTime().getMicroSecPart());
        assertEquals(newLastModifiedLink, info2l.getLastAccessTime().getSecs());
        assertEquals(lastMicros, info2l.getLastAccessTime().getMicroSecPart());

        final Stat info2f = Unix.getFileInfo(s.getAbsolutePath());
        final Stat info2f2 = Unix.getLinkInfo(f.getAbsolutePath());
        assertNotEquals(info2l.getLastModifiedTime(), info2f2.getLastModifiedTime());
        assertEquals(info2f2.getLastModifiedTime(), info2f.getLastModifiedTime());
        assertEquals(newLastModifiedFile, info2f.getLastModifiedTime().getSecs());
        assertEquals(lastMicros, info2f.getLastModifiedTime().getMicroSecPart());
        assertEquals(newLastModifiedFile, info2f.getLastAccessTime().getSecs());
        assertEquals(lastMicros, info2f.getLastAccessTime().getMicroSecPart());

        Thread.sleep(10);

        final Unix.Time now1 = Unix.getSystemTime();
        assertNotEquals(0, now1.getNanoSecPart() % 1_000);
        Unix.setLinkTimestamps(s.getAbsolutePath());
        final Unix.Time now2 = Unix.getSystemTime();
        final Stat info3 = Unix.getLinkInfo(s.getAbsolutePath());
        
        assertTrue(now1.getSecs() <= info3.getLastModified() && info3.getLastModified() <= now2.getSecs());
        assertTrue(now1.getMicroSecPart() <= info3.getLastModifiedTime().getMicroSecPart() && info.getLastModifiedTime().getMilliSecPart() <= now2.getMicroSecPart());
        assertTrue(now1.getSecs() <= info3.getLastAccess() && info3.getLastAccess() <= now2.getSecs());
        assertTrue(now1.getMicroSecPart() <= info3.getLastAccessTime().getMicroSecPart() && info.getLastAccessTime().getMilliSecPart() <= now2.getMicroSecPart());
        assertNotEquals(lastMicros, info3.getLastModifiedTime().getMicroSecPart());
        assertNotEquals(lastMicros, info3.getLastAccessTime().getMicroSecPart());

    }

    @Test(groups =
        { "requires_unix" })
    public void testTouchSymLinkAndFile() throws IOException, InterruptedException
    {
        Unix.setUseUnixRealtimeTimer(false);
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
        final long lastMicros = info.getLastModifiedTime().getMicroSecPart();
        final long newLastModifiedLink = info.getLastModifiedTime().getSecs() - 24 * 3600;
        Unix.setLinkTimestamps(s.getAbsolutePath(), newLastModifiedLink, lastMicros, newLastModifiedLink, lastMicros);

        final long newLastModifiedFile = info.getLastModifiedTime().getSecs() - 2 * 24 * 3600;
        Unix.setFileTimestamps(f.getAbsolutePath(), newLastModifiedFile, lastMicros, newLastModifiedFile, lastMicros);

        final Stat info2l = Unix.getLinkInfo(s.getAbsolutePath(), false);
        assertEquals(newLastModifiedLink, info2l.getLastModifiedTime().getSecs());
        assertEquals(lastMicros, info2l.getLastModifiedTime().getMicroSecPart());
        assertEquals(newLastModifiedLink, info2l.getLastAccessTime().getSecs());
        assertEquals(lastMicros, info2l.getLastAccessTime().getMicroSecPart());

        final Stat info2f = Unix.getFileInfo(s.getAbsolutePath());
        final Stat info2f2 = Unix.getLinkInfo(f.getAbsolutePath());
        assertNotEquals(info2l.getLastModifiedTime(), info2f2.getLastModifiedTime());
        assertEquals(info2f2.getLastModifiedTime(), info2f.getLastModifiedTime());
        assertEquals(newLastModifiedFile, info2f.getLastModifiedTime().getSecs());
        assertEquals(lastMicros, info2f.getLastModifiedTime().getMicroSecPart());
        assertEquals(newLastModifiedFile, info2f.getLastAccessTime().getSecs());
        assertEquals(lastMicros, info2f.getLastAccessTime().getMicroSecPart());


        Thread.sleep(10);

        final Unix.Time now1 = Unix.getSystemTime();
        assertEquals(0, now1.getNanoSecPart() % 1_000);
        Unix.setLinkTimestamps(s.getAbsolutePath());
        final Unix.Time now2 = Unix.getSystemTime();
        final Stat info3 = Unix.getLinkInfo(s.getAbsolutePath());
        
        assertTrue(now1.getSecs() <= info3.getLastModified() && info3.getLastModified() <= now2.getSecs());
        assertTrue(now1.getMicroSecPart() <= info3.getLastModifiedTime().getMicroSecPart() && info.getLastModifiedTime().getMilliSecPart() <= now2.getMicroSecPart());
        assertTrue(now1.getSecs() <= info3.getLastAccess() && info3.getLastAccess() <= now2.getSecs());
        assertTrue(now1.getMicroSecPart() <= info3.getLastAccessTime().getMicroSecPart() && info.getLastAccessTime().getMilliSecPart() <= now2.getMicroSecPart());
        assertNotEquals(lastMicros, info3.getLastModifiedTime().getMicroSecPart());
        assertNotEquals(lastMicros, info3.getLastAccessTime().getMicroSecPart());

    }

    @Test(groups =
        { "requires_unix" })
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
        assertEquals("No such file or directory", Unix.getLastError());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetLinkInfoNonExistent() throws IOException
    {
        final File s = new File(workingDirectory, "nonExistent");
        final Stat info = Unix.tryGetLinkInfo(s.getAbsolutePath());
        assertNull(info);
        assertEquals("No such file or directory", Unix.getLastError());
        final Stat info2 = Unix.tryGetFileInfo(s.getAbsolutePath());
        assertNull(info2);
        assertEquals("No such file or directory", Unix.getLastError());
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testCreateSymbolicLinkNull() throws IOException
    {
        Unix.createSymbolicLink(null, null);
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testCreateHardLinkNull() throws IOException
    {
        Unix.createHardLink(null, null);
    }

    @Test(groups =
        { "requires_unix" })
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

    @Test(groups =
        { "requires_unix" })
    public void testGetUid()
    {
        assertTrue(Unix.getUid() >= 0);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetEuid()
    {
        assertTrue(Unix.getEuid() >= 0);
        assertEquals(Unix.getUid(), Unix.getEuid());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetGid()
    {
        assertTrue(Unix.getGid() >= 0);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetEgid()
    {
        assertTrue(Unix.getEgid() >= 0);
        assertEquals(Unix.getGid(), Unix.getEgid());
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetUidForUserName()
    {
        assertEquals(0, Unix.getUidForUserName("root"));
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testGetUidForUserNameNull() throws IOException
    {
        Unix.getUidForUserName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testGetGidForGroupName()
    {
        final String rootGroup = Unix.tryGetGroupNameForGid(0);
        assertTrue(rootGroup, "root".equals(rootGroup) || "wheel".equals(rootGroup));
        assertEquals(0, Unix.getGidForGroupName(rootGroup));
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testGetGidForGroupNameNull() throws IOException
    {
        Unix.getGidForGroupName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetGroupByName()
    {
        final String rootGroup = Unix.tryGetGroupNameForGid(0);
        final Group group = Unix.tryGetGroupByName(rootGroup);
        assertNotNull(group);
        assertEquals(rootGroup, group.getGroupName());
        assertEquals(0, group.getGid());
        assertNotNull(group.getGroupMembers());
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testTryGetGroupByNameNull() throws IOException
    {
        Unix.tryGetGroupByName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetGroupByGid()
    {
        final Group group = Unix.tryGetGroupByGid(0);
        assertNotNull(group);
        final String rootGroup = group.getGroupName();
        assertTrue(rootGroup, "root".equals(rootGroup) || "wheel".equals(rootGroup));
        assertEquals(0, group.getGid());
        assertNotNull(group.getGroupMembers());
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetUserByName()
    {
        final Password user = Unix.tryGetUserByName("root");
        assertNotNull(user);
        assertEquals("root", user.getUserName());
        assertEquals(0, user.getUid());
        assertEquals(0, user.getGid());
        assertNotNull(user.getUserFullName());
        assertNotNull(user.getHomeDirectory());
        assertNotNull(user.getShell());
        assertTrue(user.getShell().startsWith("/"));
    }

    @Test(groups =
        { "requires_unix" }, expectedExceptions = NullPointerException.class)
    public void testTryGetUserByNameNull() throws IOException
    {
        Unix.tryGetUserByName(null);
    }

    @Test(groups =
        { "requires_unix" })
    public void testTryGetUserByUid()
    {
        final Password user = Unix.tryGetUserByUid(0);
        assertNotNull(user);
        assertEquals("root", user.getUserName());
        assertEquals(0, user.getUid());
        assertEquals(0, user.getGid());
        assertNotNull(user.getUserFullName());
        assertNotNull(user.getHomeDirectory());
        assertNotNull(user.getShell());
        assertTrue(user.getShell().startsWith("/"));
    }

    @Test(groups =
        { "requires_unix" })
    public void testDetectProcess()
    {
        assertTrue(Unix.canDetectProcesses());
        assertTrue(Unix.isProcessRunningPS(Unix.getPid()));
    }

    public static void main(String[] args) throws Throwable
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
        System.out.println("Test class: " + UnixTests.class.getSimpleName());
        System.out.println();
        if (Unix.isOperational() == false)
        {
            System.err.println("No unix library found.");
            System.exit(1);
        }
        boolean stopOnError = args.length > 0 && "stopOnError".equalsIgnoreCase(args[0]);
        int failed = 0;
        final UnixTests test = new UnixTests();
        try
        {
            for (Method m : UnixTests.class.getMethods())
            {
                final Test testAnnotation = m.getAnnotation(Test.class);
                if (testAnnotation == null)
                {
                    continue;
                }
                System.out.println("Running " + m.getName());
                test.setUp();
                try
                {
                    m.invoke(test);
                } catch (InvocationTargetException wrapperThrowable)
                {
                    final Throwable th = wrapperThrowable.getCause();
                    boolean exceptionFound = false;
                    for (Class<?> expectedExClazz : testAnnotation.expectedExceptions())
                    {
                        if (expectedExClazz == th.getClass())
                        {
                            exceptionFound = true;
                            break;
                        }
                    }
                    if (exceptionFound == false)
                    {
                        ++failed;
                        System.out.println("Caught exception in method " + m.getName());
                        th.printStackTrace();
                        if (stopOnError)
                        {
                            System.exit(1);
                        }
                    }
                }
            }
            if (failed == 0)
            {
                System.out.println("Tests OK!");
            } else
            {   
                System.out.printf("%d tests FAILED!\n", failed);
            }
        } finally
        {
            if (failed == 0)
            {
                test.afterClass();
            }
        }
    }

}
