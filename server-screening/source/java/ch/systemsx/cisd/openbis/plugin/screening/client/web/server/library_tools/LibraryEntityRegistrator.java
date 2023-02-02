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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.string.UnicodeUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Creates files to register genes, oligos and plate with wells.
 * <p>
 * This registrator works with the assumption that the entities which should be registered do not already exist in openBIS. If it is the case we
 * should extend the implementation to fetch existing entities, LIBRARY_ID property can be used to recognize that.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class LibraryEntityRegistrator
{

    private final GeneRegistrator geneRegistrator;

    private final OligoRegistrator oligoRegistrator;

    private final PlateRegistrator plateRegistrator;

    public LibraryEntityRegistrator(QiagenScreeningLibraryColumnExtractor extractor,
            String experimentIdentifier, String plateGeometry, String spaceCode, String sampleProjectOrNull, String genesFile,
            String oligosFile, String platesFile) throws IOException
    {
        this.geneRegistrator = new GeneRegistrator(new File(genesFile));
        this.oligoRegistrator =
                new OligoRegistrator(new File(oligosFile),
                        extractor.getAdditionalOligoPropertyNames());
        this.plateRegistrator =
                new PlateRegistrator(new File(platesFile), experimentIdentifier, plateGeometry,
                        spaceCode, sampleProjectOrNull);
    }

    public void register(QiagenScreeningLibraryColumnExtractor extractor, String[] row)
            throws IOException
    {
        String geneId = geneRegistrator.tryRegister(extractor, row);
        if (geneId != null)
        {
            String oligoId = oligoRegistrator.register(extractor, row, geneId);
            String plateId = plateRegistrator.registerPlate(extractor, row);
            plateRegistrator.registerWell(extractor, row, plateId, oligoId, geneId);
        }
    }

    /** smust be called at the end of registration of all rows */
    public void saveResults() throws IOException, FileNotFoundException
    {
        plateRegistrator.saveResults();
        close();
    }

    private void close() throws IOException
    {
        geneRegistrator.close();
        oligoRegistrator.close();
        plateRegistrator.close();
    }

    abstract static protected class AbstractMetadataRegistrator
    {
        private static final String TAB = "\t";

        private final OutputStream stream;

        protected AbstractMetadataRegistrator(File file) throws FileNotFoundException
        {
            this.stream = new FileOutputStream(file);
        }

        protected void writeLine(String... tokens) throws IOException
        {
            writeLine(join(tokens));
        }

        // joins token into one line adding separators in between
        public static String join(String... tokens)
        {
            return StringUtils.join(tokens, TAB);
        }

        private void writeLine(String line) throws IOException
        {
            IOUtils.writeLines(Arrays.asList(line), "\n", stream,
                    UnicodeUtils.DEFAULT_UNICODE_CHARSET);
        }

        public void close() throws IOException
        {
            stream.close();
        }
    }

    private static class PlateRegistrator extends AbstractMetadataRegistrator
    {
        private static final String HEADER_PLATES = "[PLATE]\n"
                + join("identifier", "experiment", "$PLATE_GEOMETRY");

        private static final String HEADER_SIRNAS = "[" + ScreeningConstants.SIRNA_WELL_TYPE_CODE
                + "]\n" + join("identifier", "experiment", "container", "SIRNA", "GENE");

        private final Set<String/* plate code */> registeredPlates;

        private final String experimentIdentifier;

        private final String plateGeometry;

        private final String spaceCode;

        private final String sampleProjectOrNull;
        
        private final List<String[]> wellRegistrationBuffer;

        public PlateRegistrator(File outputPlateFile, String experimentIdentifier,
                String plateGeometry, String spaceCode, String sampleProjectOrNull) throws IOException
        {
            super(outputPlateFile);
            this.experimentIdentifier = experimentIdentifier;
            this.plateGeometry = plateGeometry;
            this.spaceCode = spaceCode;
            this.sampleProjectOrNull = sampleProjectOrNull;
            this.registeredPlates = new HashSet<String>();
            this.wellRegistrationBuffer = new ArrayList<String[]>();
            writeLine(HEADER_PLATES);
        }

        /** @return sampleIdentifier */
        public String registerPlate(QiagenScreeningLibraryColumnExtractor extractor, String[] row)
                throws IOException
        {
            String plateCode = extractor.getPlateCode(row);
            String sampleIdentifier = getSampleIdentifier(plateCode);
            if (registeredPlates.contains(plateCode) == false)
            {
                writeLine(sampleIdentifier, experimentIdentifier, plateGeometry);
                registeredPlates.add(plateCode);
            }
            return sampleIdentifier;
        }

        private String getSampleIdentifier(String plateCode)
        {
            return "/" + spaceCode + "/" + (sampleProjectOrNull == null ? "" : sampleProjectOrNull + "/") + plateCode;
        }

        public void registerWell(QiagenScreeningLibraryColumnExtractor extractor, String[] row,
                String plateId, String oligoId, String geneId) throws IOException
        {
            String wellCode = extractor.getWellCode(row);
            String wellIdentifier = plateId + ":" + wellCode;
            String oligoMaterialProperty =
                    oligoId + " (" + ScreeningConstants.SIRNA_PLUGIN_TYPE_NAME + ")";
            String geneMaterialProperty =
                    geneId + " (" + ScreeningConstants.GENE_PLUGIN_TYPE_CODE + ")";
            saveWell(wellIdentifier, experimentIdentifier, plateId, oligoMaterialProperty,
                    geneMaterialProperty);
        }

        private void saveWell(String... tokens)
        {
            wellRegistrationBuffer.add(tokens);
        }

        // saves all the wells to the file
        public void saveResults() throws IOException
        {
            writeLine(HEADER_SIRNAS);
            for (String[] wellLine : wellRegistrationBuffer)
            {
                writeLine(wellLine);
            }
        }
    }

    private static class GeneRegistrator extends AbstractMetadataRegistrator
    {
        private static final String HEADER = join("CODE", "DESCRIPTION",
                ScreeningConstants.GENE_SYMBOLS);

        private final Set<String/* gene code */> registeredGenes;

        public GeneRegistrator(File genesFile) throws IOException
        {
            super(genesFile);
            this.registeredGenes = new HashSet<String>();
            writeLine(HEADER);
        }

        // returns gene id or null if gene symbol is not specified
        public String tryRegister(QiagenScreeningLibraryColumnExtractor extractor, String[] row)
                throws IOException
        {
            String geneCode = extractor.getGeneId(row);
            if (StringUtils.isBlank(geneCode))
            {
                return null;
            }
            if (registeredGenes.contains(geneCode) == false)
            {
                String geneSymbol = extractor.getGeneSymbol(row);
                String desc = extractor.getGeneDescription(row);
                writeLine(geneCode, desc, geneSymbol);
                registeredGenes.add(geneCode);
            }
            return geneCode;
        }
    }

    private static class OligoRegistrator extends AbstractMetadataRegistrator
    {
        private static final String HEADER = join("CODE", "NUCLEOTIDE_SEQUENCE", "INHIBITOR_OF",
                "LIBRARY_ID");

        private final Set<String/* code */> registeredOligos;

        private final List<String> additionalPropertyNames;

        public OligoRegistrator(File file, List<String> additionalPropertyNames) throws IOException
        {
            super(file);
            this.registeredOligos = new HashSet<String>();
            this.additionalPropertyNames = additionalPropertyNames;
            writeLine(createHeader(additionalPropertyNames));
        }

        private static String createHeader(List<String> additionalPropertyNames)
        {
            String header = HEADER;
            for (String propertyName : additionalPropertyNames)
            {
                header = join(header, propertyName);
            }
            return header;
        }

        // / returns openbis id
        public String register(QiagenScreeningLibraryColumnExtractor extractor, String[] row,
                String inhibitedGeneCode) throws IOException
        {
            String geneId = extractor.getGeneId(row);
            String oligoId = extractor.getSiRNAId(row);
            String openbisOligoId = geneId + "_" + oligoId;
            if (containsCaseInsensitive(registeredOligos, openbisOligoId) == false)
            {
                String seq = extractor.getRNASequence(row);
                String geneMaterialProperty = inhibitedGeneCode + " (GENE)";
                String line = join(openbisOligoId, seq, geneMaterialProperty, oligoId);
                // add additional properties
                List<String> propertyValues =
                        extractor.getAdditionalOligoPropertyValues(row, additionalPropertyNames);
                for (int i = 0; i < propertyValues.size(); i++)
                {
                    line = join(line, propertyValues.get(i));
                }

                writeLine(line);
                addCaseInsensitive(registeredOligos, openbisOligoId);
            }
            return openbisOligoId;
        }

        private void addCaseInsensitive(Set<String> set, String value)
        {
            set.add(value.toLowerCase());
        }

        private boolean containsCaseInsensitive(Set<String> set, String value)
        {
            return set.contains(value.toLowerCase());
        }
    }
}
