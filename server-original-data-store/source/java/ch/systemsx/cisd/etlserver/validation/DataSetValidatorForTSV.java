/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.validation;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.etlserver.utils.FileScanner;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Validator for data sets containing TAB-separated value (TSV) files. Such a file is valid if
 * <ul>
 * <li>All column headers are unique.
 * <li>For each column a {@link ColumnDefinition} as specified by the properties are found.
 * <li>All values of a column are valid in accordance to the {@link IValidator} created by the {@link ColumnDefinition} instance of the column.
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetValidatorForTSV implements IDataSetValidator
{
    static final String PATH_PATTERNS_KEY = "path-patterns";

    static final String EXCLUDE_PATH_PATTERNS_KEY = "exclude-path-patterns";

    static final String IGNORE_EMPTY_LINES_KEY = "ignore-empty-lines";

    static final String STRICT_ROW_SIZE_KEY = "strict-row-size";

    static final String COLUMNS_KEY = "columns";

    private final List<FileScanner> fileScanners;

    private final List<FileScanner> excludeFileScanners;

    private final List<ColumnDefinition> unorderedDefinitions;

    private final Map<Integer, ColumnDefinition> orderedDefinitions;

    private final boolean ignoreEmptyLines;

    private final boolean strictRowSize;

    public DataSetValidatorForTSV(Properties properties)
    {
        fileScanners = new ArrayList<FileScanner>();
        ignoreEmptyLines = PropertyUtils.getBoolean(properties, IGNORE_EMPTY_LINES_KEY, true);
        strictRowSize = PropertyUtils.getBoolean(properties, STRICT_ROW_SIZE_KEY, true);
        String pathPatterns = properties.getProperty(PATH_PATTERNS_KEY, "*");
        StringTokenizer tokenizer = new StringTokenizer(pathPatterns, ",");
        while (tokenizer.hasMoreTokens())
        {
            String pathPattern = tokenizer.nextToken().trim();
            fileScanners.add(new FileScanner(pathPattern));
        }
        excludeFileScanners = new ArrayList<FileScanner>();
        String excludePathPatterns = properties.getProperty(EXCLUDE_PATH_PATTERNS_KEY);
        if (excludePathPatterns != null)
        {
            tokenizer = new StringTokenizer(excludePathPatterns, ",");
            while (tokenizer.hasMoreTokens())
            {
                String pathPattern = tokenizer.nextToken().trim();
                excludeFileScanners.add(new FileScanner(pathPattern));
            }
        }
        unorderedDefinitions = new ArrayList<ColumnDefinition>();
        orderedDefinitions = new LinkedHashMap<Integer, ColumnDefinition>();
        SectionProperties[] columnsProperties =
                PropertyParametersUtil.extractSectionProperties(properties, COLUMNS_KEY, false);
        for (SectionProperties sectionProperties : columnsProperties)
        {
            try
            {
                String name = sectionProperties.getKey();
                ColumnDefinition definition =
                        ColumnDefinition.create(name, sectionProperties.getProperties());
                Integer orderOrNull = definition.getOrderOrNull();
                if (orderOrNull == null)
                {
                    unorderedDefinitions.add(definition);
                } else
                {
                    ColumnDefinition previousDefinition =
                            orderedDefinitions.put(orderOrNull, definition);
                    if (previousDefinition != null)
                    {
                        throw new ConfigurationFailureException(
                                "There is already a column definied for order " + orderOrNull + ".");
                    }
                }
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException("Couldn't create column definition '"
                        + sectionProperties.getKey() + "': " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void assertValidDataSet(DataSetType dataSetType, File incomingDataSetFileOrFolder)
    {
        Set<File> excludedFiles = new HashSet<File>();
        for (FileScanner fileScanner : excludeFileScanners)
        {
            excludedFiles.addAll(fileScanner.scan(incomingDataSetFileOrFolder));
        }

        for (FileScanner fileScanner : fileScanners)
        {
            List<File> files = fileScanner.scan(incomingDataSetFileOrFolder);
            for (File file : files)
            {
                if (excludedFiles.contains(file) == false)
                {
                    assertValidFile(dataSetType, file);
                }
            }
        }
    }

    private void assertValidFile(DataSetType dataSetType, File file)
    {
        if (file.isFile() == false)
        {
            return;
        }
        Reader reader = null;
        try
        {
            reader = new FileReader(file);
            String dataSourceName = file.toString();
            assertValidDataSet(dataSetType, reader, dataSourceName);
        } catch (RuntimeException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    @Override
    public void assertValidDataSet(DataSetType dataSetType, Reader reader, String dataSourceName)
    {
        TabSeparatedValueTable table =
                new TabSeparatedValueTable(reader, dataSourceName, ignoreEmptyLines, strictRowSize,
                        false);
        List<String> headers = table.getHeaders();
        assertUniqueHeaders(headers);
        ColumnDefinition[] definitions = findColumnDefinitions(headers);
        IValidator[] validators = new IValidator[definitions.length];
        for (int i = 0; i < validators.length; i++)
        {
            validators[i] = definitions[i].createValidator(headers.get(i));
        }
        int lineNumber = 1;
        while (table.hasMoreRows())
        {
            lineNumber++;
            List<String> row = table.tryToGetNextRow();
            if (row.size() > definitions.length)
            {
                for (int i = definitions.length; i < row.size(); i++)
                {
                    if (StringUtils.isNotBlank(row.get(i)))
                    {
                        throw new UserFailureException("The row in line " + lineNumber
                                + " has " + row.size() + " cells instead of "
                                + definitions.length);
                    }
                }
            }
            for (int i = 0, n = Math.min(row.size(), validators.length); i < n; i++)
            {
                try
                {
                    validators[i].assertValid(row.get(i));
                } catch (RuntimeException ex)
                {
                    throw new UserFailureException("Error in file '" + dataSourceName + "': "
                            + (i + 1) + ". cell in line " + lineNumber + ": " + ex.getMessage(), ex);
                }
            }
        }
    }

    private void assertUniqueHeaders(List<String> headers)
    {
        HashSet<String> headerSet = new HashSet<String>();
        for (String header : headers)
        {
            if (headerSet.contains(header))
            {
                throw new UserFailureException("Column header '" + header + "' appeared twice.");
            }
            headerSet.add(header);
        }
    }

    private ColumnDefinition[] findColumnDefinitions(List<String> columnHeaders)
    {
        ColumnDefinition[] definitions = findOrderedColumnDefinitions(columnHeaders);
        List<ColumnDefinition> remainingDefinitions =
                new LinkedList<ColumnDefinition>(unorderedDefinitions);
        for (int i = 0; i < definitions.length; i++)
        {
            if (definitions[i] == null)
            {
                ColumnDefinition orderedColumDefinitionOrNull = orderedDefinitions.get(i + 1);
                definitions[i] =
                        getDefinition(remainingDefinitions, orderedColumDefinitionOrNull,
                                columnHeaders, i);
            }
        }
        String list = createListOfMissingColumns(remainingDefinitions);
        if (list.length() > 0)
        {
            throw new UserFailureException(
                    "No column(s) found for the following mandatory column definition(s): " + list);
        }
        return definitions;
    }

    private String createListOfMissingColumns(List<ColumnDefinition> remainingDefinitions)
    {
        StringBuilder builder = new StringBuilder();
        for (ColumnDefinition columnDefinition : remainingDefinitions)
        {
            if (columnDefinition.isMandatory())
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(columnDefinition.getName());
            }
        }
        return builder.toString();
    }

    private ColumnDefinition[] findOrderedColumnDefinitions(List<String> columnHeaders)
    {
        ColumnDefinition[] definitions = new ColumnDefinition[columnHeaders.size()];
        for (ColumnDefinition columnDefinition : orderedDefinitions.values())
        {
            boolean mandatory = columnDefinition.isMandatory();
            int orderIndex = columnDefinition.getOrderOrNull() - 1;
            if (orderIndex >= columnHeaders.size())
            {
                if (mandatory)
                {
                    throw new UserFailureException(columnDefinition.getOrderOrNull()
                            + ". column [name=" + columnDefinition.getName()
                            + "] is mandatory but missing because there are only "
                            + columnHeaders.size() + " column headers.");
                }
            } else
            {
                String header = columnHeaders.get(orderIndex);
                Result result = columnDefinition.validateHeader(header);
                if (result.isValid())
                {
                    definitions[orderIndex] = columnDefinition;

                } else if (mandatory)
                {
                    throw new UserFailureException("According to column definition '"
                            + columnDefinition.getName() + "' the header '" + header
                            + "' is invalid because of the following reason: " + result);
                }

            }
        }
        return definitions;
    }

    private ColumnDefinition getDefinition(List<ColumnDefinition> definitions,
            ColumnDefinition orderedColumDefinitionOrNull, List<String> columnHeaders, int i)
    {
        String columnHeader = columnHeaders.get(i);
        StringBuilder builder = new StringBuilder();
        for (Iterator<ColumnDefinition> iterator = definitions.iterator(); iterator.hasNext();)
        {
            ColumnDefinition columnDefinition = iterator.next();
            Result result = columnDefinition.validateHeader(columnHeader);
            if (result.isValid())
            {
                if (columnDefinition.canDefineMultipleColumns() == false)
                {
                    iterator.remove();
                }
                return columnDefinition;
            } else
            {
                addMessage(builder, columnDefinition, result);
            }
        }
        if (orderedColumDefinitionOrNull != null)
        {
            Result result = orderedColumDefinitionOrNull.validateHeader(columnHeader);
            if (result.isValid() == false)
            {
                addMessage(builder, orderedColumDefinitionOrNull, result);
            }
        }
        throw new UserFailureException("No column definition matches the header of the " + (i + 1)
                + ". column: " + columnHeader + builder);
    }

    private void addMessage(StringBuilder builder, ColumnDefinition columnDefinition, Result result)
    {
        builder.append("\nColumn Definition '").append(columnDefinition.getName());
        builder.append("' does not match: Reason: ").append(result);
    }

}
