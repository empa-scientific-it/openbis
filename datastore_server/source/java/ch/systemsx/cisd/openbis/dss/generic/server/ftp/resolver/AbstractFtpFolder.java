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

import java.io.IOException;
import java.io.InputStream;

/**
 * A convenience abstract implementation for an ftp folder.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractFtpFolder extends AbstractFtpFile
{

    public AbstractFtpFolder(String absolutePath)
    {
        super(absolutePath);
    }

    public boolean isDirectory()
    {
        return true;
    }

    public boolean isFile()
    {
        return false;
    }

    public InputStream createInputStream(long arg0) throws IOException
    {
        return null;
    }

    public long getSize()
    {
        return 0;
    }

    public long getLastModified()
    {
        return System.currentTimeMillis();
    }

}
