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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileSystemUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IInputStream;
import ch.systemsx.cisd.base.io.IOutputStream;
import ch.systemsx.cisd.common.exceptions.UnknownLastChangedException;

/**
 * Operations on {@link File} that are good to monitor when being performed on remote file systems because they can hang when a remote resource goes
 * down unexpectedly.
 * 
 * @author Bernd Rinn
 */
public interface IFileOperations extends IFileRemover
{

    //
    // java.io.File
    //

    /**
     * @see File#getCanonicalPath()
     */
    public String getCanonicalPath(File file) throws IOExceptionUnchecked;

    /**
     * @see File#getCanonicalFile()
     */
    public File getCanonicalFile(File file) throws IOExceptionUnchecked;

    /**
     * @see File#canRead()
     */
    public boolean canRead(File file);

    /**
     * @see File#canWrite()
     */
    public boolean canWrite(File file);

    /**
     * @see File#exists()
     */
    public boolean exists(File file);

    /**
     * @see File#isDirectory()
     */
    public boolean isDirectory(File file);

    /**
     * @see File#isFile()
     */
    public boolean isFile(File file);

    /**
     * @see File#isHidden()
     */
    public boolean isHidden(File file);

    /**
     * @see File#lastModified()
     */
    public long lastModified(File file);

    /**
     * @see File#length()
     */
    public long length(File file);

    public boolean createNewFile(File file) throws IOExceptionUnchecked;

    /**
     * @see File#delete()
     */
    public boolean delete(File file);

    /**
     * @see File#list()
     */
    public String[] list(File file);

    /**
     * @see File#list(FilenameFilter)
     */
    public String[] list(File file, FilenameFilter filter);

    /**
     * @see File#listFiles()
     */
    public File[] listFiles(File file);

    /**
     * @see File#listFiles(FilenameFilter)
     */
    public File[] listFiles(File file, FilenameFilter filter);

    /**
     * @see File#listFiles(FileFilter)
     */
    public File[] listFiles(File file, FileFilter filter);

    /**
     * @see File#mkdir()
     */
    public boolean mkdir(File file);

    /**
     * @see File#mkdirs()
     */
    public boolean mkdirs(File file);

    /**
     * @see File#renameTo(File)
     */
    public boolean rename(File source, File destination);

    public boolean setLastModified(File file, long time);

    public boolean setReadOnly(File file);

    public File createTempFile(String prefix, String suffix, File directory)
            throws IOExceptionUnchecked;

    public File createTempFile(String prefix, String suffix) throws IOExceptionUnchecked;

    //
    // Advanced
    //

    /**
     * Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
     * <p>
     * This call is suitable for large or huge directories on slow file systems as it is able to notify the monitor of progress.
     * 
     * @param directory The directory to search in.
     * @param extensionsOrNull An array of extensions, ex. {"java","xml"}. If this parameter is <code>null</code>, all files are returned.
     * @param recursive If true all subdirectories are searched as well.
     * @return A list of java.io.File (all files) with the matching files, or an empty list, if <var>directory</var> ist not a directory.
     */
    public List<File> listFiles(File directory, String[] extensionsOrNull, boolean recursive);

    /**
     * Finds directories within a given directory (and optionally its subdirectories).
     * <p>
     * This call is suitable for large or huge directories on slow file systems as it is able to notify the monitor of progress.
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     */
    public List<File> listDirectories(File directory, boolean recursive);

    /**
     * Finds files or directories within a given directory (and optionally its subdirectories).
     * <p>
     * This call is suitable for large or huge directories on slow file systems as it is able to notify the monitor of progress.
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     */
    public List<File> listFilesAndDirectories(File directory, boolean recursive);

    /**
     * @return The time when any file in (or below) <var>path</var> has last been changed in the file system.
     * @throws UnknownLastChangedException if the <var>path</var> does not exist or is not readable.
     */
    public long lastChanged(final File path) throws UnknownLastChangedException;

