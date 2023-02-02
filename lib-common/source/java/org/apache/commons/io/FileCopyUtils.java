/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.concurrent.IActivityObserver;

/**
 * General file copying utilities.
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils, CISD common
 * 
 * @author <a href="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:Christoph.Reck@dlr.de">Christoph.Reck</a>
 * @author <a href="mailto:peter@apache.org">Peter Donald</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @author Matthew Hawthorne
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @author Stephen Colebourne
 * @author Ian Springer
 * @author Chris Eldredge
 * @author Jim Harrington
 * @author Niall Pemberton
 * @author Sandy McArthur
 * @author Bernd Rinn
 */
public class FileCopyUtils
{

    private FileCopyUtils()
    {
        // Not to be instantiated.
    }

    /**
     * Copies a file or directory to a new location, preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param source an existing file to copy, must not be <code>null</code>
     * @param destination the new file, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     */
    public static void copy(File source, File destination) throws IOException
    {
        if (source.isDirectory())
        {
            copyDirectory(source, destination);
        } else
        {
            copyFile(source, destination);
        }
    }

    /**
     * Copies a file or directory to a new location, preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param source an existing file to copy, must not be <code>null</code>
     * @param destination the new file, must not be <code>null</code>
     * @param observerOrNull activity observer of the copy process
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     */
    public static void copy(File source, File destination, IActivityObserver observerOrNull)
            throws IOException
    {
        if (source.isDirectory())
        {
            copyDirectory(source, destination, observerOrNull);
        } else
        {
            copyFile(source, destination, observerOrNull);
        }
    }

    /**
     * Copies a file or directory to a directory preserving the file or directory date.
     * <p>
     * This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination
     * directory is created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param source an existing file to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyToDirectory(File source, File destDir) throws IOException
    {
        if (source.isDirectory())
        {
            copyDirectoryToDirectory(source, destDir);
        } else
        {
            copyFileToDirectory(source, destDir);
        }
    }

    /**
     * Copies a file or directory to a directory preserving the file or directory date.
     * <p>
     * This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination
     * directory is created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param source an existing file to copy, must not be <code>null</code>
     * @param destination the directory to place the copy in, must not be <code>null</code>
     * @param observerOrNull activity observer of the copy process
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyToDirectory(File source, File destination,
            IActivityObserver observerOrNull) throws IOException
    {
        if (source.isDirectory())
        {
            copyDirectoryToDirectory(source, destination, observerOrNull);
        } else
        {
            copyFileToDirectory(source, destination);
        }
    }

    /**
     * Copies a file to a directory preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination
     * directory is created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.3
     */
    public static void copyFileToDirectory(File srcFile, File destDir) throws IOException
    {
        copyFileToDirectory(srcFile, destDir, null);
    }

    /**
     * Copies a file to a directory preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination
     * directory is created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @param observerOrNull the activity observer to notify about copy progress.
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyFileToDirectory(File srcFile, File destDir,
            IActivityObserver observerOrNull) throws IOException
    {
        if (destDir == null)
        {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && destDir.isDirectory() == false)
        {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        copyFile(srcFile, new File(destDir, srcFile.getName()), observerOrNull);
    }

    /**
     * Copies a file to a new location, preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destFile the new file, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyFile(File srcFile, File destFile) throws IOException
    {
        copyFile(srcFile, destFile, null);
    }

    /**
     * Copies a file to a new location, preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this method will overwrite it.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destFile the new file, must not be <code>null</code>
     * @param observerOrNull the activity observer to notify about copy progress.
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyFile(File srcFile, File destFile, IActivityObserver observerOrNull)
            throws IOException
    {
        if (srcFile == null)
        {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null)
        {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcFile.exists() == false)
        {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory())
        {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath()))
        {
            throw new IOException("Source '" + srcFile + "' and destination '" + destFile
                    + "' are the same");
        }
        if (destFile.getParentFile() != null && destFile.getParentFile().exists() == false)
        {
            if (destFile.getParentFile().mkdirs() == false)
            {
                throw new IOException("Destination '" + destFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && destFile.canWrite() == false)
        {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, observerOrNull);
    }

    /**
     * Internal copy file method.
     * 
     * @param srcFile the validated source file, must not be <code>null</code>
     * @param destFile the validated destination file, must not be <code>null</code>
     * @param observerOrNull the activity observer to notify about copy progress.
     * @throws IOException if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile, IActivityObserver observerOrNull)
            throws IOException
    {
        if (destFile.exists() && destFile.isDirectory())
        {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream input = new FileInputStream(srcFile);
        try
        {
            FileOutputStream output = new FileOutputStream(destFile);
            try
            {
                copy(input, output, observerOrNull);
                output.close();
                output = null;
            } finally
            {
                IOUtils.closeQuietly(output);
            }
        } finally
        {
            IOUtils.closeQuietly(input);
        }

        if (srcFile.length() != destFile.length())
        {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '"
                    + destFile + "'");
        }
        destFile.setLastModified(srcFile.lastModified());
    }

    /**
     * The default buffer size to use.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 16;

    private static long copy(InputStream input, OutputStream output,
            IActivityObserver observerOrNull)
            throws IOException
    {
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer)))
        {
            output.write(buffer, 0, n);
            count += n;
            update(observerOrNull);
        }
        return count;
    }

    private static void update(IActivityObserver observerOrNull)
    {
        if (observerOrNull != null)
        {
            observerOrNull.update();
        }
    }

    /**
     * Copies a directory to within another directory preserving the file dates.
     * <p>
     * This method copies the source directory and all its contents to a directory of the same name in the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.2
     */
    public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException
    {
        copyDirectoryToDirectory(srcDir, destDir, null);
    }

