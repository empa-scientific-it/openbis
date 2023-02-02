/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * An {@link IFileRemover} decorator that logs path removals.
 * 
 * @author Bernd Rinn
 */
public class LoggingPathRemoverDecorator implements IFileRemover
{
    private final IFileRemover delegate;

    private final ISimpleLogger logger;

    private final boolean failuresOnly;

    public LoggingPathRemoverDecorator(IFileRemover delegate, ISimpleLogger logger,
            boolean failuresOnly)
    {
        this.delegate = delegate;
        this.logger = logger;
        this.failuresOnly = failuresOnly;
    }

    @Override
    public boolean removeRecursively(File fileToRemove)
    {
        final boolean ok = delegate.removeRecursively(fileToRemove);
        if (shouldLog(ok))
        {
            logger.log(ok ? LogLevel.INFO : LogLevel.ERROR, String.format("Deleting %s '%s': %s.",
                    getType(fileToRemove), fileToRemove.getPath(), ok ? "OK" : "FAILED"));
        }
        return ok;
    }

    private boolean shouldLog(final boolean ok)
    {
        return (ok && failuresOnly) == false;
    }

    private String getType(File fileToRemove)
    {
        return fileToRemove.isDirectory() ? "directory" : "file";
    }

}