    /**
     * Determines the time (in milliseconds since start of the epoch) when any item below <var>path</var> has last been changed in the file system.
     * 
     * @param path The path (file or directory) to check for last change.
     * @param subDirectoriesOnly If <code>true</code>, only subdirectories of <var>path</var> are checked, if <var>path</var> is a directory. If
     *            <var>path</var> is a file, this parameter is ignored. When considering what this parameter is good for, note that the mtime of a
     *            directory is changed when an entry in the directory changes.
     * @param stopWhenFindYounger If &gt; 0, the recursive search for younger file will be stopped when a file or directory is found that is younger
     *            than the time specified in this parameter. Supposed to be used when one does not care about the absolute youngest entry, but only,
     *            if there are entries that are "young enough".
     * @return The time when any file in (or below) <var>path</var> has last been changed in the file system.
     * @throws UnknownLastChangedException if the <var>path</var> does not exist or is not readable.
     */
    public long lastChanged(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYounger) throws UnknownLastChangedException;

    /**
     * Determines the time (in milliseconds since start of the epoch) when any item below <var>path</var> has last been changed in the file system.
     * 
     * @param path The path (file or directory) to check for last change.
     * @param subDirectoriesOnly If <code>true</code>, only subdirectories of <var>path</var> are checked, if <var>path</var> is a directory. If
     *            <var>path</var> is a file, this parameter is ignored. When considering what this parameter is good for, note that the mtime of a
     *            directory is changed when an entry in the directory changes.
     * @param stopWhenFindYoungerRelative If &gt; 0, the recursive search for younger file will be stopped when a file or directory is found that is
     *            younger than <code>System.currentTimeMillis() - stopWhenYoungerRelative</code>. Supposed to be used when one does not care about the
     *            absolute youngest entry, but only, if there are entries that are "young enough".
     * @return The time when any file in (or below) <var>path</var> has last been changed in the file system.
     * @throws UnknownLastChangedException if the <var>path</var> does not exist or is not readable.
     */
    public long lastChangedRelative(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYoungerRelative) throws UnknownLastChangedException;

    /**
     * Checks whether a <var>path</var> of some <var>kind</var> is fully accessible to the program.
     * 
     * @param kindOfPath description of given <var>path</var>. Mainly used for error messages.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public String checkPathFullyAccessible(final File path, final String kindOfPath);

    /**
     * Checks whether a <var>path</var> of some <var>kind</var> is accessible for reading to the program.
     * 
     * @param kindOfPath description of given <var>path</var>. Mainly used for error messages.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public String checkPathReadAccessible(final File path, final String kindOfPath);

    /**
     * Checks whether a <var>directory</var> of some <var>kind</var> is fully accessible to the program (it's a directory, you can read and write in
     * it)
     * 
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public String checkDirectoryFullyAccessible(final File directory, final String kindOfDirectory);

    /**
     * Checks whether a <var>directory</var> of some <var>kind</var> is accessible for reading to the program (it's a directory, you can read and
     * write in it)
     * 
     * @return <code>null</code> if the <var>directory</var> is accessible for reading and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public String checkDirectoryReadAccessible(final File directory, final String kindOfDirectory);

    /**
     * Checks whether a <var>file</var> of some <var>kindOfFile</var> is accessible for reading to the program (so it's a file and you can read it)
     * 
     * @return <code>null</code> if the <var>file</var> is accessible to reading and an error message describing the problem with the <var>file</var>
     *         otherwise.
     */
    public String checkFileReadAccessible(final File file, final String kindOfFile);

    /**
     * Checks whether a <var>file</var> of some <var>kindOfFile</var> is accessible for reading and writing to the program (so it's a file and you can
     * read and write it)
     * 
     * @return <code>null</code> if the <var>file</var> is fully accessible and an error message describing the problem with the <var>file</var>
     *         otherwise.
     */
    public String checkFileFullyAccessible(final File file, final String kindOfFile);

