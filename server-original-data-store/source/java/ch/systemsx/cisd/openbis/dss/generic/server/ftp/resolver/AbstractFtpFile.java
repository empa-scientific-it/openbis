/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;

/**
 * A convenience abstract implementation for an ftp folder.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractFtpFile implements FtpFile
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractFtpFile.class);

    protected final String absolutePath;
    
    private long lastModified;

    public AbstractFtpFile(String absolutePath)
    {
        this.absolutePath = absolutePath;
    }

    /**
     * Base implementation of the {@link #listFiles()} method that makes sure no unchecked exceptions are being propagated to the surrounding Apache
     * FTP classes. Apache FTP swallows exceptions without logging and thereby hinders error analysis.
     */
    @Override
    public final List<FtpFile> listFiles()
    {
        try
        {
            return unsafeListFiles();
        } catch (RuntimeException rex)
        {
            operationLog.error("Error while listing files for FTP :" + rex.getMessage(), rex);
            throw rex;
        }
    }

    /**
     * implementers are free of unchecked exception handling.
     */
    public abstract List<FtpFile> unsafeListFiles() throws RuntimeException;

    @Override
    public boolean doesExist()
    {
        return true;
    }

    @Override
    public String getAbsolutePath()
    {
        return absolutePath;
    }

    @Override
    public String getName()
    {
        return new File(absolutePath).getName();
    }

    @Override
    public String getOwnerName()
    {
        return FtpConstants.FTP_USER_NAME;
    }

    @Override
    public String getGroupName()
    {
        return FtpConstants.FTP_USER_GROUP_NAME;
    }

    @Override
    public boolean isHidden()
    {
        return false;
    }

    @Override
    public boolean isReadable()
    {
        return true;
    }

    @Override
    public boolean isRemovable()
    {
        return false;
    }

    @Override
    public boolean isWritable()
    {
        return false;
    }

    @Override
    public boolean setLastModified(long arg0)
    {
        lastModified = arg0;
        return true;
    }

    @Override
    public long getLastModified()
    {
        return lastModified;
    }
    
    // =================================
    // Unsupported operations
    // =================================

    @Override
    public OutputStream createOutputStream(long arg0) throws IOException
    {
        return null;
    }

    @Override
    public boolean delete()
    {
        return false;
    }

    @Override
    public boolean mkdir()
    {
        return false;
    }

    @Override
    public boolean move(FtpFile arg0)
    {
        return false;
    }

    @Override
    public int getLinkCount()
    {
        return 0;
    }

}
