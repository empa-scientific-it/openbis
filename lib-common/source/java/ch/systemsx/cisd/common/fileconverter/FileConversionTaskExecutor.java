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

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A worker {@link Runnable} for (image) compression.
 * 
 * @author Bernd Rinn
 */
class FileConversionTaskExecutor implements ITaskExecutor<File>
{
    @Private
    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileConversionTaskExecutor.class);

    private static final String INPROGRESS_MARKER = ".CONVERSION_IN_PROGRESS_";

    private static final String CONVERTED_MARKER = ".CONVERTED_";

    private File prefixInProgress(File file)
    {
        assert file != null;

        return new File(file.getParent(), INPROGRESS_MARKER + file.getName());
    }

    private File prefixConverted(File file)
    {
        assert file != null;

        return new File(file.getParent(), CONVERTED_MARKER + file.getName());
    }

    private File tryRemovePrefix(File file)
    {
        assert file != null;

        final String name = file.getName();
        if (name.startsWith(INPROGRESS_MARKER))
        {
            return new File(file.getParent(), name.substring(INPROGRESS_MARKER.length()));
        } else if (name.startsWith(CONVERTED_MARKER))
        {
            return new File(file.getParent(), name.substring(CONVERTED_MARKER.length()));
        } else
        {
            return null;
        }
    }

    private boolean isConvertedFile(File fileToCompress)
    {
        return fileToCompress.getName().startsWith(CONVERTED_MARKER);
    }

    private boolean isInProgressFile(File fileToCompress)
    {
        return fileToCompress.getName().startsWith(INPROGRESS_MARKER);
    }

    private Status createStatus(String msgTemplate, Object... params)
    {
        final String msg = String.format(msgTemplate, params);
        return Status.createError(msg);
    }

    private final IFileConversionStrategy conversionStrategy;

    FileConversionTaskExecutor(final IFileConversionStrategy conversionStrategy)
    {
        assert conversionStrategy != null;

        this.conversionStrategy = conversionStrategy;
    }

    @Override
    public Status execute(File fileToConvert)
    {
        final File convertedFile = conversionStrategy.tryCheckConvert(fileToConvert);
        if (convertedFile == null)
        {
            return createStatus("IllegalFile: '%s'", fileToConvert.getAbsolutePath());
        }
        if (convertedFile.equals(fileToConvert))
        {
            return convertInPlace(fileToConvert);
        } else
        {
            return convert(fileToConvert, convertedFile, conversionStrategy.deleteOriginalFile());
        }
    }

    private Status convert(File fileToConvert, File convertedFile, boolean deleteOriginalFile)
    {
        // Clean up
        if (isInProgressFile(fileToConvert))
        {
            return deleteOriginalFile(fileToConvert);
        }
        if (convertedFile.exists() && convertedFile.length() > 0)
        {
            return deleteOriginalFileIfRequestedAndGetStatus(fileToConvert, deleteOriginalFile);
        }

        // Convert
        final File inProgressFile = prefixInProgress(convertedFile);
        final boolean runOK =
                conversionStrategy.getConverter().convert(fileToConvert, inProgressFile);
        if (runOK == false)
        {
            return createStatus("Unable to convert '%s'.", fileToConvert.getAbsolutePath());
        }
        final boolean renameOK = inProgressFile.renameTo(convertedFile);
        if (renameOK == false)
        {
            return createStatus("Unable to rename '%s' to '%s'.",
                    inProgressFile.getAbsolutePath(), convertedFile.getAbsolutePath());
        }
        return deleteOriginalFileIfRequestedAndGetStatus(fileToConvert, deleteOriginalFile);
    }

    private Status convertInPlace(File fileToConvert)
    {
        // Clean up
        if (isInProgressFile(fileToConvert))
        {
            return deleteOriginalFile(fileToConvert);
        }
        if (isConvertedFile(fileToConvert))
        {
            final File originalFile = tryRemovePrefix(fileToConvert);
            assert originalFile != null;
            if (originalFile.exists())
            {
                final boolean ok = originalFile.delete();
                if (ok == false)
                {
                    return createStatus(
                            "Clean up: Unable to delete original file '%s' failed.", originalFile);
                }
            }
            if (fileToConvert.renameTo(originalFile))
            {
                return Status.OK;
            } else
            {
                return createStatus(
                        "Renaming converted file '%s' to original name '%s' failed.",
                        fileToConvert, originalFile);
            }
        }

        // Convert
        final File inProgressFile = prefixInProgress(fileToConvert);
        final File convertedFile = prefixConverted(fileToConvert);
        final boolean runOK =
                conversionStrategy.getConverter().convert(fileToConvert, inProgressFile);
        if (runOK == false)
        {
            return createStatus("Unable to convert '%s'.", fileToConvert.getAbsolutePath());
        }
        final boolean firstRenameOK = inProgressFile.renameTo(convertedFile);
        if (firstRenameOK == false)
        {
            return createStatus("Unable to rename '%s' to '%s'.",
                    inProgressFile.getAbsolutePath(), convertedFile.getAbsolutePath());
        }
        final boolean removalOfOriginalOK = fileToConvert.delete();
        if (removalOfOriginalOK == false)
        {
            return createStatus("Unable to delete original file '%s'",
                    fileToConvert.getAbsolutePath());
        }
        final boolean secondRenameOK = convertedFile.renameTo(fileToConvert);
        if (secondRenameOK == false)
        {
            return createStatus("Unable to rename '%s' to '%s'.",
                    convertedFile.getAbsolutePath(), fileToConvert.getAbsolutePath());
        }
        return Status.OK;
    }

    private Status deleteOriginalFileIfRequestedAndGetStatus(File fileToConvert,
            boolean deleteOriginalFile)
    {
        if (deleteOriginalFile && fileToConvert.exists())
        {
            return deleteOriginalFile(fileToConvert);
        }
        return Status.OK;
    }

    private Status deleteOriginalFile(File fileToConvert)
    {
        final boolean ok = fileToConvert.delete();
        if (ok)
        {
            operationLog.info(String.format("Clean up: successfully deleting temporary file '%s'",
                    fileToConvert.getAbsolutePath()));
            return Status.OK;
        } else
        {
            return createStatus("Clean up: Unable to delete temporary file '%s'",
                    fileToConvert.getAbsolutePath());
        }
    }

}
