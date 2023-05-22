/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;

/**
 * Utilities for handling Java resources.
 * 
 * @author Bernd Rinn
 */
public class ResourceUtilities
{

    /**
     * Tries to copy the resource with the given name to a temporary file.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be copied.
     */
    public static String tryCopyResourceToTempFile(final String resource, final String prefix,
            final String postfix)
    {
        return tryCopyResourceToTempFile(resource, prefix, postfix, true, true, null);
    }

    /**
     * Tries to copy the resource with the given name to a temporary file. The file will be deleted on program exit.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @param cleanUpOldResources If <code>true</code>, remove old leftover temporary files for this <var>prefix</var> and <var>postfix</var>.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be copied.
     */
    public static String tryCopyResourceToTempFile(final String resource, final String prefix,
            final String postfix, final boolean cleanUpOldResources)
    {
        return tryCopyResourceToTempFile(resource, prefix, postfix, cleanUpOldResources, true, null);
    }
    
    /**
     * Tries to copy the resource with the given name to a temporary file. The file will be deleted on program exit.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @param cleanUpOldResources If <code>true</code>, remove old leftover temporary files for this <var>prefix</var> and <var>postfix</var>.
     * @param verbose If <code>true</code>, print error to <code>stderr</code> if copying fails.
     * @param logPrefixOrNull If <code>verbose == true</code>, a prefix for logging failure conditions.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be copied.
     */
    public static String tryCopyResourceToTempFile(final String resource, final String prefix,
            final String postfix, final boolean cleanUpOldResources, final boolean verbose, final String logPrefixOrNull)
    {
        try
        {
            return copyResourceToFileImpl(resource, null, prefix, postfix, cleanUpOldResources);
        } catch (final Exception ex)
        {
            if (verbose)
            {
                System.err.printf("%sFAILURE to copy resource '%s' to temporary file.\n", (logPrefixOrNull != null) ? logPrefixOrNull : "", resource);
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Copies the resource with the given name to a temporary file. The file will be deleted on program exit.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @return The name of the temporary file.
     * @throws IllegalArgumentException If the resource cannot be found in the class path.
     * @throws IOExceptionUnchecked If an {@link IOException} occurs.
     */
    public static String copyResourceToTempFile(final String resource, final String prefix,
            final String postfix) throws IOExceptionUnchecked
    {
        return copyResourceToFileImpl(resource, null, prefix, postfix, false);
    }

    /**
     * Copies the resource with the given name to a temporary file. The file will be deleted on program exit.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @param cleanUpOldResources If <code>true</code>, remove old leftover temporary files for this <var>prefix</var> and <var>postfix</var>.
     * @return The name of the temporary file.
     * @throws IllegalArgumentException If the resource cannot be found in the class path.
     * @throws IOExceptionUnchecked If an {@link IOException} occurs.
     */
    public static String copyResourceToTempFile(final String resource, final Path destinationOrNull, final String prefix,
            final String postfix, final boolean cleanUpOldResources) throws IOExceptionUnchecked
    {
        return copyResourceToFileImpl(resource, null, prefix, postfix, cleanUpOldResources);
    }

    /**
     * Tries to copy the resource with the given name to a file on the file system.
     * <p>
     * This method catches all exceptions and returns a status flag.
     * 
     * @param resource The name of the resource to copy.
     * @param filename The name of the file to copy the resource content to.
     * @param randomAccessFile The destination file to copy the resource content to.
     * @param verbose If <code>true</code>, print error information to <code>stderr</code> if the copying fails.
     * @param logPrefixOrNull If <code>verbose == true</code>, a prefix for logging failure conditions.
     * @return <code>true</code> if the copying was successfull and <code>false</code> otherwise.
     */
    public static boolean tryCopyResourceToFile(final String resource, final Path filename, final RandomAccessFile randomAccessFile, boolean verbose,
            String logPrefixOrNull) throws IOExceptionUnchecked
    {
        try
        {
            copyResourceToFileImpl(resource, randomAccessFile, null, null, false);
            return true;
        } catch (Exception ex)
        {
            if (verbose)
            {
                System.err.printf("%sFAILURE to copy resource '%s' to file '%s'.\n", (logPrefixOrNull != null) ? logPrefixOrNull : "", resource,
                        filename);
                ex.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Copies the resource with the given name to a file on the file system..
     * 
     * @param resource The name of the resource to copy.
     * @param randomAccessFile The destination file to copy the resource content to.
     * @throws IllegalArgumentException If the resource cannot be found in the class path.
     * @throws IOExceptionUnchecked If an {@link IOException} occurs.
     */
    public static void copyResourceToFile(final String resource, final RandomAccessFile randomAccessFile) throws IOExceptionUnchecked
    {
        copyResourceToFileImpl(resource, randomAccessFile, null, null, false);
    }

    private static String copyResourceToFileImpl(final String resource, final RandomAccessFile randomAccessFileOrNull, final String prefix,
            final String postfix, final boolean cleanUpOldResources) throws IOExceptionUnchecked
    {
        if (cleanUpOldResources)
        {
            deleteOldResourceTempFiles(prefix, postfix);
        }
        try
        {
            try (final InputStream resourceStream = ResourceUtilities.class.getResourceAsStream(resource))
            {
                if (resourceStream == null)
                {
                    throw new IllegalArgumentException("Resource '" + resource + "' not found.");
                }
                final boolean createTmpFile = (randomAccessFileOrNull == null);
                final File fileOrNull = createTmpFile ? File.createTempFile(prefix, postfix) : null;
                if (createTmpFile)
                {
                    fileOrNull.deleteOnExit();
                }
                try (final OutputStream fileStream =
                        (fileOrNull == null) ? Channels.newOutputStream(randomAccessFileOrNull.getChannel()) : new FileOutputStream(fileOrNull))
                {
                    final long numberBytesInStream = IOUtils.copyLarge(resourceStream, fileStream);
                    if (randomAccessFileOrNull != null)
                    {
                        randomAccessFileOrNull.setLength(numberBytesInStream);
                    }
                    fileStream.close();
                }
                return createTmpFile ? fileOrNull.getAbsolutePath() : null;
            }
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static void deleteOldResourceTempFiles(final String prefix, final String postfix)
    {
        final FilenameFilter filter = new WildcardFileFilter(prefix + "*" + postfix);
        for (File file : new File(System.getProperty("java.io.tmpdir")).listFiles(filter))
        {
            file.delete();
        }
    }
}
