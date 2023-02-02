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

package ch.systemsx.cisd.openbis.dss.generic.server.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractTabularDataGraphTest extends AbstractFileSystemTestCase
{
    /**
     * Return the tabular data as a DatasetFileLines.
     */
    protected DatasetFileLines getDatasetFileLines(String fileName) throws IOException
    {
        File file =
                new File("sourceTest/java/ch/systemsx/cisd/openbis/dss/generic/server/graph/"
                        + fileName);
        CsvReader reader = getCsvReader(file);
        List<String[]> lines = new ArrayList<String[]>();
        while (reader.readRecord())
        {
            lines.add(reader.getValues());
        }

        return new DatasetFileLines(file, fileName, lines);
    }

    /**
     * Return the tabular data as a DatasetFileLines.
     */
    protected DatasetFileLines getTestDatasetFileLines() throws IOException
    {
        return getDatasetFileLines("TestFeatureVectors.csv");
    }

    /**
     * Get a CsvReader for parsing a tabular data file.
     */
    protected CsvReader getCsvReader(File file) throws IOException
    {
        if (file.isFile() == false)
        {
            throw new UserFailureException(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(';');
        csvReader.setSkipEmptyRecords(true);
        csvReader.setUseComments(true);
        csvReader.setComment('#');

        return csvReader;
    }

    /**
     * Return an output stream for writing the image.
     */
    protected OutputStream getOutputStream(File outputFile)
    {
        try
        {
            return new FileOutputStream(outputFile);
        } catch (FileNotFoundException ex)
        {
            fail("Could not create output file");

            // never gets here
            return null;
        }
    }

    /**
     * Set to true if you want to be able to view the graphs output by the tests.
     */
    private static final boolean SAVE_RESULTS = false;

    /**
     * Return a file for writing an image to. Depending on the value of the SAVE_RESULTS constant, the image output file will be deleted at the end of
     * the test or saved at the end of the test.
     */
    protected File getImageOutputFile()
    {
        if (SAVE_RESULTS)
            return getImageOutputFileSavedAfterTest();
        else
            return getImageOutputFileDeletedAfterTest();

    }

    /**
     * Return a file for writing an image to. The file will be deleted when the test completes. Use this for normal testing.
     */
    protected File getImageOutputFileDeletedAfterTest()
    {
        return new File(workingDirectory, "test.png");
    }

    /**
     * Return a file for writing an image to. The file will be <b>not</b> be deleted when the test completes. Use this for looking at the result of
     * generating the images.
     */
    protected File getImageOutputFileSavedAfterTest()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        String className = getClass().toString();
        String[] classTokens = className.split("\\.");
        className = classTokens[classTokens.length - 1];
        String fileName =
                String.format("%s-%d-%d-%d--%02d-%02d-%02d-%03d.png", className, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR), calendar
                        .get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar
                        .get(Calendar.MILLISECOND));
        return getImageOutputFileSavedAfterTest(fileName);
    }

    /**
     * Return a file for writing an image to. The file will <b>not</b> be deleted when the test completes. Use this to view the result of the graph;
     * useful for tweaking appearance.
     * 
     * @param fileName The name for the output file
     */
    protected File getImageOutputFileSavedAfterTest(String fileName)
    {
        // For Testing, put it on the Desktop
        return new File(new File(TARGETS_DIRECTORY + "/" + UNIT_TEST_WORKING_DIRECTORY), fileName);
    }

}