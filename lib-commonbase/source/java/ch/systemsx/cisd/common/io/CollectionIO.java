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

package ch.systemsx.cisd.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.FromStringIdentityConverter;
import ch.systemsx.cisd.common.collection.IFromStringConverter;
import ch.systemsx.cisd.common.collection.IToStringConverter;
import ch.systemsx.cisd.common.collection.ToStringDefaultConverter;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * I/O operations for collections. Allows to read and write collections of {@link String}s and other objects from and to streams and files.
 * 
 * @author Bernd Rinn
 */
public class CollectionIO
{

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, CollectionIO.class);

    /**
     * Reads a collection of {@link String}s from a <var>resource</var>. One line in the resource corresponds to one entry in the collection.
     * 
     * @param resource The resource to read the collection from.
     * @param collection The collection to add the entries from the file to. It will <i>not</i> be cleared initially.
     * @return <code>true</code> if the collection was read successfully, or <code>false</code> otherwise.
     */
    public static boolean readCollectionFromResource(String resource, Collection<String> collection)
    {
        return readCollectionFromResource(resource, collection, FromStringIdentityConverter
                .getInstance());
    }

    /**
     * Reads a collection from a <var>resource</var>. One line in the resource corresponds to one entry in the collection.
     * 
     * @param resource The resource to read the collection from.
     * @param collection The collection to add the entries from the file to. It will <i>not</i> be cleared initially.
     * @param converter The converter to use in order to convert each line in the file.
     * @return <code>true</code> if the collection was read successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean readCollectionFromResource(String resource, Collection<T> collection,
            IFromStringConverter<T> converter)
    {
        InputStream resourceStream = CollectionIO.class.getResourceAsStream(resource);
        if (resourceStream == null)
        {
            machineLog.error(String.format("Resource '%s' not found.", resource));
            return false;
        }
        return readCollection(resourceStream, collection, converter, String.format(
                "<resource '%s'>", resource));
    }

    /**
     * Reads a collection of {@link String}s from a <var>file</var>. One line in the file corresponds to one entry in the collection.
     * 
     * @param file The file to read the collection from.
     * @param collection The collection to add the entries from the file to. It will <i>not</i> be cleared initially.
     * @return <code>true</code> if the collection was read successfully, or <code>false</code> otherwise.
     */
    public static boolean readCollection(File file, Collection<String> collection)
    {
        return readCollection(file, collection, FromStringIdentityConverter.getInstance());
    }

    /**
     * Reads a collection from a <var>file</var>. One line in the file corresponds to one entry in the collection.
     * 
     * @param file The file to read the collection from.
     * @param collection The collection to add the entries from the file to. It will <i>not</i> be cleared initially.
     * @param converter The converter to use in order to convert each line in the file.
     * @return <code>true</code> if the collection was read successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean readCollection(File file, Collection<T> collection,
            IFromStringConverter<T> converter)
    {
        try
        {
            return readCollection(new FileInputStream(file), collection, converter, String.format(
                    "<file '%s'>", file.getPath()));
        } catch (FileNotFoundException e)
        {
            machineLog.error(String.format("File '%s' is not accessible.", file.getPath()), e);
            return false;
        }
    }

    /**
     * Reads a collection of {@link String}s from <var>istream</var>. One line as read from the stream corresponds to one entry in the collection.
     * 
     * @param istream The stream to read the collection from.
     * @param collection The collection to add the entries from the file to. It will <i>not</i> be cleared initially.
     * @return <code>true</code> if the collection was read successfully, or <code>false</code> otherwise.
     */
    public static boolean readCollection(InputStream istream, Collection<String> collection)
    {
        return readCollection(istream, collection, FromStringIdentityConverter.getInstance());
    }

    /**
     * Reads a collection from a <var>istream</var>. One line read from the stream corresponds to one entry in the collection.
     * 
     * @param istream The stream to read the collection from.
     * @param collection The collection to add the entries from the file to. It will <i>not</i> be cleared initially.
     * @param converter The converter to use in order to convert each line in the file.
     * @return <code>true</code> if the collection was read successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean readCollection(InputStream istream, Collection<T> collection,
            IFromStringConverter<T> converter)
    {
        return readCollection(istream, collection, converter, null);
    }

    /**
     * Reads a collection from a <var>istream</var>. One line read read from the stream corresponds to one entry in the collection.
     * 
     * @param istream The stream to read the collection from.
     * @param collection The collection to add the entries from the file to. It will <i>not</i> be cleared initially.
     * @param converter The converter to use in order to convert each line in the file.
     * @param id The identifier associated with the reader (for use in error messages) or <code>null</code>, if no identifier is associated with the
     *            reader.
     * @return <code>true</code> if the collection was read successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean readCollection(InputStream istream, Collection<T> collection,
            IFromStringConverter<T> converter, String id)
    {
        assert istream != null;
        assert converter != null;
        assert collection != null;

        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(istream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                collection.add(converter.fromString(line));
            }
        } catch (IOException e)
        {
            if (id != null)
            {
                machineLog.error(String.format("Error when reading from %s.", id), e);
            } else
            {
                machineLog.error("Error when reading from reader.", e);
            }
            return false;
        } finally
        {
            if (bufferedReader != null)
            {
                try
                {
                    bufferedReader.close();
                } catch (IOException e)
                {
                    if (id != null)
                    {
                        machineLog.warn(String.format("Error when closing reader of %s.", id), e);
                    } else
                    {
                        machineLog.warn("Error when closing reader.", e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Reads a list of {@link String}s from a <var>resource</var>. One line in the resource corresponds to one entry in the list.
     * 
     * @param resource The resource to read the list from.
     * @return A new {@link ArrayList} containing the entries from the <var>file</var>, or <code>null</code> if there is an error when reading the
     *         <var>file</var>.
     */
    public static List<String> readListFromResource(String resource)
    {
        return readListFromResource(resource, FromStringIdentityConverter.getInstance());
    }

    /**
     * Reads a list from a <var>file</var>. One line in the file corresponds to one entry in the list.
     * 
     * @param resource The resource to read the list from.
     * @param converter The converter to use in order to convert each line in the file.
     * @return A new {@link ArrayList} containing the entries from the <var>file</var>, or <code>null</code> if there is an error when reading the
     *         <var>file</var>.
     */
    public static <T> List<T> readListFromResource(String resource,
            IFromStringConverter<T> converter)
    {
        assert resource != null;
        assert converter != null;

        final List<T> result = new ArrayList<T>();
        if (readCollectionFromResource(resource, result, converter))
        {
            return result;
        } else
        {
            return null;
        }
    }

    /**
     * Reads a set of {@link String}s from a <var>r</var>. One line in the resource corresponds to one entry in the set.
     * 
     * @param resource The resource to read the set from.
     * @return A new {@link HashSet} containing the entries from the <var>file</var>, or <code>null</code> if there is an error reading when the
     *         <var>file</var>.
     */
    public static Set<String> readSetFromResource(String resource)
    {
        return readSetFromResource(resource, FromStringIdentityConverter.getInstance());
    }

    /**
     * Reads a set from a <var>resource</var>. One line in the resource corresponds to one entry in the set.
     * 
     * @param resource The resource to read the set from.
     * @param converter The converter to use in order to convert each line in the file.
     * @return A new {@link HashSet} containing the entries from the <var>file</var>, or <code>null</code> if there is an error reading when the
     *         <var>file</var>.
     */
    public static <T> Set<T> readSetFromResource(String resource, IFromStringConverter<T> converter)
    {
        assert resource != null;
        assert converter != null;

        final Set<T> result = new HashSet<T>();
        if (readCollectionFromResource(resource, result, converter))
        {
            return result;
        } else
        {
            return null;
        }
    }

    /**
     * Reads a list of {@link String}s from a <var>file</var>. One line in the file corresponds to one entry in the list.
     * 
     * @param file The file to read the list from.
     * @return A new {@link ArrayList} containing the entries from the <var>file</var>, or <code>null</code> if there is an error when reading the
     *         <var>file</var>.
     */
    public static List<String> readList(File file)
    {
        return readList(file, FromStringIdentityConverter.getInstance());
    }

    /**
     * Reads a list from a <var>file</var>. One line in the file corresponds to one entry in the list.
     * 
     * @param file The file to read the list from.
     * @param converter The converter to use in order to convert each line in the file.
     * @return A new {@link ArrayList} containing the entries from the <var>file</var>, or <code>null</code> if there is an error when reading the
     *         <var>file</var>.
     */
    public static <T> List<T> readList(File file, IFromStringConverter<T> converter)
    {
        assert file != null;
        assert converter != null;

        final List<T> result = new ArrayList<T>();
        if (readCollection(file, result, converter))
        {
            return result;
        } else
        {
            return null;
        }
    }

    /**
     * Reads a set of {@link String}s from a <var>file</var>. One line in the file corresponds to one entry in the set.
     * 
     * @param file The file to read the set from.
     * @return A new {@link HashSet} containing the entries from the <var>file</var>, or <code>null</code> if there is an error reading when the
     *         <var>file</var>.
     */
    public static Set<String> readSet(File file)
    {
        return readSet(file, FromStringIdentityConverter.getInstance());
    }

    /**
     * Reads a set from a <var>file</var>. One line in the file corresponds to one entry in the set.
     * 
     * @param file The file to read the set from.
     * @param converter The converter to use in order to convert each line in the file.
     * @return A new {@link HashSet} containing the entries from the <var>file</var>, or <code>null</code> if there is an error reading when the
     *         <var>file</var>.
     */
    public static <T> Set<T> readSet(File file, IFromStringConverter<T> converter)
    {
        assert file != null;
        assert converter != null;

        final Set<T> result = new HashSet<T>();
        if (readCollection(file, result, converter))
        {
            return result;
        } else
        {
            return null;
        }
    }

    /**
     * Writes a collection to a <var>file</var>. One entry in the collection corresponds to one line in the file.
     * 
     * @param file The file to write the collection to. The file <i>will</i> be truncated initially.
     * @param iterable The iterable to write to the file.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean writeIterable(File file, Iterable<T> iterable)
    {
        return writeIterable(file, iterable, ToStringDefaultConverter.getInstance());
    }

    /**
     * Writes a collection to a <var>file</var>. One entry in the collection corresponds to one line in the file.
     * 
     * @param file The file to write the collection to. The file <i>will</i> be truncated initially.
     * @param iterable The iterable to write to the file.
     * @param converter The {@link IToStringConverter} to use to convert a <var>T</var> value into a {@link String}.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean writeIterable(File file, Iterable<T> iterable,
            IToStringConverter<? super T> converter)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            return writeIterable(fos, iterable, converter);
        } catch (FileNotFoundException e)
        {
            machineLog.error(String.format("File '%s' is not accessible.", file.getPath()), e);
            return false;
        } finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                } catch (IOException ex)
                {
                    machineLog.error(String.format("Error closing file '%s'.", file.getPath()), ex);
                    return false;
                }
            }
        }
    }

    /**
     * Writes a collection to a {@link OutputStream}. One entry in the collection corresponds to one line written to the stream.
     * 
     * @param ostream The stream to write the collection to.
     * @param iterable The iterable to write to the file.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean writeIterable(OutputStream ostream, Iterable<T> iterable)
    {
        return writeIterable(ostream, iterable, ToStringDefaultConverter.getInstance());
    }

    /**
     * Writes a collection to a {@link OutputStream}. One entry in the collection corresponds to one line written to the stream.
     * 
     * @param ostream The stream to write the collection to.
     * @param iterable The iterable to write to the file.
     * @param converter The {@link IToStringConverter} to use to convert a <var>T</var> value into a {@link String}.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean writeIterable(OutputStream ostream, Iterable<T> iterable,
            IToStringConverter<? super T> converter)
    {
        assert ostream != null;

        return writeIterable(new PrintStream(ostream), iterable, converter);
    }

    /**
     * Writes a collection to a {@link PrintStream}. One entry in the collection corresponds to one line written to the stream.
     * 
     * @param printStream The stream to write the collection to.
     * @param iterable The iterable to write to the file.
     * @param converter The {@link IToStringConverter} to use to convert a <var>T</var> value into a {@link String}.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean writeIterable(PrintStream printStream, Iterable<T> iterable,
            IToStringConverter<? super T> converter)
    {
        assert printStream != null;
        assert iterable != null;
        assert converter != null;

        for (T entry : iterable)
        {
            printStream.println(converter.toString(entry));
        }
        return (printStream.checkError() == false);
    }

    /**
     * Writes a collection to a {@link PrintStream}. One entry in the collection corresponds to one line written to the stream.
     * 
     * @param printStream The stream to write the collection to.
     * @param iterable The iterable to write to the file.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public static <T> boolean writeIterable(PrintStream printStream, Iterable<T> iterable)
    {
        return writeIterable(printStream, iterable, ToStringDefaultConverter.getInstance());
    }

    /**
     * Writes a collection to a {@link Writer}. One entry in the collection corresponds to one line written to the stream.
     * 
     * @param writer The writer to write the collection to.
     * @param iterable The iterable to write to the writer.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public final static <T> boolean writeIterable(final Writer writer, final Iterable<T> iterable,
            final IToStringConverter<? super T> converter)
    {
        assert writer != null : "Given Writer can not be null.";
        return writeIterable(new PrintWriter(writer), iterable, converter);
    }

    /**
     * Writes a collection to a {@link PrintWriter}. One entry in the collection corresponds to one line written to the stream.
     * 
     * @param writer The writer to write the collection to.
     * @param iterable The iterable to write to the writer.
     * @return <code>true</code> if the collection was written successfully, or <code>false</code> otherwise.
     */
    public final static <T> boolean writeIterable(final PrintWriter writer,
            final Iterable<T> iterable, final IToStringConverter<? super T> converterOrNull)
    {
        assert writer != null : "Given PrintWriter can not be null";
        assert iterable != null : "Given Iterable can not be null";
        final IToStringConverter<? super T> converter;
        if (converterOrNull == null)
        {
            converter = ToStringDefaultConverter.getInstance();
        } else
        {
            converter = converterOrNull;
        }
        for (final T entry : iterable)
        {
            writer.println(converter.toString(entry));
        }
        return writer.checkError() == false;
    }

}
