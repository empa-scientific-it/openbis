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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.openbis.generic.shared.util.MaterialConfigurationProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.library_tools.QiagenScreeningLibraryColumnExtractor.GeneDetails;

/**
 * Merges image analysis results with gene information from library.
 * 
 * @author Tomasz Pylak
 */
public class ImageAnalysisGeneMerger
{
    private static final char SEPARATOR = ',';

    private static final String GENE_SYMBOL = "gene";

    private static final String GENE_DESCRIPTION = "description";

    public static void main(String[] args) throws Exception
    {
        if (args.length != 3)
        {
            error("Invalid parameters. Expected: "
                    + "<library-file-path> <image-analysis-folder> <output-folder>");
        }
        CsvReader libraryReader = readFile(new File(args[0]));
        // should contain one file per plate analysis results
        File analysisFolder = new File(args[1]);
        // folder where results will be saved
        File outputFolder = new File(args[2]);
        Map<PlateWellLocation, GeneDetails> geneMap = readGeneMap(libraryReader);
        libraryReader.close();
        mergeAnalysisDirWithGenes(analysisFolder, outputFolder, geneMap);
    }

    private static void mergeAnalysisDirWithGenes(File analysisFolder, File outputFolder,
            Map<PlateWellLocation, GeneDetails> geneMap) throws Exception
    {
        outputFolder.mkdirs();
        for (File plateAnalysisFile : analysisFolder.listFiles())
        {
            File outFile = new File(outputFolder, plateAnalysisFile.getName());
            mergeAnalysisFileWithGenes(plateAnalysisFile, outFile, geneMap);
        }
    }

    private static void mergeAnalysisFileWithGenes(File plateAnalysisFile, File outFile,
            Map<PlateWellLocation, GeneDetails> geneMap) throws Exception
    {
        // open file to read
        CsvReader reader = readFile(plateAnalysisFile);
        boolean headerPresent = reader.readRecord();
        if (headerPresent == false)
        {
            throw error("header not found");
        }
        String orgHeaders = reader.getRawRecord();
        PlateImageAnalysisColumnExtractor extractor =
                new PlateImageAnalysisColumnExtractor(reader.getValues());

        // open file to write results
        OutputStream out = new FileOutputStream(outFile);
        writeLine(createHeader(orgHeaders), out);

        while (reader.readRecord())
        {
            String[] row = reader.getValues();
            PlateWellLocation loc = extractor.getPlateWellLocation(row);
            GeneDetails gene = geneMap.get(loc);
            String resultLine = createLine(reader.getRawRecord(), gene);
            writeLine(resultLine, out);
        }
        out.close();
    }

    private static void writeLine(String line, OutputStream out) throws IOException
    {
        IOUtils.writeLines(Arrays.asList(line), "\n", out);
    }

    private static String createHeader(String originalLine)
    {
        return originalLine + SEPARATOR + GENE_SYMBOL + SEPARATOR + GENE_DESCRIPTION;
    }

    private static String createLine(String originalLine, GeneDetails geneOrNull)
    {
        return originalLine + SEPARATOR + (geneOrNull == null ? "" : geneOrNull.getSymbol())
                + SEPARATOR + (geneOrNull == null ? "" : quote(geneOrNull.getDescription()));
    }

    private static String quote(String value)
    {
        return "\"" + value + "\"";
    }

    private static Map<PlateWellLocation, GeneDetails> readGeneMap(CsvReader libraryReader)
            throws Exception
    {
        Map<PlateWellLocation, GeneDetails> map = new HashMap<PlateWellLocation, GeneDetails>();
        boolean headerPresent = libraryReader.readRecord();
        if (headerPresent == false)
        {
            throw error("header not found");
        }
        String[] headers = libraryReader.getValues();
        QiagenScreeningLibraryColumnExtractor extractor =
                new QiagenScreeningLibraryColumnExtractor(headers,
                        MaterialConfigurationProvider.getInstance());
        while (libraryReader.readRecord())
        {
            String[] row = libraryReader.getValues();
            PlateWellLocation loc = extractor.getPlateWellLocation(row);
            GeneDetails gene = extractor.getGeneDetails(row);
            map.put(loc, gene);
        }
        return map;
    }

    static CsvReader readFile(File file) throws FileNotFoundException, IOException
    {
        if (file.isFile() == false)
        {
            error(file + " does not exist or is not a file.");
        }
        FileInputStream fileInputStream = new FileInputStream(file);

        CsvReader csvReader = new CsvReader(fileInputStream, Charset.defaultCharset());
        csvReader.setDelimiter(SEPARATOR);
        csvReader.setSafetySwitch(false);
        return csvReader;
    }

    private static Exception error(String msg)
    {
        System.err.println(msg);
        System.exit(1);
        return new Exception();
    }
}
