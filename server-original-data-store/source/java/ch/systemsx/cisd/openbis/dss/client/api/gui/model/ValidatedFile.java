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

package ch.systemsx.cisd.openbis.dss.client.api.gui.model;

import java.io.File;

/**
 * @author Pawel Glyzewski
 */
public class ValidatedFile
{
    private File file;

    private long lastValidated;

    private boolean fileExisted = true;

    public ValidatedFile(File file)
    {
        this.file = file;
        this.lastValidated = file.lastModified();
    }

    public File getFile()
    {
        return file;
    }

    public long getLastValidated()
    {
        return lastValidated;
    }

    public boolean validationRequired()
    {
        if (fileExisted)
        {
            return false == file.exists() || lastValidated < file.lastModified();
        } else
        {
            return file.exists();
        }
    }

    public void markValidation()
    {
        fileExisted = file.exists();
        this.lastValidated = file.lastModified();
    }

    @Override
    public String toString()
    {
        return file.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof ValidatedFile)
        {
            return file.equals(((ValidatedFile) o).getFile());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return file.hashCode();
    }
}
