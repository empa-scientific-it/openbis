/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IDescribingActivitySensor;
import ch.systemsx.cisd.common.concurrent.RecordingActivityObserverSensor;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.FileExistsException;
import ch.systemsx.cisd.common.exceptions.UnknownLastChangedException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.parser.Line;
import ch.systemsx.cisd.common.parser.filter.AlwaysAcceptLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.string.StringUtilities;
import ch.systemsx.cisd.common.string.StringUtilities.IUniquenessChecker;

/**
 * Some useful utility methods for files and directories.
 * <p>
 * Note that these utilities are considered to be <i>internal</i> methods, that means they are not prepared to do the error checking. If you hand in
 * inappropriate values, e.g. <code>null</code>, all you will get are {@link AssertionError}s or {@link NullPointerException}.
 * <p>
 * If you are tempted to add new functionality to this class, ensure that the new functionality does not yet exist in
 * <code>org.apache.common.io.FileUtils</code>, see <a
 * href="https://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/FileUtils.html" >javadoc</a>.
 * 
 * @author Bernd Rinn
 */
public final class FileUtilities
{
    public static final List<String> HDF5_FILE_TYPES = Arrays.asList("h5", "h5ar");

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileUtilities.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            FileUtilities.class);

    private FileUtilities()
    {
        // Can not be instantiated.
    }

    /**
     * A file filter that accepts all entries.
     */
    public static final FileFilter ACCEPT_ALL_FILTER = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return true;
            }
        };

    /**
     * A file filter that accepts all entries but the hidden ones.
     */
    public static final FileFilter ACCEPT_ALL_BUT_HIDDEN_FILTER = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isHidden() == false;
            }
        };

    /**
     * Loads a text file to a {@link String}.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention). Never returns <code>null</code>.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static String loadToString(final File file) throws IOExceptionUnchecked
    {
        assert file != null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readString(new BufferedReader(fileReader));
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    /**
     * Loads a file to a byte[].
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention). Never returns <code>null</code>.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static byte[] loadToByteArray(final File file) throws IOExceptionUnchecked
    {
        assert file != null;

        final int size = (int) file.length();
        if (size != file.length())
        {
            throw new IOExceptionUnchecked("File " + file.getPath() + " too large.");
        }
        final byte[] buffer = new byte[size];
        FileInputStream fstream = null;
        try
        {
            fstream = new FileInputStream(file);
            final int sizeRead = fillBuffer(fstream, buffer);
            if (sizeRead < size)
            {
                final byte[] smallerBuffer = new byte[sizeRead];
                System.arraycopy(buffer, 0, smallerBuffer, 0, sizeRead);
                return smallerBuffer;
            }
            return buffer;
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fstream);
        }
    }

    /**
     * Loads a file to an {@link Object}.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention). Never returns <code>null</code>.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     * @throws CheckedExceptionTunnel for wrapping an {@link ClassNotFoundException}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadToObject(final File file, Class<T> clazz) throws IOExceptionUnchecked,
            CheckedExceptionTunnel
    {
        assert file != null;

        final int size = (int) file.length();
        if (size != file.length())
        {
            throw new IOExceptionUnchecked("File " + file.getPath() + " too large.");
        }
        FileInputStream fstream = null;
        ObjectInputStream oistream = null;
        try
        {
            fstream = new FileInputStream(file);
            oistream = new ObjectInputStream(fstream);
            return (T) oistream.readObject();
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (final ClassNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(oistream);
            IOUtils.closeQuietly(fstream);
        }
    }

    private static int fillBuffer(InputStream input, byte[] buffer) throws IOException
    {
        int ofs = 0;
        int len = buffer.length;
        int count = 0;
        int n = 0;
        while (len > 0 && -1 != (n = input.read(buffer, ofs, len)))
        {
            ofs += n;
            len -= n;
            count += n;
        }
        return count;
    }

    /**
     * Loads a text file to a {@link String}. Doesn't append new line at the end.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention). Never returns <code>null</code>.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static String loadExactToString(final File file) throws IOExceptionUnchecked
    {
        assert file != null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readExactString(new BufferedReader(fileReader));
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    /**
     * Writes the specified string to the specified file.
     * 
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}.
     */
    public static void writeToFile(final File file, final String str) throws IOExceptionUnchecked
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
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    /**
     * Writes the specified string to the specified file.
     * 
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}.
     */
    public static void writeToFile(final File file, final byte[] data) throws IOExceptionUnchecked
    {
        assert file != null : "Unspecified file.";
        assert data != null : "Unspecified data.";

        FileOutputStream fileStream = null;
        try
        {
            fileStream = new FileOutputStream(file);
            fileStream.write(data);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileStream);
        }
    }

    /**
     * Writes a content of the specified input stream to the file at the specified position.
     */
    public static long writeToFile(final File file, final long dataPosition,
            final InputStream dataInputStream) throws IOExceptionUnchecked
    {
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream dataBufferedInputStream = null;
        byte[] dataBuffer = new byte[1024];

        try
        {
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(dataPosition);

            dataBufferedInputStream = new BufferedInputStream(dataInputStream);

            long dataSize = 0;
            int dataPartSize = 0;

            while ((dataPartSize = dataBufferedInputStream.read(dataBuffer)) != -1)
            {
                randomAccessFile.write(dataBuffer, 0, dataPartSize);
                dataSize += dataPartSize;
            }

            return dataSize;

        } catch (FileNotFoundException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        } finally
        {
            if (randomAccessFile != null)
            {
                try
                {
                    randomAccessFile.close();
                } catch (IOException ex)
                {
                }
            }
            IOUtils.closeQuietly(dataBufferedInputStream);
        }
    }

    /**
     * Writes the specified string to the specified file.
     * 
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}.
     */
    public static void writeToFile(final File file, final Serializable object)
            throws IOExceptionUnchecked
    {
        assert file != null : "Unspecified file.";
        assert object != null : "Unspecified data.";

        FileOutputStream fileStream = null;
        ObjectOutputStream oostream = null;
        try
        {
            fileStream = new FileOutputStream(file);
            oostream = new ObjectOutputStream(fileStream);
            oostream.writeObject(object);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(oostream);
            IOUtils.closeQuietly(fileStream);
        }
    }

    /**
     * Append the specified string to the specified file.
     * 
     * @param file The file to append to.
     * @param str The string to append to the file.
     * @param shouldAppendNewline If true, a newline will be appended after the string.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}.
     */
    public static void appendToFile(final File file, final String str,
            final boolean shouldAppendNewline) throws IOExceptionUnchecked
    {
        assert file != null : "Unspecified file.";
        assert str != null : "Unspecified string.";

        BufferedWriter bw = null;
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            bw.append(str);
            if (shouldAppendNewline)
            {
                bw.newLine();
            }
            bw.flush();
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fw);
        }
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file line by line.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public final static List<String> loadToStringList(final File file) throws IOExceptionUnchecked
    {
        return loadToStringList(file, null);
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @param lineFilterOrNull a line filter if you are not interested in all lines. May be <code>null</code>.
     * @return The content of the file line by line.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public final static List<String> loadToStringList(final File file,
            final ILineFilter lineFilterOrNull) throws IOExceptionUnchecked
    {
        assert file != null : "Unspecified file.";

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readStringList(new BufferedReader(fileReader), lineFilterOrNull);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
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
     * @param clazz Class for which <code>getResource()</code> will be invoked (must not be <code>null</code>).
     * @param resource Absolute path of the resource (will be the argument of <code>getResource()</code>).
     * @return The content of the resource, or <code>null</code> if the specified resource does not exist.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}
     */
    public static String loadToString(final Class<?> clazz, final String resource)
            throws IOExceptionUnchecked
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
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
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
     * @param resource absolute path of the resource (will be the argument of <code>getResource()</code>).
     * @return The content of the resource line by line.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public final static List<String> loadToStringList(final Class<?> clazz, final String resource)
            throws IOExceptionUnchecked
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
     * @param resource absolute path of the resource (will be the argument of <code>getResource()</code>).
     * @param lineFilterOrNull a line filter if you are not interested in all lines. May be <code>null</code>.
     * @return The content of the resource line by line or <code>null</code> if given <var>resource</var> can not be found.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public final static List<String> loadToStringList(final Class<?> clazz, final String resource,
            final ILineFilter lineFilterOrNull) throws IOExceptionUnchecked
    {
        assert clazz != null : "Given class can not be null.";
        assert StringUtils.isNotEmpty(resource) : "Given resource can not be empty.";

        BufferedReader reader = null;
        try
        {
            reader = tryGetBufferedReader(clazz, resource);
            return reader == null ? null : readStringList(reader, lineFilterOrNull);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private final static BufferedReader tryGetBufferedReader(final Class<?> clazz,
            final String resource)
    {
        final InputStream stream = clazz.getResourceAsStream(resource);
        if (stream == null)
        {
            return null;
        }
        return new BufferedReader(new InputStreamReader(stream));
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

    private static String readExactString(final BufferedReader reader) throws IOException
    {
        assert reader != null : "Unspecified BufferedReader.";
        final StringBuilder builder = new StringBuilder();
        int numRead = 0;
        while ((numRead = reader.read()) != -1)
        {
            builder.append(String.copyValueOf(Character.toChars(numRead)));
        }
        reader.close();
        return builder.toString();
    }

    /**
     * Loads a list of strings from the given input stream.
     */
    public final static List<String> loadToStringList(final InputStream is)
            throws IOExceptionUnchecked
    {
        return readStringList(new BufferedReader(new InputStreamReader(is)), null);
    }

    private final static List<String> readStringList(final BufferedReader reader,
            final ILineFilter lineFilterOrNull) throws IOExceptionUnchecked
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
        try
        {
            String line = reader.readLine();
            for (int lineNumber = 0; line != null; ++lineNumber, line = reader.readLine())
            {
                if (lineFilter.acceptLine(new Line(lineNumber, line)))
                {
                    list.add(line);
                }
            }
            return list;
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Checks whether a <var>path</var> of some <var>kind</var> is fully accessible to the program.
     * 
     * @param kindOfPath description of given <var>path</var>. Mainly used for error messages.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public static String checkPathFullyAccessible(final File path, final String kindOfPath)
    {
        assert path != null;
        assert kindOfPath != null;

        return checkPathAccessible(path, kindOfPath, "path", true);
    }

    /**
     * Checks whether a <var>path</var> of some <var>kind</var> is accessible for reading to the program.
     * 
     * @param kindOfPath description of given <var>path</var>. Mainly used for error messages.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public static String checkPathReadAccessible(final File path, final String kindOfPath)
    {
        assert path != null;
        assert kindOfPath != null;

        return checkPathAccessible(path, kindOfPath, "path", false);
    }

    /**
     * Checks whether a <var>directory</var> of some <var>kind</var> is accessible for reading to the program (it's a directory, you can read and
     * write in it)
     * 
     * @return <code>null</code> if the <var>directory</var> is accessible for reading and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public static String checkDirectoryReadAccessible(final File directory,
            final String kindOfDirectory)
    {
        assert directory != null;
        assert kindOfDirectory != null;

        final String msg = checkPathAccessible(directory, kindOfDirectory, "directory", false);
        if (msg == null && directory.isDirectory() == false)
        {
            return String.format("Path '%s' is supposed to be a %s directory but isn't.",
                    directory.getPath(), kindOfDirectory);
        }
        return msg;
    }

    /**
     * Checks whether a <var>directory</var> of some <var>kind</var> is fully accessible to the program (it's a directory, you can read and write in
     * it)
     * 
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the problem with the
     *         <var>directory</var> otherwise.
     */
    public static String checkDirectoryFullyAccessible(final File directory,
            final String kindOfDirectory)
    {
        assert directory != null;
        assert kindOfDirectory != null;

        final String msg = checkPathAccessible(directory, kindOfDirectory, "directory", true);
        if (msg == null && directory.isDirectory() == false)
        {
            return String.format("Path '%s' is supposed to be a %s directory but isn't.",
                    directory.getPath(), kindOfDirectory);
        }
        return msg;
    }

    /**
     * Checks whether a <var>file</var> of some <var>kindOfFile</var> is accessible for reading to the program (so it's a file and you can read it)
     * 
     * @return <code>null</code> if the <var>file</var> is accessible to reading and an error message describing the problem with the <var>file</var>
     *         otherwise.
     */
    public static String checkFileReadAccessible(final File file, final String kindOfFile)
    {
        assert file != null;
        assert kindOfFile != null;

        final String msg = checkPathAccessible(file, kindOfFile, "directory", false);
        if (msg == null && file.isFile() == false)
        {
            return String.format("Path '%s' is supposed to be a %s file but isn't.",
                    file.getPath(), kindOfFile);
        }
        return msg;
    }

    /**
     * Checks whether a <var>file</var> of some <var>kindOfFile</var> is accessible for reading and writing to the program (so it's a file and you can
     * read and write it)
     * 
     * @return <code>null</code> if the <var>file</var> is fully accessible and an error message describing the problem with the <var>file</var>
     *         otherwise.
     */
    public static String checkFileFullyAccessible(final File file, final String kindOfFile)
    {
        assert file != null;
        assert kindOfFile != null;

        final String msg = checkPathAccessible(file, kindOfFile, "file", true);
        if (msg == null && file.isFile() == false)
        {
            return String.format("Path '%s' is supposed to be a %s file but isn't.",
                    file.getPath(), kindOfFile);
        }
        return msg;
    }

    private static String checkPathAccessible(final File path, final String kindOfPath,
            final String directoryOrFile, final boolean readAndWrite)
    {
        assert path != null;
        assert kindOfPath != null;
        assert directoryOrFile != null;

        if (path.canRead() == false)
        {
            if (path.exists() == false)
            {
                return String.format("%s %s '%s' does not exist.",
                        StringUtilities.capitalize(kindOfPath), directoryOrFile, path.getPath());
            } else
            {
                return String.format("%s %s '%s' is not readable.",
                        StringUtilities.capitalize(kindOfPath), directoryOrFile, path.getPath());
            }
        }
        if (readAndWrite && path.canWrite() == false)
        {
            return String.format("%s directory '%s' is not writable.",
                    StringUtilities.capitalize(kindOfPath), path.getPath());
        }
        return null;
    }

    /**
     * A class that monitors activity on recursive delete processes.
     */
    public static class DeleteActivityDetector extends RecordingActivityObserverSensor implements
            IDescribingActivitySensor
    {
        private final File path;

        public DeleteActivityDetector(File path)
        {
            super();
            this.path = path;
        }

        @Override
        public String describeInactivity(long now)
        {
            return "No delete activity of path " + path.getPath() + " for "
                    + DurationFormatUtils.formatDuration(now - getLastActivityMillis(), "H:mm:ss.SSS");
        }

    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory itself.
     * <p>
     * Convenience method for {@link #deleteRecursively(File)} with <var>logger</var> set to <code>null</code>.
     * 
     * @param path Path of the file or directory to delete.
     * @return <code>true</code> if the path has been deleted successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path) throws InterruptedExceptionUnchecked
    {
        assert path != null;

        return deleteRecursively(path, (ISimpleLogger) null, null);
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory itself.
     * 
     * @param path Path of the file or directory to delete.
     * @param loggerOrNull The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @return <code>true</code> if the path has been deleted successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final ISimpleLogger loggerOrNull)
            throws InterruptedExceptionUnchecked
    {
        return deleteRecursively(path, loggerOrNull, null);
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory itself.
     * 
     * @param path Path of the file or directory to delete.
     * @param loggerOrNull The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @param observerOrNull If not <code>null</code>, will be updated on progress in the deletion. This can be used to find out whether a
     *            (potentially) long-running recursive deletion call is alive-and-kicking or hangs (e.g. due to a remote directory becoming
     *            unresponsive).
     * @return <code>true</code> if the path has been deleted successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final ISimpleLogger loggerOrNull,
            final IActivityObserver observerOrNull) throws InterruptedExceptionUnchecked
    {
        assert path != null;

        if (path.isDirectory())
        {
            if (isSymbolicLink(path))
            {
                return deleteSymbolicLink(path, loggerOrNull);
            }
            ensureWritable(path);
            for (final File file : path.listFiles())
            {
                if (Thread.interrupted())
                {
                    throw new InterruptedExceptionUnchecked();
                }
                if (observerOrNull != null)
                {
                    observerOrNull.update();
                }
                if (file.isDirectory())
                {
                    deleteRecursively(file, loggerOrNull, observerOrNull);
                } else
                {
                    if (loggerOrNull != null)
                    {
                        loggerOrNull.log(LogLevel.INFO,
                                String.format("Deleting file '%s'", file.getPath()));
                    }
                    delete(file);
                }
            }
        }
        if (loggerOrNull != null)
        {
            loggerOrNull.log(LogLevel.INFO,
                    String.format("Deleting directory '%s'", path.getPath()));
        }
        return delete(path);
    }

    /** @return true if file is a symbolic link */
    public static boolean isSymbolicLink(File path)
    {
        if (Unix.isOperational())
        {
            return Unix.isSymbolicLink(path.getAbsolutePath());
        } else
        {
            // it is not Linux, Solaris or Mac. So it's probably Windows which does not support
            // symbolic links
            return false;
        }
    }

    private static boolean deleteSymbolicLink(File path, ISimpleLogger loggerOrNull)
    {
        if (loggerOrNull != null)
        {
            loggerOrNull.log(LogLevel.INFO,
                    String.format("Deleting symbolic link to a directory '%s'", path.getPath()));
        }
        return delete(path);
    }

    private static boolean ensureWritable(File path)
    {
        if (path.canWrite() == false && Unix.isOperational())
        {
            try
            {
                Unix.setAccessMode(path.getPath(), (short) 0777);
            } catch (IOExceptionUnchecked ex)
            {
                if (ex.getCause() != null
                        && ex.getCause().getMessage().contains("No such file or directory"))
                {
                    return false;
                }
            }

        }
        return path.canWrite();
    }

    /**
     * Deletes the <var>file</var>, setting it to read-write mode if necessary.
     */
    public static boolean delete(File file)
    {
        final boolean OK = file.delete();
        if (OK)
        {
            return true;
        }
        if (file.exists() && Unix.isOperational())
        {
            Unix.setAccessMode(file.getPath(), (short) 0777);
            return file.delete();
        }
        return false;
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories within the directory that are accepted by the
     * {@link FileFilter}. Any subdirectory that is accepted by the <var>filter</var> will be completely deleted. This holds true also for the
     * <var>path</var> itself.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @param logger The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final FileFilter filter,
            final ISimpleLogger logger) throws InterruptedExceptionUnchecked
    {
        assert path != null;

        return deleteRecursively(path, filter, logger, null);
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories within the directory that are accepted by the
     * {@link FileFilter}. Any subdirectory that is accepted by the <var>filter</var> will be completely deleted. This holds true also for the
     * <var>path</var> itself.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @param logger The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @param observerOrNull If not <code>null</code>, will be updated on progress in the deletion. This can be used to find out whether a
     *            (potentially) long-running recursive deletion call is alive-and-kicking or hangs (e.g. due to a remote directory becoming
     *            unresponsive).
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final FileFilter filter,
            final ISimpleLogger logger, final IActivityObserver observerOrNull)
            throws InterruptedExceptionUnchecked
    {
        assert path != null;
        assert filter != null;

        if (filter.accept(path))
        {
            return deleteRecursively(path, logger, observerOrNull);
        } else
        {
            if (path.isDirectory())
            {
                for (final File file : path.listFiles())
                {
                    if (Thread.interrupted())
                    {
                        throw new InterruptedExceptionUnchecked();
                    }
                    if (observerOrNull != null)
                    {
                        observerOrNull.update();
                    }
                    deleteRecursively(file, filter, logger, observerOrNull);
                }
            }
            return false;
        }
    }

    /**
     * Recursively finds files that the specified <var>filter</var> accepts starting from given <var>path</var>.
     * 
     * @param path Path of the directory to search in.
     * @param files List of the files found so far.
     * @param filter Filter that contains condition for the file that we are looking for.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static void findFiles(File path, List<File> files, FileFilter filter)
            throws InterruptedExceptionUnchecked

    {
        if (filter.accept(path))
        {
            files.add(path);
        }
        if (path.isDirectory())
        {
            for (File child : path.listFiles())
            {
                if (Thread.interrupted())
                {
                    throw new InterruptedExceptionUnchecked();
                }
                findFiles(child, files, filter);
            }
        }
    }

    private static final class LastChangedWorker
    {
        private final IActivityObserver observerOrNull;

        private final boolean subDirectoriesOnly;

        private final long reference;

        private final boolean referenceIsRelative;

        private long lastChanged;

        private boolean terminated;

        LastChangedWorker(final File root, final boolean subDirectoriesOnly, final long reference,
                final boolean referenceIsRelative, final IActivityObserver observerOrNull)
                throws UnknownLastChangedException
        {
            assert root != null;

            this.subDirectoriesOnly = subDirectoriesOnly;
            this.reference = reference;
            this.referenceIsRelative = referenceIsRelative;
            this.observerOrNull = observerOrNull;
            this.terminated = false;
            this.lastChanged = 0;
            updateLastChanged(root);
            if (terminated == false)
            {
                traverse(root);
            }
        }

        private void updateLastChanged(final File path) throws UnknownLastChangedException
        {
            assert path != null;

            final long lastModified = path.lastModified();
            if (lastModified == 0)
            {
                throw new UnknownLastChangedException(String.format(
                        "Can not get the last modification date of '%s'.", path.getPath()));
            }
            if (observerOrNull != null)
            {
                observerOrNull.update();
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

        private void traverse(final File path) throws UnknownLastChangedException
        {
            assert path != null;

            if (path.isDirectory() == false)
            {
                return;
            }
            File[] entries = getEntries(path);
            if (entries == null)
            {
                return;
            }
            for (final File entry : entries)
            {
                updateLastChanged(entry);
                if (terminated)
                {
                    return;
                }
                InterruptedExceptionUnchecked.check();
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
                        @Override
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
     * @throws InterruptedExceptionUnchecked if the thread that the method runs in gets interrupted.
     */
    public static long lastChanged(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYounger) throws UnknownLastChangedException
    {
        return lastChanged(path, subDirectoriesOnly, stopWhenFindYounger, null);
    }

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
     * @param observerOrNull If not <code>null</code>, will be updated on progress in scanning. This can be used to find out whether a (potentially)
     *            long-running recursive deletion call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @return The time when any file in (or below) <var>path</var> has last been changed in the file system.
     * @throws UnknownLastChangedException if the <var>path</var> does not exist or is not readable.
     * @throws InterruptedExceptionUnchecked if the thread that the method runs in gets interrupted.
     */
    public static long lastChanged(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYounger, final IActivityObserver observerOrNull)
            throws UnknownLastChangedException
    {
        return (new LastChangedWorker(path, subDirectoriesOnly, stopWhenFindYounger, false,
                observerOrNull)).getLastChanged();
    }

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
     * @throws InterruptedExceptionUnchecked if the thread that the method runs in gets interrupted.
     */
    public static long lastChangedRelative(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYoungerRelative) throws UnknownLastChangedException
    {
        return lastChangedRelative(path, subDirectoriesOnly, stopWhenFindYoungerRelative, null);
    }

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
     * @param observerOrNull If not <code>null</code>, will be updated on progress in scanning. This can be used to find out whether a (potentially)
     *            long-running recursive deletion call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @return The time when any file in (or below) <var>path</var> has last been changed in the file system.
     * @throws UnknownLastChangedException if the <var>path</var> does not exist or is not readable.
     * @throws InterruptedExceptionUnchecked if the thread that the method runs in gets interrupted.
     */
    public static long lastChangedRelative(final File path, final boolean subDirectoriesOnly,
            final long stopWhenFindYoungerRelative, final IActivityObserver observerOrNull)
            throws UnknownLastChangedException
    {
        return (new LastChangedWorker(path, subDirectoriesOnly, stopWhenFindYoungerRelative, true,
                observerOrNull)).getLastChanged();
    }

    /**
     * @return The time when any file in (or below) <var>path</var> has last been changed in the file system.
     * @throws UnknownLastChangedException if the <var>path</var> does not exist or is not readable.
     * @throws InterruptedExceptionUnchecked if the thread that the method runs in gets interrupted.
     */
    public static long lastChanged(final File path) throws UnknownLastChangedException
    {
        return lastChanged(path, false, 0L);
    }

    /**
     * Removes given <var>prefix</var> from given <var>file</var> and returns a new <code>File</code>.
     * <p>
     * Returns given <var>file</var> if prefix is <i>empty</i> or could not be found in the file name.
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
     * @param defaultFileNameOrNull the default name for the new file if the digit pattern could not be found in its name. If empty then "1" will be
     *            appended to its name.
     * @param regexOrNull pattern to find out the counter. If <code>null</code> then a default ( <code>(\\d+)</code>) will be used. The given
     *            <var>regex</var> must contain <code>(\\d+)</code> or <code>([0-9]+)</code>.
     */
    public final static File createNextNumberedFile(final File path, final Pattern regexOrNull,
            final String defaultFileNameOrNull)
    {
        assert path != null;

        final String filePath = path.getPath();
        final String defaultPathNameOrNull =
                (defaultFileNameOrNull == null) ? null : new File(path.getParentFile(),
                        defaultFileNameOrNull).getPath();
        final String uniqueFilePath =
                StringUtilities.createUniqueString(filePath, new IUniquenessChecker()
                    {
                        @Override
                        public boolean isUnique(String str)
                        {
                            return new File(str).exists() == false;
                        }
                    }, regexOrNull, defaultPathNameOrNull);
        return new File(uniqueFilePath);
    }

    public final static File createTimestampUniqueFile(final File path, final Pattern regex)
    {
        return createTimestampUniqueFile(path);
    }

    /**
     * Creates a time stamp numbered file in <var>path</var>.
     */
    public final static File createTimestampUniqueFile(final File path)
    {
        assert path != null;
        final String filePath = path.getPath();

        String uniqueTimestamp = getUniqueTimestamp();
        String uniqueFilePath = filePath + uniqueTimestamp;

        while (new File(uniqueFilePath).exists() == true)
        {
            uniqueTimestamp = getUniqueTimestamp();
            uniqueFilePath = filePath + uniqueTimestamp;

        }
        return new File(uniqueFilePath);
    }

    private static AtomicInteger uniqueIncrement = new AtomicInteger();

    /**
     * @return The format of the unique string consists of a time stamp and a unique number: <var>_yyyy_MM_dd_HH_mm_ss_SSS_</var> + unique_number
     */
    public final static String getUniqueTimestamp()
    {
        final String DATE_FORMAT = "_yyyy_MM_dd_HH_mm_ss_SSS_";

        long timestamp = System.currentTimeMillis();
        Date date = new Date(timestamp);
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(date) + uniqueIncrement.incrementAndGet();

    }

    /**
     * For given <var>root</var> and <var>file</var> extracts the relative path.
     * <p>
     * If given <var>file</var> does not contain given <var>root</var> path in its absolute path, then returns <code>null</code> (as the relative file
     * could not be determined).
     * </p>
     * 
     * @return a relative file path with no starting separator.
     */
    public final static String getRelativeFilePath(final File root, final File file)
    {
        assert root != null : "Given root can not be null.";
        assert file != null : "Given file can not be null.";
        final String filePath = file.getAbsolutePath();
        String rootPath = root.getAbsolutePath();
        if (filePath.equals(rootPath))
        {
            return "";
        }
        if (rootPath.endsWith(File.separator) == false)
        {
            rootPath += File.separator;
        }
        if (filePath.startsWith(rootPath))
        {
            return filePath.substring(rootPath.length());
        } else
        {
            return null;
        }
    }

    /**
     * Does the same as {@link #getRelativeFilePath(File, File)}, but throws exception if root file does not contain the specified file.
     */
    public final static String getRelativeFilePathOrDie(final File root, final File file)
    {
        String relativePath = getRelativeFilePath(root, file);
        if (relativePath == null)
        {
            throw UserFailureException.fromTemplate(
                    "Directory %s should be a subdirectory of directory %s.", file, root);
        }
        return relativePath;
    }

    /**
     * For given <var>relativePath</var> extracts the relative path of parent directory.
     * <p>
     * 
     * @returns <code>null</code> if given path is empty or <code>null</code>, otherwise parent directory path (empty string for current directory).
     */
    public final static String getParentRelativePath(String relativePath)
    {
        if (StringUtils.isBlank(relativePath))
        {
            return null;
        } else
        {
            if (relativePath.endsWith(File.separator))
            {
                return getParentRelativePath(relativePath.substring(0, relativePath.length() - File.separator.length()));
            }
            int lastIndexOf = relativePath.lastIndexOf(File.separator);
            if (lastIndexOf > -1)
            {
                return relativePath.substring(0, lastIndexOf);
            } else
            {
                return "";
            }
        }
    }

    /**
     * For given <var>relativePath</var> extracts the name of the file.
     */
    public final static String getFileNameFromRelativePath(String relativePath)
    {
        int index = relativePath.lastIndexOf('/');
        return (index < 0) ? relativePath : relativePath.substring(index + 1);
    }

    /**
     * For given <var>file</var> returns true if it is an HDF5 container/archive based on filename extension.
     * 
     * @returns <code>true</code> if and only if <var>file</var> has 'h5' or 'h5ar' as extension
     */
    public final static boolean hasHDF5ContainerSuffix(File file)
    {
        return FilenameUtils.isExtension(file.getName().toLowerCase(), HDF5_FILE_TYPES);
    }

    /**
     * @returns <code>true</code> if and only if <var>file</var> is a valid file and has 'h5' or 'h5ar' as extension
     */
    public final static boolean isHDF5ContainerFile(File file)
    {
        return file.isFile() && hasHDF5ContainerSuffix(file);
    }

    /**
     * Lists all resources in a given directory.
     * 
     * @param directory the directory to list
     * @param loggerOrNull logger, if <code>null</code> than no logging occurs
     * @return all files in <var>directory</var> or <code>null</code>, if <var>directory</var> does not exist or is not a directory.
     */
    public static File[] tryListFiles(final File directory, final ISimpleLogger loggerOrNull)
    {
        return tryListFiles(directory, ACCEPT_ALL_FILTER, loggerOrNull);
    }

    /**
     * Lists all resources in a given directory which match the filter.
     * 
     * @param directory the directory to list
     * @param filterOrNull only files matching this filter will show up in the result, if it is not <code>null</code>
     * @param loggerOrNull logger, if <code>null</code> than no logging occurs
     * @return all files in <var>directory</var> that match the filter, or <code>null</code>, if <var>directory</var> does not exist or is not a
     *         directory.
     */
    public static File[] tryListFiles(final File directory, final FileFilter filterOrNull,
            final ISimpleLogger loggerOrNull)
    {
        File[] paths = null;
        RuntimeException ex = null;
        try
        {
            paths = directory.listFiles(filterOrNull);
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
     * Lists files of given <var>directory</var>.
     * <p>
     * Throws an <code>EnvironmentFailureException</code> if {@link File#listFiles()} returns <code>null</code>.
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
     * A {@link FileFilter} that matches against a list of file extensions.
     * 
     * @author Bernd Rinn
     */
    private static final class ExtensionFileFilter implements FileFilter
    {
        private final String[] extensionsOrNull;

        private final IActivityObserver observerOrNull;

        private final boolean recursive;

        ExtensionFileFilter(String[] extensionsOrNull, boolean recursive,
                IActivityObserver observerOrNull)
        {
            this.extensionsOrNull = extensionsOrNull;
            this.recursive = recursive;
            this.observerOrNull = observerOrNull;
        }

        private boolean correctType(File file)
        {
            // Small optimization: if recursive, we know alrady that file.isDirectory() == false.
            return recursive || file.isFile();
        }

        private boolean match(String extensionFound)
        {
            if (extensionsOrNull == null)
            {
                return true;
            }
            if (extensionFound.length() == 0)
            {
                return false;
            }
            for (String ext : extensionsOrNull)
            {
                if (extensionFound.equalsIgnoreCase(ext))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean accept(File file)
        {
            if (observerOrNull != null)
            {
                observerOrNull.update();
            }
            if (recursive && file.isDirectory())
            {
                return true; // We need to traverse directories in any case.
            }
            return correctType(file) && match(FilenameUtils.getExtension(file.getName()));
        }

    }

    /**
     * A {@link FileFilter} that accepts all directories.
     * 
     * @author Bernd Rinn
     */
    private static final class DirectoryFilter implements FileFilter
    {
        private final IActivityObserver observerOrNull;

        DirectoryFilter(IActivityObserver observerOrNull)
        {
            this.observerOrNull = observerOrNull;
        }

        @Override
        public boolean accept(File pathname)
        {
            if (observerOrNull != null)
            {
                observerOrNull.update();
            }
            return pathname.isDirectory();
        }

    }

    /**
     * A {@link FileFilter} that is <code>true</code> if either of the two filters provided is <code>true</code>.
     * 
     * @author Bernd Rinn
     */
    private static final class OrFilter implements FileFilter
    {
        private final IActivityObserver observerOrNull;

        private final FileFilter filter1, filter2;

        OrFilter(IActivityObserver observerOrNull, FileFilter filter1, FileFilter filter2)
        {
            this.observerOrNull = observerOrNull;
            this.filter1 = filter1;
            this.filter2 = filter2;
        }

        @Override
        public boolean accept(File pathname)
        {
            if (observerOrNull != null)
            {
                observerOrNull.update();
            }
            return filter1.accept(pathname) || filter2.accept(pathname);
        }

    }

    /**
     * A {@link FileFilter} that matches against a list of file extensions and that
     * 
     * @author Bernd Rinn
     */
    private static final class TrueFilter implements FileFilter
    {
        private final IActivityObserver observerOrNull;

        TrueFilter(IActivityObserver observerOrNull)
        {
            this.observerOrNull = observerOrNull;
        }

        @Override
        public boolean accept(File pathname)
        {
            if (observerOrNull != null)
            {
                observerOrNull.update();
            }
            return true;
        }

    }

    /**
     * Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
     * 
     * @param directory The directory to search in.
     * @param filterOrNull The {@link FileFilter} used to decide whether a given file is listed or not. If <code>null</code>, all files are returned.
     * @param recursive If true all subdirectories are searched as well.
     * @return A list of java.io.File (all files) with the matching files, or an empty list, if <var>directory</var> is not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFiles(File directory, FileFilter filterOrNull, boolean recursive)
            throws EnvironmentFailureException
    {
        return listFiles(directory, filterOrNull, recursive, null);
    }

    /**
     * Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
     * 
     * @param directory The directory to search in.
     * @param filterOrNull The {@link FileFilter} used to decide whether a given file is listed or not. If <code>null</code>, all files are returned.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @return A list of java.io.File (all files) with the matching files, or an empty list, if <var>directory</var> is not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFiles(File directory, FileFilter filterOrNull, boolean recursive,
            IActivityObserver observerOrNull) throws EnvironmentFailureException
    {
        return listFiles(directory, filterOrNull, recursive, observerOrNull, null);
    }

    /**
     * Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
     * 
     * @param directory The directory to search in.
     * @param filterOrNull The {@link FileFilter} used to decide whether a given file is listed or not. If <code>null</code>, all files are returned.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @param loggerOrNull The logger to use to report failures in listing.
     * @return A list of java.io.File (all files) with the matching files, or an empty list, if <var>directory</var> is not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFiles(File directory, FileFilter filterOrNull, boolean recursive,
            IActivityObserver observerOrNull, ISimpleLogger loggerOrNull)
            throws EnvironmentFailureException
    {
        assert directory != null;

        final List<File> result = new LinkedList<File>();
        entryInternalListFiles(directory, result, (filterOrNull == null) ? new TrueFilter(
                observerOrNull) : filterOrNull, observerOrNull, recursive, FType.FILE, loggerOrNull);
        return result;
    }

    /**
     * Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
     * 
     * @param directory The directory to search in.
     * @param extensionsOrNull An array of extensions, ex. {"java","xml"}. If this parameter is <code>null</code>, all files are returned.
     * @param recursive If true all subdirectories are searched as well.
     * @return A list of java.io.File (all files) with the matching files, or an empty list, if <var>directory</var> is not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFiles(File directory, String[] extensionsOrNull, boolean recursive)
            throws EnvironmentFailureException
    {
        return listFiles(directory, extensionsOrNull, recursive, null, null);
    }

    /**
     * Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
     * 
     * @param directory The directory to search in.
     * @param extensionsOrNull An array of extensions, ex. {"java","xml"}. If this parameter is <code>null</code>, all files are returned.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @return A list of java.io.File (all files) with the matching files, or an empty list, if <var>directory</var> is not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFiles(File directory, String[] extensionsOrNull,
            boolean recursive, IActivityObserver observerOrNull) throws EnvironmentFailureException
    {
        return listFiles(directory, extensionsOrNull, recursive, observerOrNull, null);
    }

    /**
     * Finds files within a given directory (and optionally its subdirectories) which match an array of extensions.
     * 
     * @param directory The directory to search in.
     * @param extensionsOrNull An array of extensions, ex. {"java","xml"}. If this parameter is <code>null</code>, all files are returned.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @param loggerOrNull The logger to use to report failures in listing.
     * @return A list of java.io.File (all files) with the matching files, or an empty list, if <var>directory</var> is not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFiles(File directory, String[] extensionsOrNull,
            boolean recursive, IActivityObserver observerOrNull, ISimpleLogger loggerOrNull)
            throws EnvironmentFailureException
    {
        assert directory != null;

        final List<File> result = new LinkedList<File>();
        entryInternalListFiles(directory, result, new ExtensionFileFilter(extensionsOrNull,
                recursive, observerOrNull), observerOrNull, recursive, FType.FILE, loggerOrNull);
        return result;
    }

    /**
     * Finds directories within a given directory (and optionally its subdirectories).
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listDirectories(File directory, boolean recursive)
            throws EnvironmentFailureException
    {
        return listDirectories(directory, recursive, null, null);
    }

    /**
     * Finds directories within a given directory (and optionally its subdirectories).
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listDirectories(File directory, boolean recursive,
            IActivityObserver observerOrNull) throws EnvironmentFailureException
    {
        return listDirectories(directory, recursive, observerOrNull, null);
    }

    /**
     * Finds directories within a given directory (and optionally its subdirectories).
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @param loggerOrNull The logger to use to report failures in listing.
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listDirectories(File directory, boolean recursive,
            IActivityObserver observerOrNull, ISimpleLogger loggerOrNull)
            throws EnvironmentFailureException
    {
        assert directory != null;

        final List<File> result = new LinkedList<File>();
        entryInternalListFiles(directory, result, new DirectoryFilter(observerOrNull),
                observerOrNull, recursive, FType.DIRECTORY, loggerOrNull);
        return result;
    }

    /**
     * Finds files and directories within a given directory (and optionally its subdirectories).
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFilesAndDirectories(File directory, boolean recursive)
            throws EnvironmentFailureException
    {
        return listFilesAndDirectories(directory, recursive, null, null);
    }

    /**
     * Finds files and directories within a given directory (and optionally its subdirectories).
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFilesAndDirectories(File directory, boolean recursive,
            IActivityObserver observerOrNull) throws EnvironmentFailureException
    {
        return listFilesAndDirectories(directory, recursive, observerOrNull, null);
    }

    /**
     * Finds files and directories within a given directory (and optionally its subdirectories).
     * 
     * @param directory The directory to search in.
     * @param recursive If true all subdirectories are searched as well.
     * @param observerOrNull If not <code>null</code>, will be updated on progress of file gathering. This can be used to find out whether a
     *            (potentially) long-running file gathering call is alive-and-kicking or hangs (e.g. due to a remote directory becoming unresponsive).
     * @param loggerOrNull The logger to use to report failures in listing.
     * @return A list of java.io.File (all directories), or an empty list, if <var>directory</var> ist not a directory.
     * @throw {@link EnvironmentFailureException} if an error occurs on listing <var>directory</var>.
     */
    public static List<File> listFilesAndDirectories(File directory, boolean recursive,
            IActivityObserver observerOrNull, ISimpleLogger loggerOrNull)
            throws EnvironmentFailureException
    {
        assert directory != null;

        final List<File> result = new LinkedList<File>();
        entryInternalListFiles(directory, result, new TrueFilter(observerOrNull), observerOrNull,
                recursive, FType.EITHER, loggerOrNull);
        return result;
    }

    private enum FType
    {
        FILE, DIRECTORY, EITHER
    }

    private static void entryInternalListFiles(File directory, List<File> result,
            FileFilter filter, IActivityObserver observerOrNull, boolean recursive, FType ftype,
            final ISimpleLogger loggerOrNull)
    {
        if (recursive)
        {
            internalListFiles(directory, result, new OrFilter(null, new DirectoryFilter(null),
                    filter), observerOrNull, recursive, ftype, loggerOrNull);
        } else
        {
            internalListFiles(directory, result, filter, observerOrNull, recursive, ftype,
                    loggerOrNull);
        }
    }

    private static void internalListFiles(File directory, List<File> result, FileFilter filter,
            IActivityObserver observerOrNull, boolean recursive, FType ftype,
            final ISimpleLogger loggerOrNull)
    {
        RuntimeException ex = null;
        File[] filteredFilesAndDirectories = null;
        try
        {
            filteredFilesAndDirectories = directory.listFiles(filter);
        } catch (final RuntimeException e)
        {
            ex = e;
        }
        if (filteredFilesAndDirectories == null)
        {
            if (loggerOrNull != null)
            {
                logFailureInDirectoryListing(ex, directory, loggerOrNull);
            }
            if (ex != null)
            {
                throw new EnvironmentFailureException(
                        "Error listing directory '" + directory + "'", ex);
            } else
            {
                throw new EnvironmentFailureException("Path '" + directory.getPath()
                        + "' is not a directory.");
            }
        }
        for (File f : filteredFilesAndDirectories)
        {
            if (observerOrNull != null)
            {
                observerOrNull.update();
            }
            if (f.isDirectory())
            {
                if (ftype != FType.FILE && filter.accept(f))
                {
                    result.add(f);
                }
                if (recursive)
                {
                    internalListFiles(f, result, filter, observerOrNull, recursive, ftype,
                            loggerOrNull);
                }
            } else if (ftype != FType.DIRECTORY && filter.accept(f))
            {
                result.add(f);
            }
        }
    }

    /**
     * Normalizes given <var>file</var> path, removing double and single dot path steps.
     * <p>
     * It first tries to call {@link File#getCanonicalFile()}. If this fails, works with the file name returned by {@link File#getAbsolutePath()}.
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
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * <p>
     * By comparison with {@link FileUtils#byteCountToDisplaySize(long)}, the output of this version is more exact.
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
        } else if (size != 1)
        {
            displaySize = size + " bytes";
        } else
        {
            displaySize = "1 byte";
        }
        return displaySize;
    }

    /**
     * For given array of {@link File} returns the file names.
     */
    public final static String[] toFileNames(final File[] files)
    {
        assert files != null : "Unspecified files";
        final String[] fileNames = new String[files.length];
        int i = 0;
        for (final File file : files)
        {
            fileNames[i++] = file.getName();
        }
        return fileNames;
    }

    /**
     * Checks remote connections for specified path copier.
     */
    public static void checkPathCopier(IPathCopier copier, String host,
            String rsyncExecutableOnHostOrNull, String rsyncModuleOrNull,
            String rsyncPasswordFileOrNull, long millisToWaitForCompletion)
    {
        if (rsyncModuleOrNull != null)
        {
            final boolean connectionOK =
                    copier.checkRsyncConnectionViaRsyncServer(host, rsyncModuleOrNull,
                            rsyncPasswordFileOrNull, millisToWaitForCompletion);
            if (connectionOK == false)
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Connection to rsync module %s::%s failed", host, rsyncModuleOrNull);
            }
        } else
        {
            final boolean connectionOK =
                    copier.checkRsyncConnectionViaSsh(host, rsyncExecutableOnHostOrNull,
                            millisToWaitForCompletion);
            if (connectionOK == false)
            {
                throw ConfigurationFailureException.fromTemplate(
                        "No good rsync executable found on host '%s'", host);
            }
        }

    }

    /**
     * Checks the given <var>inFile</var> on whether it exists and is readable.
     * 
     * @throws CheckedExceptionTunnel of {@link FileNotFoundException} if the input file does not exist.
     * @throws CheckedExceptionTunnel of {@link IOException} if the input file cannot be read .
     */
    public static void checkInputFile(File inFile)
    {
        if (inFile.exists() == false)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(new FileNotFoundException("Input file '"
                    + inFile.getAbsolutePath() + "' not found."));
        }
        if (inFile.canRead() == false)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(new IOException("Cannot read input file '"
                    + inFile.getAbsolutePath() + "'."));
        }
    }

    /**
     * Checks the given <var>inFile</var> on whether it exists and is readable.
     * 
     * @throws CheckedExceptionTunnel of {@link FileExistsException} if the <var>outFile</var> exists and <var>overwriteOutFile</var> is
     *             <code>false</code>.
     * @throws CheckedExceptionTunnel of {@link IOException} if the output file cannot be written to.
     */
    public static void checkOutputFile(File outFile, IFileOverwriteStrategy fileOverwriteStrategy)
    {
        if (outFile.exists() && fileOverwriteStrategy.overwriteAllowed(outFile) == false)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(new FileExistsException(outFile));
        }
        // The fileOverwriteStrategy may have deleted the file, see whether it still exists.
        if (outFile.exists())
        {
            if (outFile.canWrite() == false)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(new IOException(
                        "Cannot write to output file '" + outFile.getAbsolutePath() + "'."));
            }
        } else
        {
            final File parent = outFile.getParentFile();
            if (parent != null && canCreateFile(parent) == false)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(new IOException(
                        "Cannot write to output directory '" + parent.getAbsolutePath() + "'."));
            }
        }
    }

    private static boolean canCreateFile(File directory)
    {
        if (directory.isDirectory() == false)
        {
            return false;
        }
        if (directory.canWrite())
        {
            return true;
        }
        // Workaround for Windows 7: sometimes gives a wrong answer on File.canWrite() for a
        // directory
        if (OSUtilities.isWindows())
        {
            try
            {
                final File f = File.createTempFile("fcc", null, directory);
                f.delete();
                return true;
            } catch (IOException ex)
            {
                return false;
            }
        } else
        {
            return false;
        }
    }

    private static String getTempDir()
    {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Return true if <code>fileName</code> is a valid filename.
     * <p>
     * The methods works by trying to create a file with the specified name. Thus, the method should not be called where performance matters.
     */
    public static boolean isValidFileName(String fileName)
    {
        if (StringUtils.isBlank(fileName) || fileName.contains(File.separator))
        {
            return false;
        }

        File tmpFile = new File(getTempDir(), fileName);
        try
        {
            if (tmpFile.createNewFile())
            {
                tmpFile.delete();
                return true;
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }

        return tmpFile.exists();
    }

    /**
     * Returns the size of the specified file or folder. In case of a folder and a Unix-type OS the command <tt>du</tt> will be used for better
     * performance in case of huge folders/subfolders.
     */
    public static long getSizeOf(File file)
    {
        if (file == null)
        {
            throw new IllegalArgumentException("Unspecified file.");
        }
        if (file.exists() == false)
        {
            throw new IllegalArgumentException("File does not exists: " + file);
        }
        if (file.isFile())
        {
            return file.length();
        }
        if (OSUtilities.isUnix() == false)
        {
            return FileUtils.sizeOfDirectory(file);
        }
        List<String> command =
                new ArrayList<String>(Arrays.asList("du", "-k", file.toString()));
        if (OSUtilities.isMacOS())
        {
            command.add(2, "-d0");
        } else
        {
            command.add(2, "--max-depth");
            command.add(3, "0");
        }
        ProcessResult result = ProcessExecutionHelper.run(command, operationLog, machineLog);
        if (result.isOK())
        {
            return Long.parseLong(result.getOutput().get(0).split("\\t")[0]) * 1024;
        }
        Throwable exception = result.getProcessIOResult().tryGetException();
        if (exception != null)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(exception);
        }
        throw new EnvironmentFailureException("The size of the folder '" + file
                + "' couldn't be determined:\n command: " + result.getCommandName()
                + "\n commad line arguments: " + result.getCommandLine()
                + "\n output: " + result.getOutput()
                + "\n error output: " + result.getErrorOutput());
    }
}