    /**
     * Sets the file's last modification time to the current time without changing the content. If the file does not exist, create it empty.
     * 
     * @throws IOExceptionUnchecked If the file cannot be created or the last modification time cannot be changed.
     */
    public void touch(File file) throws IOExceptionUnchecked;

    /**
     * Removes the given <var>fileToRemove</var>, if necessary recursively.
     * 
     * @param fileToRemove File or directory to remove. If it is a directory, it will be removed recursively.
     * @throws IOExceptionUnchecked If the file or directory cannot be removed.
     */
    public void deleteRecursively(File fileToRemove) throws IOExceptionUnchecked;

    /**
     * Removes the given <var>fileToRemove</var>, if necessary recursively. If it is a file, it will be deleted immediately, if it is a directory, it
     * will be queued up for asynchronous deletion.
     * 
     * @param fileToRemove File or directory to remove. If it is a directory, it will be removed asynchronous recursively.
     * @return <code>true</code> if the file or directory was removed successfully and <code>false</code> otherwise.
     */
    public boolean removeRecursivelyQueueing(File fileToRemove);

    /**
     * Move <var>source</var> to <var>destinationDirectory</var>.
     * 
     * @param source File or directory to move. Must exist when this method is called.
     * @param destinationDirectory The directory to move <var>source</var> to. Has to be an existing directory.
     * @throws IOExceptionUnchecked If <var>source</var> cannot be moved into <var>destinationDirectory</var>.
     */
    public void moveToDirectory(File source, File destinationDirectory) throws IOExceptionUnchecked;

    /**
     * Rename <var>source</var> to <var>destination</var>, or move <var>source</var> to <var>destination</var> if it is an existing directory.
     * Combines {@link #rename(File, File)} and {@link #moveToDirectory(File, File)} in move method, choosing the right method depending on the
     * <var>destination</var>.
     * 
     * @param source File or directory to move. Must exist when this method is called.
     * @param destination If it does not exist, this is the new name for <var>source</var>. If it is an existing directory, <var>source</var> will be
     *            moved to this directory.
     * @throws IOExceptionUnchecked If <var>source</var> cannot be moved to <var>destination</var>.
     */
    public void move(File source, File destination) throws IOExceptionUnchecked;

    /**
     * Copies a file or a whole directory to a new location preserving the file dates.
     * <p>
     * Calls {@link #copyFile(File, File)} if <var>source</var> is a file and {@link #copyDirectory(File, File)} if it is a directory.
     * 
     * @param source an existing file or directory to copy, must not be <code>null</code>
     * @param destination the new file or directory, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOExceptionUnchecked if source or destination is invalid
     * @throws IOExceptionUnchecked if an IO error occurs during copying
     * @see #copyFile(File, File)
     * @see #copyDirectory(File, File)
     */
    public void copy(File source, File destination) throws IOExceptionUnchecked;

    /**
     * Copies a file or a whole directory to a new location preserving the file dates.
     * <p>
     * Calls {@link #copyFileToDirectory(File, File)} if <var>source</var> is a file and {@link #copyDirectoryToDirectory(File, File)} if it is a
     * directory.
     * 
     * @param source an existing file or directory to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOExceptionUnchecked if source or destination is invalid
     * @throws IOExceptionUnchecked if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File)
     * @see #copyDirectoryToDirectory(File, File)
     */
    public void copyToDirectory(File source, File destDir) throws IOExceptionUnchecked;

    /**
     * Copies a file or a whole directory to a new location with a new specified name. File dates will be preserved.
     * <p>
     * 
     * @param source an existing file or directory to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @param newName the new name of the copy which will be created, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOExceptionUnchecked if source or destination is invalid
     * @throws IOExceptionUnchecked if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File)
     * @see #copyDirectoryToDirectory(File, File)
     */
    public void copyToDirectoryAs(File source, File destDir, String newName)
            throws IOExceptionUnchecked;

