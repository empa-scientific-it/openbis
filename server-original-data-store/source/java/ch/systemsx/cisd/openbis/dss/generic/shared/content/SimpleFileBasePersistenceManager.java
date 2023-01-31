/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.string.StringUtilities;

/**
 * Simple implementation of {@link IPersistenceManager} using a file. Persistence request are processed immediately.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleFileBasePersistenceManager implements
        IPersistenceManager
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SimpleFileBasePersistenceManager.class);

    private final File file;

    private final File fileNew;

    private final String nameOfObject;

    private Serializable object;

    /**
     * Creates an instance for the specified file storing the object.
     *
     * @param nameOfObject Name of the object. Will be used in exception messages.
     */
    public SimpleFileBasePersistenceManager(File file, String nameOfObject)
    {
        this.file = file;
        this.nameOfObject = nameOfObject;
        fileNew = new File(file.getParentFile(), file.getName() + ".new");
    }

    @Override
    public Serializable load(Serializable defaultObject)
    {
        if (defaultObject == null)
        {
            throw new IllegalArgumentException("Unspecified default object.");
        }
        object = doLoad(defaultObject);
        return object;
    }

    private Serializable doLoad(Serializable defaultObject)
    {
        if (file.exists() == false)
        {
            return defaultObject;
        }
        try
        {
            return FileUtilities.loadToObject(file, Serializable.class);
        } catch (Exception e)
        {
            operationLog.warn(StringUtilities.capitalize(nameOfObject)
                    + " couldn't be reloaded from file " + file + ": " + e);
            return defaultObject;
        }
    }

    @Override
    public void requestPersistence()
    {
        synchronized (object)
        {
            File folder = fileNew.getParentFile();
            if (folder.exists() == false)
            {
                boolean result = folder.mkdirs();
                if (result == false)
                {
                    throw new EnvironmentFailureException("Couldn't create folder: " + folder);
                }
            }
            FileUtilities.writeToFile(fileNew, object);
            fileNew.renameTo(file);
        }
    }

}
