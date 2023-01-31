/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Piotr Buczek
 */
public class CsvFileReaderHelper
{

    /**
     * Configuration for reading csv file.
     * 
     * @author Piotr Buczek
     */
    public interface ICsvFileReaderConfiguration
    {

        public boolean isIgnoreComments();

        public boolean isSkipEmptyRecords();

        public char getColumnDelimiter();

        public char getCommentDelimiter();
    }

    /**
     * Get a CsvReader for parsing a tabular data file with default configuration.
     */
    public static CsvReader getCsvReader(File file) throws IOException
    {
        return getCsvReader(file, new DefaultCsvFileReaderConfiguration());
    }

    /**
     * Get a CsvReader for parsing a tabular data file with specified <var>configuration</var>.
     */
    public static CsvReader getCsvReader(File file, ICsvFileReaderConfiguration configuration)
            throws IOException
    {
        if (file.isFile() == false)
        {
            throw new UserFailureException(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(configuration.getColumnDelimiter());
        csvReader.setUseComments(configuration.isIgnoreComments());
        csvReader.setComment(configuration.getCommentDelimiter());
        csvReader.setSkipEmptyRecords(configuration.isSkipEmptyRecords());

        return csvReader;
    }

    /**
     * Return the tabular data as a DatasetFileLines with default configuration.
     */
    public static DatasetFileLines getDatasetFileLines(File file) throws IOException
    {
        return getDatasetFileLines(file, new DefaultCsvFileReaderConfiguration());
    }

    /**
     * Return the tabular data as a DatasetFileLines with specified <var>configuration</var>.
     */
    public static DatasetFileLines getDatasetFileLines(File file,
            ICsvFileReaderConfiguration configuration) throws IOException
    {
        CsvReader reader = null;
        try
        {
            reader = CsvFileReaderHelper.getCsvReader(file, configuration);
            List<String[]> lines = new ArrayList<String[]>();

            while (reader.readRecord())
            {
                lines.add(reader.getValues());
            }

            return new DatasetFileLines(file, file.getPath(), lines);
        } finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    /**
     * Default configuration for reading csv file:
     * <ul>
     * <li>';' - column delimiter
     * <li>'#' - comment delimiter
     * <li>ignoring empty records
     * </ul>
     * 
     * @author Piotr Buczek
     */
    public static class DefaultCsvFileReaderConfiguration implements ICsvFileReaderConfiguration
    {
        public static final ICsvFileReaderConfiguration INSTANCE =
                new DefaultCsvFileReaderConfiguration();

        @Override
        public char getColumnDelimiter()
        {
            return ';';
        }

        @Override
        public char getCommentDelimiter()
        {
            return '#';
        }

        @Override
        public boolean isIgnoreComments()
        {
            return true;
        }

        @Override
        public boolean isSkipEmptyRecords()
        {
            return true;
        }
    }

    /**
     * Default configuration for reading TSV file:
     */
    public static class DefaultTsvFileReaderConfiguration extends DefaultCsvFileReaderConfiguration
    {
        @Override
        public char getColumnDelimiter()
        {
            return '\t';
        }
    }
}
