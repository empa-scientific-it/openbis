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
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.unix.FileLinkType;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

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
