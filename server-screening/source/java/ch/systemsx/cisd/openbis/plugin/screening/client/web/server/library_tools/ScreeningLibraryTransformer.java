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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.library_tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.string.UnicodeUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.MaterialConfigurationProvider;

/**
 * Transforms a screening library file and produces files which can be uploaded to openBIS: genes, oligos and plates with wells.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningLibraryTransformer
{
    private final static char DEFAULT_SEPARATOR = ',';

    private static final String GENES_FILE_NAME = "genes.txt";

    private static final String SIRNAS_FILE_NAME = "oligos.txt";

    private static final String PLATES_FILE_NAME = "plates.txt";

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        if (args.length != 4)
        {
            System.err.println("Invalid parameters. Expected: "
                    + "<library-file-path> <experiment-identifier> <plate-geometry> <space>");
            System.exit(1);
        }
        String experimentIdentifier = args[1];
        String plateGeometry = args[2];
        String spaceCode = args[3];
        String fileName = args[0];
        System.out.println("Processing...");
        File input = new File(fileName);
        if (input.isFile() == false)
        {
            System.err.println(input + " does not exist or is not a file.");
            return;
        }
        Status status =
                readLibrary(new FileInputStream(input), DEFAULT_SEPARATOR, experimentIdentifier,
                        plateGeometry, spaceCode, null, GENES_FILE_NAME, SIRNAS_FILE_NAME,
                        PLATES_FILE_NAME);
        if (status.isError())
        {
            System.err.println(status.tryGetErrorMessage());
        } else
        {
            System.out.println("Done, look for results in " + new File(".").getAbsolutePath());
        }
    }

    public static Status readLibrary(InputStream input, char separator,
            String experimentIdentifier, String plateGeometry, String spaceCode, String sampleProjectOrNull, 
            String genesFile, String oligosFile, String platesFile)
    {
        CsvReader csvReader = null;
        try
        {
            csvReader = createReader(input, separator);
            boolean headerPresent = csvReader.readRecord();
            if (headerPresent == false)
            {
                throw new UserFailureException("header not found");
            }
            String[] headers = csvReader.getValues();
            QiagenScreeningLibraryColumnExtractor extractor =
                    new QiagenScreeningLibraryColumnExtractor(headers,
                            MaterialConfigurationProvider.getInstance());
            LibraryEntityRegistrator registrator =
                    new LibraryEntityRegistrator(extractor, experimentIdentifier, plateGeometry,
                            spaceCode, sampleProjectOrNull, genesFile, oligosFile, platesFile);
            while (csvReader.readRecord())
            {
                String[] row = csvReader.getValues();
                registrator.register(extractor, row);
            }
            registrator.saveResults();
            return Status.OK;
        } catch (Exception ex)
        {
            return Status.createError(ex.getMessage());
        } finally
        {
            if (csvReader != null)
            {
                csvReader.close();
            }
        }
    }

    static CsvReader createReader(InputStream input, char separator) throws FileNotFoundException,
            IOException, UserFailureException
    {
        CsvReader csvReader = new CsvReader(input, UnicodeUtils.getDefaultUnicodeCharset());
        csvReader.setDelimiter(separator);
        csvReader.setSafetySwitch(false);
        return csvReader;
    }

}
