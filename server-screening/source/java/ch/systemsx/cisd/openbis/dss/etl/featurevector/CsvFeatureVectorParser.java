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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabelUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CsvFileReaderHelper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;

/**
 * Converts feature vectors from CSV files to {@link FeatureDefinition} objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Tomasz Pylak
 */
public class CsvFeatureVectorParser
{
    public static List<FeatureDefinition> parse(File dataSet, Properties properties)
            throws IOException
    {
        FeatureVectorStorageProcessorConfiguration storageProcessorConfiguration =
                new FeatureVectorStorageProcessorConfiguration(properties);
        return parse(dataSet, storageProcessorConfiguration);
    }

    public static List<FeatureDefinition> parse(File dataSet,
            FeatureVectorStorageProcessorConfiguration configuration) throws IOException
    {
        CsvFeatureVectorParserConfiguration convertorConfig =
                new CsvFeatureVectorParserConfiguration(configuration);
        DatasetFileLines datasetFileLines =
                CsvFileReaderHelper.getDatasetFileLines(dataSet, configuration);
        CsvFeatureVectorParser parser =
                new CsvFeatureVectorParser(datasetFileLines, convertorConfig);
        return parser.parse();
    }

    public static class CsvFeatureVectorParserConfiguration
    {
        private final String wellRowColumn;

        private final String wellColumnColumn;

        private final boolean isSplit;

        private final Set<String> columnsToBeIgnored;

        public CsvFeatureVectorParserConfiguration(FeatureVectorStorageProcessorConfiguration config)
        {
            this(config.getWellRow(), config.getWellColumn(), config.getColumnsToBeIgnored());
        }

        public CsvFeatureVectorParserConfiguration(String wellRow, String wellColumn)
        {
            this(wellRow, wellColumn, Collections.<String> emptySet());
        }

        public CsvFeatureVectorParserConfiguration(String wellRow, String wellColumn,
                Set<String> columnsToBeIgnored)
        {
            this.wellRowColumn = wellRow;
            this.wellColumnColumn = wellColumn;
            this.columnsToBeIgnored = columnsToBeIgnored;

            isSplit = (false == wellRow.equals(wellColumn));
        }

        public String getWellRowColumn()
        {
            return wellRowColumn;
        }

        public String getWellColumnColumn()
        {
            return wellColumnColumn;
        }

        public boolean isSplit()
        {
            return isSplit;
        }

        public boolean shouldColumnBeIgnored(String column)
        {
            return columnsToBeIgnored.contains(column);
        }
    }

    private final CsvFeatureVectorParserConfiguration configuration;

    private final String[] header;

    private final List<String[]> lines;

    private final ArrayList<FeatureColumn> columns = new ArrayList<FeatureColumn>();

    // Will be initialized during conversion
    private int xColumn = -1;

    private int yColumn = -1;

    private int maxRowFound = 0;

    private int maxColFound = 0;

    public CsvFeatureVectorParser(DatasetFileLines fileLines,
            CsvFeatureVectorParserConfiguration config)
    {
        this.configuration = config;
        this.header = fileLines.getHeaderLabels();
        this.lines = fileLines.getDataLines();
    }

    public List<FeatureDefinition> parse()
    {
        initializeColumns();
        readLines();

        return convertColumnsToFeatureDefinitions();
    }

    private List<FeatureDefinition> convertColumnsToFeatureDefinitions()
    {
        List<FeatureDefinition> result = new ArrayList<FeatureDefinition>();
        Counters<String> counters = new Counters<String>();
        for (FeatureColumn column : columns)
        {
            if ((true == column.isWellName) || column.isEmpty())
            {
                continue;
            }
            FeatureDefinition featureVector = convertColumnToFeatureDefinition(column, counters);
            result.add(featureVector);
        }

        return result;
    }

    private FeatureDefinition convertColumnToFeatureDefinition(FeatureColumn column,
            Counters<String> counters)
    {
        CodeAndLabel codeAndTitle = CodeAndLabelUtil.create(column.name);
        ImgFeatureDefDTO featureDef = new ImgFeatureDefDTO();
        featureDef.setLabel(codeAndTitle.getLabel());
        featureDef.setDescription(codeAndTitle.getLabel());
        String code = codeAndTitle.getCode();
        int count = counters.count(code);
        featureDef.setCode(count == 1 ? code : code + count);

        return column.getFeatureDefinition(featureDef);
    }

    private void readLines()
    {
        for (String[] line : lines)
        {
            readLine(line);
        }
    }

    private void readLine(String[] line)
    {
        final WellLocation well = readWellLocationFromLine(line);
        for (FeatureColumn column : columns)
        {
            if (true == column.isWellName)
            {
                continue;
            }
            String columnValue = line[column.index];
            if (StringUtils.isBlank(columnValue) == false)
            {
                column.addValue(well, columnValue);
            }
        }

        if (well.getRow() > maxRowFound)
        {
            maxRowFound = well.getRow();
        }

        if (well.getColumn() > maxColFound)
        {
            maxColFound = well.getColumn();
        }
    }

    private WellLocation readWellLocationFromLine(String[] line)
    {
        if (configuration.isSplit())
        {
            String rowString = line[xColumn];
            String colString = line[yColumn];
            return WellLocation.parseLocationStr(rowString, colString);
        } else
        {
            return WellLocation.parseLocationStr(line[xColumn]);
        }
    }

    private void initializeColumns()
    {
        for (int i = 0; i < header.length; ++i)
        {
            String headerName = header[i];
            boolean isWellName = true;
            if (configuration.getWellRowColumn().equals(headerName))
            {
                xColumn = i;
            } else if (configuration.getWellColumnColumn().equals(headerName))
            {
                yColumn = i;
            } else if (configuration.shouldColumnBeIgnored(headerName) == false)
            {
                isWellName = false;
            }
            FeatureColumn featureColumn = new FeatureColumn(i, headerName, isWellName);
            columns.add(featureColumn);
        }

        if (false == configuration.isSplit())
        {
            yColumn = xColumn;
        }

        if (xColumn < 0)
        {
            throw UserFailureException.fromTemplate(
                    "Failed to identify well-row column. The specified input file "
                            + "does not contain header with name '%s'.",
                    configuration.getWellRowColumn());
        }

        if (yColumn < 0)
        {
            throw UserFailureException.fromTemplate(
                    "Failed to identify well-col column. The specified input file "
                            + "does not contain header with name '%s'.",
                    configuration.getWellColumnColumn());
        }
    }

    private static class FeatureColumn
    {
        private final int index;

        private final String name;

        private final boolean isWellName;

        private final FeatureValuesMap values;

        private FeatureColumn(int index, String name, boolean isWellName)
        {
            this.index = index;
            this.name = name;
            this.isWellName = isWellName;
            values = new FeatureValuesMap(0., 0.);
        }

        public void addValue(WellLocation well, String columnValue)
        {
            values.addValue(columnValue, well);
        }

        public FeatureDefinition getFeatureDefinition(ImgFeatureDefDTO featureDef)
        {
            return new FeatureDefinition(featureDef, values);
        }

        public boolean isEmpty()
        {
            return values.isEmpty();
        }
    }
}
