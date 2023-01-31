package org.python.core;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class exists to expose the two-arg constructor of SyspathArchive and to hack getEntry to work when the jar file name doesn't end with .jar.
 * Fixed a bit to work with openBIS.
 * 
 * @author Kent Johnson
 */
public class SyspathArchiveHack extends SyspathArchive
{
    private static final long serialVersionUID = 694744188445154734L;

    private ZipFile zipfileToo;

    public SyspathArchiveHack(ZipFile zipFile, String archiveName) throws IOException
    {
        super(zipFile, archiveName);
        zipfileToo = zipFile;
    }

    @Override
    ZipEntry getEntry(String entryName)
    {
        return zipfileToo.getEntry("Lib/" + entryName);
    }
}
