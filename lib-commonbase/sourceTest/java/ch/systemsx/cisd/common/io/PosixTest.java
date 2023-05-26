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
    public void testTryGetGroupNameForGid() {
        int gid = Posix.getGid();
        String group = Posix.tryGetGroupNameForGid(gid);
    }

    @Test
    public void testSetOwner() throws IOException
    {
        File file = File.createTempFile("pre", "su");
        Posix.setOwner(file.getPath(), Posix.getUid(), Posix.getGid());
        file.delete();
    }

    @Test
    public void testTryGetLinkInfo() throws IOException {
        File file = File.createTempFile("pre", "su");
        Posix.Stat stat = Posix.tryGetLinkInfo(file.toString());
        file.delete();
    }
}
