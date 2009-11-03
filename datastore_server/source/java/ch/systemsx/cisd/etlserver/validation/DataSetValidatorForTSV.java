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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.utils.FileScanner;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Validator for data sets containing TAB-separated value (TSV) files.
 *
 * @author Franz-Josef Elmer
 */
class DataSetValidatorForTSV implements IDataSetValidator
{
    private static final String PATH_PATTERNS_KEY = "path-patterns";
    private static final String COLUMNS_KEY = "columns";
    
    private final List<FileScanner> fileScanners;
    private final List<ColumnDefinition> unorderedDefinitions;
    private final Map<Integer, ColumnDefinition> orderedDefinitions;

    DataSetValidatorForTSV(Properties properties)
    {
        fileScanners = new ArrayList<FileScanner>();
        String pathPatterns = properties.getProperty(PATH_PATTERNS_KEY);
        if (pathPatterns != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(pathPatterns, ",");
            while (tokenizer.hasMoreTokens())
            {
                String pathPattern = tokenizer.nextToken().trim();
                fileScanners.add(new FileScanner(pathPattern));
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
    
    public void assertValidDataSet(DataSetType dataSetType, File incomingDataSetFileOrFolder)
    {
        for (FileScanner fileScanner : fileScanners)
        {
            List<File> files = fileScanner.scan(incomingDataSetFileOrFolder);
            for (File file : files)
            {
                assertValidFile(file);
            }
        }
    }
    
    private void assertValidFile(File file)
    {
        if (file.isFile() == false)
        {
            return;
        }
        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            LineIterator lineIterator = IOUtils.lineIterator(reader);
            if (lineIterator.hasNext() == false)
            {
                throw new IOException("Empty file '" + file);
            }
            String[] headers = getRowCells(lineIterator.nextLine());
            ColumnDefinition[] definitions = findColumnDefinitions(headers);
            IValidator[] validators = new IValidator[definitions.length];
            for (int i = 0; i < validators.length; i++)
            {
                validators[i] = definitions[i].createValidator();
            }
            int lineNumber = 1;
            while (lineIterator.hasNext())
            {
                lineNumber++;
                String[] row = getRowCells(lineIterator.nextLine());
                if (row.length > definitions.length)
                {
                    throw new UserFailureException("The row in line " + lineNumber + " has "
                            + row.length + " cells instead of " + definitions.length);
                }
                for (int i = 0; i < row.length; i++)
                {
                    try
                    {
                        validators[i].assertValid(row[i]);
                    } catch (RuntimeException ex)
                    {
                        throw new UserFailureException("The row in line " + lineNumber
                                + " has for the " + (i + 1) + " cell: " + ex.getMessage(), ex);
                    }
                }
            }
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
    
    private ColumnDefinition[] findColumnDefinitions(String[] columnHeaders)
    {
        ColumnDefinition[] definitions = new ColumnDefinition[columnHeaders.length];
        for (ColumnDefinition columnDefinition : orderedDefinitions.values())
        {
            boolean mandatory = columnDefinition.isMandatory();
            int orderIndex = columnDefinition.getOrderOrNull() - 1;
            if (orderIndex >= columnHeaders.length)
            {
                if (mandatory)
                {
                    throw new UserFailureException(columnDefinition.getOrderOrNull()
                            + ". column [name=" + columnDefinition.getName()
                            + "] is mandatory but missing because there are only "
                            + columnHeaders.length + " column headers.");
                }
            }
            columnDefinition.assertValidHeader(columnHeaders[orderIndex]);
            definitions[orderIndex] = columnDefinition;
        }
        List<ColumnDefinition> remainingDefinitions =
                new LinkedList<ColumnDefinition>(unorderedDefinitions);
        for (int i = 0; i < definitions.length; i++)
        {
            if (definitions[i] != null)
            {
                definitions[i] = getDefinition(remainingDefinitions, columnHeaders[i]);
            }
        }
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
        if (builder.length() > 0)
        {
            throw new UserFailureException(
                    "No columns found for the following mandatory column definitions: " + builder);
        }
        return definitions;
    }

    private ColumnDefinition getDefinition(List<ColumnDefinition> definitions, String columnHeader)
    {
        for (Iterator<ColumnDefinition> iterator = definitions.iterator(); iterator.hasNext();)
        {
            ColumnDefinition columnDefinition = iterator.next();
            if (columnDefinition.isValidHeader(columnHeader))
            {
                iterator.remove();
                return columnDefinition;
            }
        }
        throw new UserFailureException("No column definition match the following column header: "
                + columnHeader);
    }

    private String[] getRowCells(String line)
    {
        return StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "\t");
    }

}
