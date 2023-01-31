/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.ftpserver.ftplet.FtpFile;

/**
 * @author Bernd Rinn
 */
public class NonExistingFtpFile implements FtpFile
{
    public static void throwFileNotFoundExceptionIfNonExistingFtpFile(FtpFile file) throws IOException
    {
        if (file instanceof NonExistingFtpFile)
        {
            throw new NoSuchFileException(file.getAbsolutePath());
        }
    }

    private final String path;

    private final String errorMsgOrNull;

    public NonExistingFtpFile(String path, String errorMsgOrNull)
    {
        this.path = path;
        this.errorMsgOrNull = errorMsgOrNull;
    }

    public String getErrorMessage()
    {
        return errorMsgOrNull;
    }

    @Override
    public String getAbsolutePath()
    {
        return path;
    }

    @Override
    public String getName()
    {
        return FilenameUtils.getName(path);
    }

    @Override
    public boolean isHidden()
    {
        return false;
    }

    @Override
    public boolean isDirectory()
    {
        return false;
    }

    @Override
    public boolean isFile()
    {
        return false;
    }

    @Override
    public boolean doesExist()
    {
        return false;
    }

    @Override
    public boolean isReadable()
    {
        return false;
    }

    @Override
    public boolean isWritable()
    {
        return false;
    }

    @Override
    public boolean isRemovable()
    {
        return false;
    }

    @Override
    public String getOwnerName()
    {
        return "UNKNOWN";
    }

    @Override
    public String getGroupName()
    {
        return "UNKNOWN";
    }

    @Override
    public int getLinkCount()
    {
        return 0;
    }

    @Override
    public long getLastModified()
    {
        return 0;
    }

    @Override
    public boolean setLastModified(long time)
    {
        return false;
    }

    @Override
    public long getSize()
    {
        return 0;
    }

    @Override
    public boolean mkdir()
    {
        return false;
    }

    @Override
    public boolean delete()
    {
        return false;
    }

    @Override
    public boolean move(FtpFile destination)
    {
        return false;
    }

    @Override
    public List<FtpFile> listFiles()
    {
        return Collections.emptyList();
    }

    @Override
    public OutputStream createOutputStream(long offset) throws IOException
    {
        if (errorMsgOrNull != null)
        {
            throw new IOException("File '" + path + "' does not exist ("
                    + errorMsgOrNull + ".");
        } else
        {
            throw new IOException("File '" + path + "' does not exist.");
        }
    }

    @Override
    public InputStream createInputStream(long offset) throws IOException
    {
        if (errorMsgOrNull != null)
        {
            throw new IOException("File '" + path + "' does not exist ("
                    + errorMsgOrNull + ".");
        } else
        {
            throw new IOException("File '" + path + "' does not exist.");
        }
    }

    @Override
    public String toString()
    {
        return path + (errorMsgOrNull == null ? "" : " (Error: " + errorMsgOrNull + ")");
    }
}