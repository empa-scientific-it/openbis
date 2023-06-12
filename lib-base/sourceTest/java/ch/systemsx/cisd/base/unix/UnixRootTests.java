package ch.systemsx.cisd.base.unix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.base.unix.Unix.Stat;

/**
 * Tests of the {@link Unix} class that can only be performed as user root.
 */
public class UnixRootTests extends AbstractFileSystemTestCase
{
    @Test(groups =
            { "requires_unix" })
    public void testChown() throws IOException
    {
        if (Unix.getUid() != 0)
        {
            System.out.println("Skipping test as we are not root.");
            return;
        }
        final short accessMode = (short) 0777;
        final String content = "someText\n";
        final File f = new File(workingDirectory, "someFile");
        final File s = new File(workingDirectory, "MyLink");
        FileUtils.writeStringToFile(f, content, Charset.defaultCharset());
        Unix.setAccessMode(f.getAbsolutePath(), accessMode);
        final Stat info = Unix.getLinkInfo(f.getAbsolutePath());
        Unix.setOwner(f.getAbsolutePath(), info.getUid(), info.getGid());
        assertEquals(1, info.getNumberOfHardLinks());
        assertEquals(content.length(), info.getSize());
        assertEquals(accessMode, info.getPermissions());
        final Unix.Password nobody = Unix.tryGetUserByName("nobody");
        assertNotNull(nobody);
        final Unix.Password daemon = Unix.tryGetUserByName("daemon");
        assertNotNull(daemon);
        Unix.setOwner(f.getAbsolutePath(), nobody);
        Unix.createSymbolicLink(f.getAbsolutePath(), s.getAbsolutePath());
        Unix.setLinkOwner(s.getAbsolutePath(), daemon);

        final Unix.Stat fileInfo = Unix.getFileInfo(s.getAbsolutePath());
        assertEquals(nobody.getUid(), fileInfo.getUid());
        assertEquals(nobody.getGid(), fileInfo.getGid());

        final Unix.Stat linkInfo = Unix.getLinkInfo(s.getAbsolutePath());
        assertEquals(daemon.getUid(), linkInfo.getUid());
        assertEquals(daemon.getGid(), linkInfo.getGid());
    }

    public static void main(String[] args)  throws Throwable
    {
        System.out.println(BuildAndEnvironmentInfo.INSTANCE);
        System.out.println("Test class: " + UnixRootTests.class.getSimpleName());
        System.out.println();
        if (Unix.isOperational() == false)
        {
            System.err.println("No unix library found.");
            System.exit(1);
        }
        final UnixRootTests test = new UnixRootTests();
        try
        {
            for (Method m : UnixRootTests.class.getMethods())
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
                        throw th;
                    }
                }
            }
            System.out.println("Tests OK!");
        } finally
        {
            test.afterClass();
        }
    }

}
