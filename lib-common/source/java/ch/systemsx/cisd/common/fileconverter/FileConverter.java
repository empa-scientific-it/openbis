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

package ch.systemsx.cisd.common.fileconverter;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.FailureRecord;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * The base class for file conversion.
 * 
 * @author Bernd Rinn
 */
public class FileConverter
{
    @Private
    static final int MAX_RETRY_OF_FAILED_COMPRESSIONS = 3;

    static
    {
        LogInitializer.init();
    }

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            FileConverter.class);

    private static List<File> tryGetFilesToProcess(final File directory,
            final IFileConversionStrategy conversionStrategy)
    {
        return FileUtilities.listFiles(directory, new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return conversionStrategy.tryCheckConvert(pathname) != null;
                }
            }, true, null, new Log4jSimpleLogger(machineLog));
    }

    /**
     * Performs the conversion described by <var>conversionStrategy</var> on all files in <var>directoryName</var>.
     * <p>
     * Uses #cores * <var>machineLoad</var> threads for the conversion, but not more than <var>maxThreads</var>.
     * 
     * @return error message if everything went ok or null otherwise.
     */
    public static String performConversion(File directory,
            IFileConversionStrategy conversionStrategy, double machineLoad, int maxThreads)
            throws InterruptedExceptionUnchecked, EnvironmentFailureException
    {
        conversionStrategy.getConverter().check();
        List<File> itemsToProcess = tryGetFilesToProcess(directory, conversionStrategy);
        ITaskExecutor<File> taskExecutor = new FileConversionTaskExecutor(conversionStrategy);
        Collection<FailureRecord<File>> failureReport =
                ParallelizedExecutor.process(itemsToProcess, taskExecutor, machineLoad, maxThreads,
                        "File conversion", MAX_RETRY_OF_FAILED_COMPRESSIONS, false);
        return ParallelizedExecutor.tryFailuresToString(failureReport);
    }

    private FileConverter()
    {
        // Do not instantiate.
    }
}
