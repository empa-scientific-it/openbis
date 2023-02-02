/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * Helper class/application to merge properties file. It tries to replace property values by values from the files to be merged assuming that all
 * key-value pairs are of the form <tt>&lt;key&gt; = &lt;value&gt;<tt>. If at least one properties file doesn't use
 * this simple syntax all properties files are just string-concatenated.
 * 
 * @author Franz-Josef Elmer
 */
public class PropertiesFileMerger
{
    private enum EntryType
    {
        COMMENT, PROPERTY
    }

    private static final class NotSimpleKeyValuePairException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public NotSimpleKeyValuePairException()
        {
            super();
        }
    }

    private static final class Entry
    {
        private final String line;

        private final EntryType type;

        private String key;

        private String value;

        Entry(String line)
        {
            this.line = line;
            String trimmedLine = line.trim();
            if (trimmedLine.length() == 0 || trimmedLine.startsWith("#"))
            {
                type = EntryType.COMMENT;
            } else
            {
                type = EntryType.PROPERTY;
                int indexOfEqualSign = line.indexOf('=');
                if (indexOfEqualSign < 0)
                {
                    throw new NotSimpleKeyValuePairException();
                }
                key = line.substring(0, indexOfEqualSign).trim();
                value = line.substring(indexOfEqualSign + 1).trim();
            }
        }

        public String getLine()
        {
            return line;
        }

        public EntryType getType()
        {
            return type;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            this.value = value;
        }

    }

    private static final class Entries
    {
        private final List<Entry> entries = new ArrayList<Entry>();

        private final Map<String, Entry> entryMap = new HashMap<String, Entry>();

        void addLine(String line)
        {
            Entry entry = new Entry(line);
            if (entry.getType() == EntryType.PROPERTY)
            {
                Entry existingEntry = entryMap.get(entry.getKey());
                if (existingEntry != null)
                {
                    existingEntry.setValue(entry.getValue());
                } else
                {
                    entries.add(entry);
                    entryMap.put(entry.getKey(), entry);
                }
            } else
            {
                entries.add(entry);
            }
        }

        public List<Entry> getEntries()
        {
            return entries;
        }
    }

    /**
     * Merges properties files specified by the arguments. The first file is overloaded by the content of the other files.
     */
    public static void main(String[] args)
    {
        main(args, SystemExit.SYSTEM_EXIT);
    }

    @Private
    static void main(String[] args, IExitHandler exitHandler)
    {
        if (args.length == 0)
        {
            System.err.println("Usage: java " + PropertiesFileMerger.class.getName()
                    + " <properties file> <overloading properties file 1> "
                    + "<overloading properties file 2> ...");
            exitHandler.exit(1);
        }
        File propertiesFile = new File(args[0]);
        check(propertiesFile, exitHandler);
        File[] overloadingPropertiesFiles = new File[args.length - 1];
        for (int i = 1; i < args.length; i++)
        {
            File overloadingPropertiesFile = new File(args[i]);
            check(overloadingPropertiesFile, exitHandler);
            overloadingPropertiesFiles[i - 1] = overloadingPropertiesFile;
        }
        mergePropertiesFiles(propertiesFile, overloadingPropertiesFiles);
    }

    private static void check(File propertiesFile, IExitHandler exitHandler)
    {
        if (propertiesFile.isFile() == false)
        {
            System.err.println("Isn't a file or does not exist: " + propertiesFile);
            exitHandler.exit(1);
        }
    }

    /**
     * Merges specified properties file with specified overloading properties files as described above.
     */
    public static void mergePropertiesFiles(File propertiesFile, File... overloadingPropertiesFiles)
    {
        if (overloadingPropertiesFiles.length == 0)
        {
            return;
        }
        try
        {
            Entries entries = new Entries();
            load(propertiesFile, entries);
            for (File overloadingPropertiesFile : overloadingPropertiesFiles)
            {
                load(overloadingPropertiesFile, entries);
            }
            PrintWriter printWriter = null;
            try
            {
                printWriter = new PrintWriter(propertiesFile);
                for (Entry entry : entries.getEntries())
                {
                    if (entry.getType() == EntryType.PROPERTY)
                    {
                        printWriter.println(entry.getKey() + " = " + entry.getValue());
                    } else
                    {
                        printWriter.println(entry.getLine());

                    }
                }
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                IOUtils.closeQuietly(printWriter);
            }
        } catch (NotSimpleKeyValuePairException ex)
        {
            concatenateFiles(propertiesFile, overloadingPropertiesFiles);
        }
    }

    private static void concatenateFiles(File propertiesFile, File... overloadingPropertieFiles)
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter(propertiesFile, true);
            for (File overloadingPropertiesFile : overloadingPropertieFiles)
            {
                writer.write("\n");
                FileReader reader = null;
                try
                {
                    reader = new FileReader(overloadingPropertiesFile);
                    IOUtils.copy(reader, writer);
                } finally
                {
                    IOUtils.closeQuietly(reader);
                }
            }
        } catch (IOException ex1)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex1);
        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private static void load(File propertiesFile, Entries entries)
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(propertiesFile));
            String line;
            while ((line = reader.readLine()) != null)
            {
                entries.addLine(line);
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }
}