    /**
     * Copies a directory to within another directory preserving the file dates.
     * <p>
     * This method copies the source directory and all its contents to a directory of the same name in the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @param observerOrNull activity observer of the copy process
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.2
     */
    public static void copyDirectoryToDirectory(File srcDir, File destDir,
            IActivityObserver observerOrNull) throws IOException
    {
        if (srcDir == null)
        {
            throw new NullPointerException("Source must not be null");
        }
        if (srcDir.exists() && srcDir.isDirectory() == false)
        {
            throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
        }
        if (destDir == null)
        {
            throw new NullPointerException("Destination must not be null");
        }
        if (destDir.exists() && destDir.isDirectory() == false)
        {
            throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
        }
        copyDirectory(srcDir, new File(destDir, srcDir.getName()), null, observerOrNull);
    }

    /**
     * Copies a whole directory to a new location preserving the file dates.
     * <p>
     * This method copies the specified directory and all its child directories and files to the specified destination. The destination is the new
     * location and name of the directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the new directory, must not be <code>null</code>
     * @param observerOrNull activity observer of the copy process
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.1
     */
    public static void copyDirectory(File srcDir, File destDir, IActivityObserver observerOrNull)
            throws IOException
    {
        copyDirectory(srcDir, destDir, null, observerOrNull);
    }

    /**
     * Copies a whole directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the new directory, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.1
     */
    public static void copyDirectory(File srcDir, File destDir) throws IOException
    {
        copyDirectory(srcDir, destDir, (FileFilter) null);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * <h4>Example: Copy directories only</h4>
     * 
     * <pre>
     * // only copy the directory structure
     * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     * </pre>
     * 
     * <h4>Example: Copy directories and txt files</h4>
     * 
     * <pre>
     * // Create a filter for &quot;.txt&quot; files
     * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(&quot;.txt&quot;);
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     * // Create a filter for either directories or &quot;.txt&quot; files
     * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     * // Copy using the filter
     * FileUtils.copyDirectory(srcDir, destDir, filter, false);
     * </pre>
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the new directory, must not be <code>null</code>
     * @param filter the filter to apply, null means copy all directories and files
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.4
     */
    public static void copyDirectory(File srcDir, File destDir, FileFilter filter)
            throws IOException
    {
        copyDirectory(srcDir, destDir, filter, null);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory to within the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * <p>
     * The last modification time of the source is preserved in the copy.
     * <h4>Example: Copy directories only</h4>
     * 
     * <pre>
     * // only copy the directory structure
     * FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     * </pre>
     * 
     * <h4>Example: Copy directories and txt files</h4>
     * 
     * <pre>
     * // Create a filter for &quot;.txt&quot; files
     * IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(&quot;.txt&quot;);
     * IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     * // Create a filter for either directories or &quot;.txt&quot; files
     * FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     * // Copy using the filter
     * FileUtils.copyDirectory(srcDir, destDir, filter, false);
     * </pre>
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the new directory, must not be <code>null</code>
     * @param filter the filter to apply, null means copy all directories and files
     * @param observerOrNull activity observer of the copy process
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @since Commons IO 1.4
     */
    public static void copyDirectory(File srcDir, File destDir, FileFilter filter,
            IActivityObserver observerOrNull) throws IOException
    {
        if (srcDir == null)
        {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null)
        {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcDir.exists() == false)
        {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (srcDir.isDirectory() == false)
        {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath()))
        {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir
                    + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath()))
        {
            File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0)
            {
                exclusionList = new ArrayList<String>(srcFiles.length);
                for (int i = 0; i < srcFiles.length; i++)
                {
                    File copiedFile = new File(destDir, srcFiles[i].getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, exclusionList, observerOrNull);
    }

    /**
     * Internal copy directory method.
     * 
     * @param srcDir the validated source directory, must not be <code>null</code>
     * @param destDir the validated destination directory, must not be <code>null</code>
     * @param filter the filter to apply, null means copy all directories and files
     * @param exclusionList List of files and directories to exclude from the copy, may be null
     * @param observerOrNull activity observer of the copy process
     * @throws IOException if an error occurs
     */
    private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter,
            List<String> exclusionList, IActivityObserver observerOrNull) throws IOException
    {
        if (destDir.exists())
        {
            if (destDir.isDirectory() == false)
            {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else
        {
            if (destDir.mkdirs() == false)
            {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
            destDir.setLastModified(srcDir.lastModified());
        }
        if (destDir.canWrite() == false)
        {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        // recurse
        File[] files = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (files == null)
        { // null if security restricted
            throw new IOException("Failed to list contents of " + srcDir);
        }
        for (int i = 0; i < files.length; i++)
        {
            if (observerOrNull != null)
            {
                observerOrNull.update();
            }
            File copiedFile = new File(destDir, files[i].getName());
            if (exclusionList == null || !exclusionList.contains(files[i].getCanonicalPath()))
            {
                if (files[i].isDirectory())
                {
                    doCopyDirectory(files[i], copiedFile, filter, exclusionList, observerOrNull);
                } else
                {
                    doCopyFile(files[i], copiedFile, observerOrNull);
                }
            }
        }
        destDir.setLastModified(srcDir.lastModified());
    }
}
