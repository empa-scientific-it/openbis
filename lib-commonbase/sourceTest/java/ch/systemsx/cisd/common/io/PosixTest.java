package ch.systemsx.cisd.common.io;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class PosixTest
{
    @Test
    public void testGetEuid()
    {
        int euid = Posix.getEuid();
    }

    @Test
    public void testGetUid()
    {
        int uid = Posix.getUid();
    }

    @Test
    public void testGetGid()
    {
        int gid = Posix.getGid();
    }

    @Test
    public void testSetOwner() throws IOException
    {
        File file = File.createTempFile("pre", "su");
        Posix.setOwner(file.getPath(), Posix.getUid(), Posix.getGid());
        file.delete();
    }
}
