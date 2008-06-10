/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.parser.filter.AlwaysAcceptLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * Some useful utility methods for files and directories.
 * <p>
 * Note that these utilities are considered to be <i>internal</i> methods, that means they are not
 * prepared to do the error checking. If you hand in inappropriate values, e.g. <code>null</code>,
 * all you will get are {@link AssertionError}s or {@link NullPointerException}.
 * <p>
 * If you are tempted to add new functionality to this class, ensure that the new functionality does
 * not yet exist in <code>org.apache.common.io.FileUtils</code>, see <a
 * href="http://jakarta.apache.org/commons/io/api-release/org/apache/commons/io/FileUtils.html">javadoc</a>.
 * 
 * @author Bernd Rinn
 */
public final class FileUtilities
{
    private FileUtilities()
    {
        // Can not be instantiated.
    }

    /**
     * A file filter that accepts all entries.
     */
    public static final FileFilter ACCEPT_ALL_FILTER = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return true;
            }
        };

    /**
     * Copies the content of the specified source file to the specified destination file.
     * 
     * @param sourceFile File to be copied.
     * @param destinationFile File to whom content of <code>sourceFile</code> is copied.
     * @param preservesLastModifiedDate If <code>true</code> the last modified date of
     *            <code>sourceFile</code> will be transfered to <code>destinationFile</code>.
     * @throws EnvironmentFailureException if a {@link IOException} occured.
     */
    public static void copyFileTo(final File sourceFile, final File destinationFile,
            final boolean preservesLastModifiedDate) throws CheckedExceptionTunnel
    {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try
        {
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(destinationFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (final IOException ex)
        {
            throw new EnvironmentFailureException("Couldn't copy file '" + sourceFile + "' to '"
                    + destinationFile + "'.", ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        // In Windows last modified date can only be changed of the output stream is closed
        if (preservesLastModifiedDate)
        {
            final boolean successful = destinationFile.setLastModified(sourceFile.lastModified());
            if (successful == false)
            {
                throw new EnvironmentFailureException("Couldn't copy last modified date of file '"
                        + sourceFile + "' to '" + destinationFile + "' for some unknown reason.");
            }
        }
    }

    /**
     * Loads a text file to a {@link String}.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code>
     *            is not <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention). Never
     *         returns <code>null</code>.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does
     *             not exist.
     */
    public static String loadToString(final File file) throws CheckedExceptionTunnel
    {
        assert file != null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readString(new BufferedReader(fileReader));
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    /**
     * Writes the specified string to the specified file.
     * 
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}.
     */
    public static void writeToFile(final File file, final String str) throws CheckedExceptionTunnel
    {
        assert file != null : "Unspecified file.";
        assert str != null : "Unspecified string.";

        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(file);
            fileWriter.write(str);
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code>
     *            is not <code>null</code>.
     * @return The content of the file line by line.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does
     *             not exist.
     */
    public final static List<String> loadToStringList(final File file)
            throws CheckedExceptionTunnel
    {
        return loadToStringList(file, null);
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code>
     *            is not <code>null</code>.
     * @param lineFilterOrNull a line filter if you are not interested in all lines. May be
     *            <code>null</code>.
     * @return The content of the file line by line.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does
     *             not exist.
     */
    public final static List<String> loadToStringList(final File file,
            final ILineFilter lineFilterOrNull) throws CheckedExceptionTunnel
    {
        assert file != null : "Unspecified file.";

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readStringList(new BufferedReader(fileReader), lineFilterOrNull);
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    /**
     * Loads a resource to a string.
     * <p>
     * A non-existent resource will result in a return value of <code>null</code>.
     * </p>
     * 
     * @param clazz Class for which <code>getResource()</code> will be invoked (must not be
     *            <code>null</code>).
     * @param resource Absolute path of the resource (will be the argument of
     *            <code>getResource()</code>).
     * @return The content of the resource, or <code>null</code> if the specified resource does
     *         not exist.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}
     */
    public static String loadToString(final Class<?> clazz, final String resource)
            throws CheckedExceptionTunnel
    {
        assert clazz != null : "Given class can not be null.";
        assert resource != null && resource.length() > 0 : "Given resource can not be null.";

        BufferedReader reader = null;
        try
        {
            reader = tryGetBufferedReader(clazz, resource);
            return reader == null ? null : readString(reader);
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * <p>
     * A non-existent resource will result in a return value of <code>null</code>.
     * </p>
     * 
     * @param clazz Class for which <code>getResource()</code> will be invoked.
     * @param resource absolute path of the resource (will be the argument of
     *            <code>getResource()</code>).
     * @return The content of the resource line by line.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does
     *             not exist.
     */
    public final static List<String> loadToStringList(final Class<?> clazz, final String resource)
            throws CheckedExceptionTunnel
    {
        return loadToStringList(clazz, resource, null);
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * <p>
     * A non-existent resource will result in a return value of <code>null</code>.
     * </p>
     * 
     * @param clazz Class for which <code>getResource()</code> will be invoked.
     * @param resource absolute path of the resource (will be the argument of
     *            <code>getResource()</code>).
     * @param lineFilterOrNull a line filter if you are not interested in all lines. May be
     *            <code>null</code>.
     * @return The content of the resource line by line or <code>null</code> if given
     *         <var>resource</var> can not be found.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does
     *             not exist.
     */
    public final static List<String> loadToStringList(final Class<?> clazz, final String resource,
            final ILineFilter lineFilterOrNull) throws CheckedExceptionTunnel
    {
        assert clazz != null : "Given class can not be null.";
        assert StringUtils.isNotEmpty(resource) : "Given resource can not be empty.";

        BufferedReader reader = null;
        try
        {
            reader = tryGetBufferedReader(clazz, resource);
            return reader == null ? null : readStringList(reader, lineFilterOrNull);
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private final static BufferedReader tryGetBufferedReader(final Class<?> clazz,
            final String resource) throws FileNotFoundException
    {
        final URL url = clazz.getResource(resource);
        if (url == null)
        {
            return null;
        }
        try
        {
            return new BufferedReader(new FileReader(new File(url.toURI())));
        } catch (final URISyntaxException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    private static String readString(final BufferedReader reader) throws IOException
    {
        assert reader != null : "Unspecified BufferedReader.";
        final StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            builder.append(line).append(CharUtils.LF);
        }
        return builder.toString();
    }

    private final static List<String> readStringList(final BufferedReader reader,
            final ILineFilter lineFilterOrNull) throws IOException
    {
        assert reader != null : "Unspecified BufferedReader.";
        final ILineFilter lineFilter;
        if (lineFilterOrNull == null)
        {
            lineFilter = AlwaysAcceptLineFilter.INSTANCE;
        } else
        {
            lineFilter = lineFilterOrNull;
        }
        final List<String> list = new ArrayList<String>();
        String line = reader.readLine();
        for (int lineNumber = 0; line != null; ++lineNumber, line = reader.readLine())
        {
            if (lineFilter.acceptLine(line, lineNumber))
            {
                list.add(line);
            }
        }
        return list;
    }

    /**
     * Checks whether a <var>path</var> of some <var>kind</var> is fully accessible to the
     * program.
     * 
     * @param kindOfPath description of given <var>path</var>. Mainly used for error messages.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error
     *         message describing the problem with the <var>directory</var> otherwise.
     */
    public static String checkPathFullyAccessible(final File path, final String kindOfPath)
    {
        assert path != null;
        assert kindOfPath != null;

        return checkPathFullyAccessible(path, kindOfPath, "path");
    }

    /**
     * Checks whether a <var>directory</var> of some <var>kind</var> is fully accessible to the
     * program (it's a directory, you can read and write in it)
     * 
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error
     *         message describing the problem with the <var>directory</var> otherwise.
     */
    public static String checkDirectoryFullyAccessible(final File directory,
            final String kindOfDirectory)
    {
        assert directory != null;
        assert kindOfDirectory != null;

        final String msg = checkPathFullyAccessible(directory, kindOfDirectory, "directory");
        if (msg == null && directory.isDirectory() == false)
        {
            return String.format("Path '%s' is supposed to be a %s directory, but is a file.",
                    directory.getPath(), kindOfDirectory);
        }
        return msg;
    }

    private static String checkPathFullyAccessible(final File path, final String kindOfPath,
            final String directoryOrFile)
    {
        assert path != null;
        assert kindOfPath != null;
        assert directoryOrFile != null;

        if (path.canRead() == false)
        {
            if (path.exists() == false)
            {
                return String.format("%s %s '%s' does not exist.", StringUtilities
                        .capitalize(kindOfPath), directoryOrFile, path.getPath());
            } else
            {
                return String.format("%s %s '%s' is not readable.", StringUtilities
                        .capitalize(kindOfPath), directoryOrFile, path.getPath());
            }
        }
        if (path.canWrite() == false)
        {
            return String.format("%s directory '%s' is not writable.", StringUtilities
                    .capitalize(kindOfPath), path.getPath());
        }
        return null;
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and
     * then the directory itself.
     * <p>
     * Convenience method for {@link #deleteRecursively(File)} with <var>logger</var> set to
     * <code>null</code>.
     * 
     * @param path Path of the file or directory to delete.
     * @return <code>true</code> if the path has been delete successfully, <code>false</code>
     *         otherwise.
     */
    public static boolean deleteRecursively(final File path)
    {
        assert path != null;

        return deleteRecursively(path, null);
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and
     * then the directory itself.
     * 
     * @param path Path of the file or directory to delete.
     * @param loggerOrNull The logger that should be used to log deletion of path entries, or
     *            <code>null</code> if nothing should be logged.
     * @return <code>true</code> if the path has been delete successfully, <code>false</code>
     *         otherwise.
     */
    public static boolean deleteRecursively(final File path, final ISimpleLogger loggerOrNull)
    {
        assert path != null;

        if (path.isDirectory())
        {
            for (final File file : path.listFiles())
            {
                if (file.isDirectory())
                {
                    deleteRecursively(file, loggerOrNull);
                } else
                {
                    if (loggerOrNull != null)
                    {
                        loggerOrNull.log(LogLevel.INFO, String.format("Deleting file '%s'", file
                                .getPath()));
                    }
                    file.delete();
                }
            }
        }
        if (loggerOrNull != null)
        {
            loggerOrNull.log(LogLevel.INFO, String
                    .format("Deleting directory '%s'", path.getPath()));
        }
        return path.delete();
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories
     * within the directory that are accepted by the {@link FileFilter}. Any subdirectory that is
     * accepted by the <var>filter</var> will be completely deleted. This holds true also for the
     * <var>path</var> itself.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @param logger The logger that should be used to log deletion of path entries, or
     *            <code>null</code> if nothing should be logged.
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     */
    public static boolean deleteRecursively(final File path, final FileFilter filter,
            final ISimpleLogger logger)
    {
        assert path != null;
        assert filter != null;

        if (filter.accept(path))
        {
            return deleteRecursively(path, logger);
        } else
        {
            if (path.isDirectory())
            {
                for (final File file : path.listFiles())
                {
                    deleteRecursively(file, filter, logger);
                }
            }
            return false;
        }
    }

    private static final class LastChangedWorker
    {
        private boolean subDirectoriesOnly;

        private final long reference;

        private final boolean referenceIsRelative;

        private long lastChanged;

        private boolean terminated;

        LastChangedWorker(final File root, final boolean subDirectoriesOnly, final long reference,
                final boolean referenceIsRelative)
        {
            assert root != null;

            this.subDirectoriesOnly = subDirectoriesOnly;
            this.reference = reference;
            this.referenceIsRelative = referenceIsRelative;
            this.terminated = false;
            this.lastChanged = 0;
            updateLastChanged(root);
            if (terminated == false)
            {
                traverse(root);
            }
        }

        private void updateLastChanged(final File path)
        {
            assert path != null;

            final long lastModified = path.lastModified();
            if (lastModified == 0)
            {
                throw new CheckedExceptionTunnel(new IOException(String.format(
                        "Cannot get the last modification date of '%s'.", path.getPath())));
            }

            lastChanged = Math.max(lastModified, lastChanged);
            if (isYoungEnough(lastChanged))
            {
                terminated = true;
            }
        }

        private boolean isYoungEnough(final long currentLastChanged)
        {
            if (referenceIsRelative)
            {
                return reference > 0 && currentLastChanged > System.currentTimeMillis() - reference;
            } else
            {
                return reference > 0 && currentLastChanged > reference;
            }
        }

        private void traverse(final File path)
        {
            assert path != null;

            if (path.isDirectory() == false)
            {
                return;
            }
            for (final File entry : getEntries(path))
            {
                updateLastChanged(entry);
                if (terminated)
                {
                    return;
                }
                StopException.check();
                traverse(entry);
            }
        }

        private File[] getEntries(final File directory)
        {
            assert directory != null;

            if (subDirectoriesOnly)
            {
                return directory.listFiles(new FileFilter()
                    {
                        public boolean accept(final File pathname)
                        {
                            return pathname.isDirectory();
                        }
                    });
            } else
            {
                return directory.listFiles();
            }
        }

        /**
         * Returns the time of last change of the youngest file found below <var>root</var>.
         */
        long getLastChanged()
        {
            return lastChanged;
        }
    }

    /**
     * Determines the time (in milliseconds since start of the epoch) when any item below <var>path</var>
     * has last been changed in the file system.
     * 
     * @param path The path (file or directory) to check for last change.
     * @param subDirectoriesOnly If <code>true</code>, only subdirectories of <var>path</var>
     *            are checked, if <var>path</var> is a directory. If <var>path</var> is a file,
     *            this parameter is ignored. When considering what this parameter is good for, note
     *            that the mtime of a directory is changed when an entry in the directory changes.
     * @param stopWhenFindYounger If &gt; 0, the recursive search for younger file will be stopped
     *            when a file or directory is found that is younger than the time specified in this
     *            parameter. Supposed to be used when one does not care about the absolute youngest
     *            entry, but only, if there are entries that are "young enough".
     * @return The time when any file in (or below) <var>path</var> has last been changed in the
     *         file system.
     * @throws CheckedExceptionTunnel of an {@link IOException} if the <var>path</var> does not
     *             exist or is not readable.
     * @throws StopException if the thread that the method runs in gets interrupted.
     */
    public static long lastChanged(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYounger)
    {
        return (new LastChangedWorker(path, subDirectoriesOnly, stopWhenFindYounger, false))
                .getLastChanged();
    }

    /**
     * Determines the time (in milliseconds since start of the epoch) when any item below <var>path</var>
     * has last been changed in the file system.
     * 
     * @param path The path (file or directory) to check for last change.
     * @param subDirectoriesOnly If <code>true</code>, only subdirectories of <var>path</var>
     *            are checked, if <var>path</var> is a directory. If <var>path</var> is a file,
     *            this parameter is ignored. When considering what this parameter is good for, note
     *            that the mtime of a directory is changed when an entry in the directory changes.
     * @param stopWhenFindYoungerRelative If &gt; 0, the recursive search for younger file will be
     *            stopped when a file or directory is found that is younger than
     *            <code>System.currentTimeMillis() - stopWhenYoungerRelative</code>. Supposed to
     *            be used when one does not care about the absolute youngest entry, but only, if
     *            there are entries that are "young enough".
     * @return The time when any file in (or below) <var>path</var> has last been changed in the
     *         file system.
     * @throws CheckedExceptionTunnel of an {@link IOException} if the <var>path</var> does not
     *             exist or is not readable.
     * @throws StopException if the thread that the method runs in gets interrupted.
     */
    public static long lastChangedRelative(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYoungerRelative)
    {
        return (new LastChangedWorker(path, subDirectoriesOnly, stopWhenFindYoungerRelative, true))
                .getLastChanged();
    }

    /**
     * @return The time when any file in (or below) <var>path</var> has last been changed in the
     *         file system.
     * @throws CheckedExceptionTunnel of an {@link IOException} if the <var>path</var> does not
     *             exist or is not readable.
     * @throws StopException if the thread that the method runs in gets interrupted.
     */
    public static long lastChanged(final File path)
    {
        return lastChanged(path, false, 0L);
    }

    /**
     * Removes given <var>prefix</var> from given <var>file</var> and returns a new
     * <code>File</code>.
     * <p>
     * Returns given <var>file</var> if prefix is <i>empty</i> or could not be found in the file
     * name.
     * </p>
     * 
     * @param file can not be <code>null</code>.
     * @param prefix prefix that should be removed from the file name. Can be <code>null</code>.
     */
    public final static File removePrefixFromFileName(final File file, final String prefix)
    {
        assert file != null;
        final String name = file.getName();
        if (StringUtils.isEmpty(prefix))
        {
            return file;
        }
        if (name.indexOf(prefix) < 0)
        {
            return file;
        }
        return new File(file.getParent(), name.substring(prefix.length()));
    }

    /** A <i>Java</i> pattern matching one or more digits. */
    private final static Pattern ONE_OR_MORE_DIGITS = Pattern.compile(".*(\\d+)$");

    public final static File createNextNumberedFile(final File path, final Pattern regex)
    {
        return createNextNumberedFile(path, regex, null);
    }

    /**
     * Creates the next numbered file if given <var>path</var> does already exist.
     * <p>
     * If the new suggested file already exists, then this method is called recursively.
     * </p>
     * 
     * @param defaultFileNameOrNull the default name for the new file if the digit pattern could not
     *            be found in its name. If empty then "1" will be appended to its name.
     * @param regexOrNull pattern to find out the counter. If <code>null</code> then a default (<code>(\\d+)</code>)
     *            will be used. The given <var>regex</var> must contain <code>(\\d+)</code> or
     *            <code>([0-9]+)</code>.
     */
    public final static File createNextNumberedFile(final File path, final Pattern regexOrNull,
            final String defaultFileNameOrNull)
    {
        assert path != null;
        if (path.exists() == false)
        {
            return path;
        }
        final Pattern pattern;
        if (regexOrNull == null)
        {
            pattern = ONE_OR_MORE_DIGITS;
        } else
        {
            assert regexOrNull.pattern().indexOf("(\\d+)") > -1
                    || regexOrNull.pattern().indexOf("([0-9]+)") > -1;
            pattern = regexOrNull;
        }

        final String pathName = path.getName();
        final Matcher matcher = pattern.matcher(pathName);
        boolean found = matcher.find();
        if (found == false)
        {
            final String fileName;
            if (StringUtils.isEmpty(defaultFileNameOrNull) == false)
            {
                fileName = defaultFileNameOrNull;
            } else
            {
                fileName = pathName + "1";
            }
            return createNextNumberedFile(new File(path.getParent(), fileName), pattern,
                    defaultFileNameOrNull);
        }
        final StringBuilder builder = new StringBuilder();
        int nextStart = 0;
        while (found)
        {
            final String group = matcher.group(1);
            final int newNumber = Integer.parseInt(group) + 1;
            builder.append(pathName.substring(nextStart, matcher.start(1))).append(newNumber);
            nextStart = matcher.end(1);
            found = matcher.find();
        }
        builder.append(pathName.substring(nextStart));
        final File newFile = new File(path.getParent(), builder.toString());
        if (newFile.exists())
        {
            return createNextNumberedFile(newFile, pattern, defaultFileNameOrNull);
        }
        return newFile;
    }

    /**
     * For given <var>root</var> and <var>file</var> extracts the relative path.
     * <p>
     * If given <var>file</var> does not contain given <var>root</var> path in its absolute path,
     * then returns <code>null</code> (as the relative file could not be determined).
     * </p>
     * 
     * @return a relative file with no starting separator.
     */
    public final static String getRelativeFile(final File root, final File file)
    {
        assert root != null : "Given root can not be null.";
        assert file != null : "Given file can not be null.";
        final String rootPath = root.getAbsolutePath() + File.separator;
        final String filePath = file.getAbsolutePath();
        if (filePath.startsWith(rootPath))
        {
            return filePath.substring(rootPath.length());
        } else
        {
            return null;
        }
    }

    /**
     * Lists all resources in a given directory.
     * 
     * @param directory the directory to list
     * @param loggerOrNull logger, if <code>null</code> than no logging occurs
     * @return all files in <var>directory</var> or <code>null</code>, if <var>directory</var>
     *         does not exist or is not a directory.
     */
    public static File[] tryListFiles(final File directory, final ISimpleLogger loggerOrNull)
    {
        return tryListFiles(directory, ACCEPT_ALL_FILTER, loggerOrNull);
    }

    /**
     * Lists all resources in a given directory which match the filter.
     * 
     * @param directory the directory to list
     * @param filter only files matching this filter will show up in the result
     * @param loggerOrNull logger, if <code>null</code> than no logging occurs
     * @return all files in <var>directory</var> that match the filter, or <code>null</code>, if
     *         <var>directory</var> does not exist or is not a directory.
     */
    public static File[] tryListFiles(final File directory, final FileFilter filter,
            final ISimpleLogger loggerOrNull)
    {
        File[] paths = null;
        RuntimeException ex = null;
        try
        {
            paths = directory.listFiles(filter);
        } catch (final RuntimeException e)
        {
            ex = e;
        }
        if (paths == null && loggerOrNull != null)
        {
            logFailureInDirectoryListing(ex, directory, loggerOrNull);
        }
        return paths;
    }

    @SuppressWarnings("unchecked")
    public final static void sortByLastModified(final File[] files)
    {
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
    }

    private static void logFailureInDirectoryListing(final RuntimeException exOrNull,
            final File directory, final ISimpleLogger logger)
    {
        if (exOrNull == null)
        {
            if (directory.isFile())
            {
                logger.log(LogLevel.ERROR, String.format("Failed to get listing of directory '%s' "
                        + "(path is file instead of directory).", directory));
            } else
            {
                logger.log(LogLevel.ERROR, String.format(
                        "Failed to get listing of directory '%s' (path not found).", directory));
            }
        } else
        {
            final StringWriter exStackWriter = new StringWriter();
            exOrNull.printStackTrace(new PrintWriter(exStackWriter));
            logger.log(LogLevel.ERROR, String.format(
                    "Failed to get listing of directory '%s'. Exception: %s", directory,
                    exStackWriter.toString()));
        }
    }

    /**
     * Copies the resource with the given name to a temporary file.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @return The name of the temporary file.
     * @throws IllegalArgumentException If the resource cannot be found in the class path.
     * @throws CheckedExceptionTunnel If an {@link IOException} occurs.
     */
    public final static String copyResourceToTempFile(final String resource, final String prefix,
            final String postfix)
    {
        final InputStream resourceStream = FileUtilities.class.getResourceAsStream(resource);
        if (resourceStream == null)
        {
            throw new IllegalArgumentException("Resource '" + resource + "' not found.");
        }
        try
        {
            final File tempFile = File.createTempFile(prefix, postfix);
            tempFile.deleteOnExit();
            final OutputStream fileStream = new FileOutputStream(tempFile);
            try
            {
                IOUtils.copy(resourceStream, fileStream);
            } finally
            {
                IOUtils.closeQuietly(fileStream);
            }
            return tempFile.getAbsolutePath();
        } catch (final IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(resourceStream);
        }
    }

    /**
     * Tries to copy the resource with the given name to a temporary file.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be
     *         copied.
     */
    public final static String tryCopyResourceToTempFile(final String resource,
            final String prefix, final String postfix)
    {
        try
        {
            return copyResourceToTempFile(resource, prefix, postfix);
        } catch (final Exception ex)
        {
            return null;
        }
    }

    /**
     * Lists files of given <var>directory</var>.
     * <p>
     * Throws an <code>EnvironmentFailureException</code> if {@link File#listFiles()} returns
     * <code>null</code>.
     * </p>
     * 
     * @param directory must be a directory.
     */
    public final static File[] listFiles(final File directory) throws EnvironmentFailureException
    {
        final File[] fileList = directory.listFiles();
        if (fileList == null)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Failed to get listing of directory '%s'", directory.getAbsolutePath());
        }
        return fileList;
    }

    /**
     * Normalizes given <var>file</var> path, removing double and single dot path steps.
     * <p>
     * It first tries to call {@link File#getCanonicalFile()}. If this fails, works with the file
     * name returned by {@link File#getAbsolutePath()}.
     * </p>
     */
    public final static File normalizeFile(final File file)
    {
        assert file != null : "Given file can not be null.";
        try
        {
            return file.getCanonicalFile();
        } catch (final IOException ex)
        {
            return new File(FilenameUtils.normalize(file.getAbsolutePath()));
        }
    }

    /**
     * Tries to get the canonical path of given <var>file</var>.
     * <p>
     * If it fails (by throwing an <code>IOException</code>), then returns the absolute path.
     * </p>
     */
    public static final String getCanonicalPath(final File file)
    {
        assert file != null : "Given file can not be null.";
        try
        {
            return file.getCanonicalPath();
        } catch (final IOException ex)
        {
            return file.getAbsolutePath();
        }
    }

    private static final NumberFormat SIZE_FORMAT = new DecimalFormat("0.00");

    /**
     * Returns a human-readable version of the file size, where the input represents a specific
     * number of bytes.
     * <p>
     * By comparison with {@link FileUtils#byteCountToDisplaySize(long)}, the output of this
     * version is more exact.
     * </p>
     * 
     * @param size the number of bytes
     * @return a human-readable display value (includes units)
     * @see FileUtils#byteCountToDisplaySize(long)
     */
    public final static String byteCountToDisplaySize(final long size)
    {
        assert size > -1 : "Negative size value";
        final String displaySize;
        if (size / FileUtils.ONE_GB > 0)
        {
            displaySize = SIZE_FORMAT.format(size / (float) FileUtils.ONE_GB) + " GB";
        } else if (size / FileUtils.ONE_MB > 0)
        {
            displaySize = SIZE_FORMAT.format(size / (float) FileUtils.ONE_MB) + " MB";
        } else if (size / FileUtils.ONE_KB > 0)
        {
            displaySize = SIZE_FORMAT.format(size / (float) FileUtils.ONE_KB) + " KB";
        } else
        {
            displaySize = SIZE_FORMAT.format(size) + " bytes";
        }
        return displaySize;
    }

}