    /**
     * Copies a whole directory to a new location preserving the file dates.
     * <p>
     * This method copies the specified directory and all its child directories and files to the specified destination. The destination is the new
     * location and name of the directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the new directory, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOExceptionUnchecked if source or destination is invalid
     * @throws IOExceptionUnchecked if an IO error occurs during copying
     */
    public void copyDirectory(File srcDir, File destDir) throws IOExceptionUnchecked;

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this method will overwrite it.
     * 
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destFile the new file, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOExceptionUnchecked if source or destination is invalid
     * @throws IOExceptionUnchecked if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File)
     */
    public void copyFile(File srcFile, File destFile) throws IOExceptionUnchecked;

    /**
     * Copies a file to a directory preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to a file of the same name in the specified destination directory. The destination
     * directory is created if it does not exist. If the destination file exists, then this method will overwrite it.
     * 
     * @param srcFile an existing file to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws NullPointerException if source or destination is null
     * @throws IOExceptionUnchecked if source or destination is invalid
     * @throws IOExceptionUnchecked if an IO error occurs during copying
     */
    public void copyFileToDirectory(File srcFile, File destDir) throws IOExceptionUnchecked;

    /**
     * Copies a directory to within another directory preserving the file dates.
     * <p>
     * This method copies the source directory and all its contents to a directory of the same name in the specified destination directory.
     * <p>
     * The destination directory is created if it does not exist. If the destination directory did exist, then this method merges the source with the
     * destination, with the source taking precedence.
     * 
     * @param srcDir an existing directory to copy, must not be <code>null</code>
     * @param destDir the directory to place the copy in, must not be <code>null</code>
     * @throws NullPointerException if source or destination is <code>null</code>
     * @throws IOExceptionUnchecked if source or destination is invalid
     * @throws IOExceptionUnchecked if an IO error occurs during copying
     */
    public void copyDirectoryToDirectory(File srcDir, File destDir) throws IOExceptionUnchecked;

    //
    // Content
    //

    /**
     * Returns the content of <var>file</var> as a byte array.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during reading the file
     */
    public byte[] getContentAsByteArray(File file) throws IOExceptionUnchecked;

    /**
     * Returns the content of <var>file</var> as a String.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during reading the file
     */
    public String getContentAsString(File file) throws IOExceptionUnchecked;

    /**
     * Returns the content of <var>file</var> as a String. Doesn't append new line at the end.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during reading the file
     */
    public String getExactContentAsString(File file) throws IOExceptionUnchecked;

    /**
     * Returns the content of <var>file</var> as a list of Strings.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during reading the file
     */
    public List<String> getContentAsStringList(File file) throws IOExceptionUnchecked;

    /**
     * Returns a monitored {@link InputStream} of <var>file</var>.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during opening the stream
     */
    public InputStream getInputStream(File file) throws IOExceptionUnchecked;

    /**
     * Returns a monitored {@link IInputStream} of <var>file</var>.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during opening the stream
     */
    public IInputStream getIInputStream(File file) throws IOExceptionUnchecked;

    /**
     * Returns a monitored {@link InputStream} of <var>file</var>.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during opening the stream
     */
    public OutputStream getOutputStream(File file) throws IOExceptionUnchecked;

    /**
     * Returns a monitored {@link IInputStream} of <var>file</var>.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during opening the stream
     */
    public IOutputStream getIOutputStream(File file) throws IOExceptionUnchecked;

    /**
     * Writes <var>content</var> to <var>file</var>. If <var>file</var> already exists, it will be overwritten.
     * 
     * @throws IOExceptionUnchecked if an IO error occurs during writing
     */
    public void writeToFile(File file, String content) throws IOExceptionUnchecked;

    //
    // File system
    //

    /**
     * @see FileSystemUtils#freeSpaceKb(String)
     */
    public long freeSpaceKb(String path) throws IOExceptionUnchecked;

}
