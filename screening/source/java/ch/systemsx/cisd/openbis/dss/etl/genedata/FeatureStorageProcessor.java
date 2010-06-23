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

package ch.systemsx.cisd.openbis.dss.etl.genedata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.FeatureVectorUploader;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Franz-Josef Elmer
 */
public class FeatureStorageProcessor extends AbstractDelegatingStorageProcessor
{
    private static final char DELIMITER = ';';

    public static final String LAYER_PREFIX = "<Layer=";

    private final DataSource dataSource;

    private final IEncapsulatedOpenBISService openBisService;

    // Execution state of this object -- set to null after an execution is finished.
    private IImagingUploadDAO dataAccessObject = null;

    private final class ColumnsBuilder
    {
        private final List<Column> columns = new ArrayList<Column>();

        private final List<String> rowLetters = new ArrayList<String>();

        private TableBuilder tableBuilder;

        private int numberOfColumns;

        public void startNewColumn(String columnName)
        {
            addColumn();
            tableBuilder = new TableBuilder(columnName);
        }

        public void checkColumnsLine(int lineIndex, String line)
        {
            int columnCount = new StringTokenizer(line).countTokens();
            if (numberOfColumns == 0)
            {
                numberOfColumns = columnCount;
            }
            if (numberOfColumns != columnCount)
            {
                throw error(lineIndex, line, "Inconsistent number of columns: Expected "
                        + numberOfColumns + " but was " + columnCount);
            }

        }

        public void addRow(int lineIndex, String line)
        {
            StringTokenizer tokenizer = new StringTokenizer(line);
            int countTokens = tokenizer.countTokens();
            if (countTokens != numberOfColumns + 1)
            {
                throw error(lineIndex, line, "Inconsistent number of columns: Expected "
                        + numberOfColumns + " but was " + (countTokens - 1));
            }
            String rowLetter = tokenizer.nextToken();
            if (rowLetters.contains(rowLetter) == false)
            {
                rowLetters.add(rowLetter);
            }
            while (tokenizer.hasMoreTokens())
            {
                if (tableBuilder != null)
                {
                    tableBuilder.addRow(tokenizer.nextToken());
                }
            }
        }

        public void finish()
        {
            addColumn();
        }

        public int getNumberOfWells()
        {
            return numberOfColumns * rowLetters.size();
        }

        public String getRowLetter(int index)
        {
            return rowLetters.get((index / numberOfColumns) % rowLetters.size());
        }

        public int getColumnNumber(int index)
        {
            return index % numberOfColumns + 1;
        }

        public List<Column> getColumns()
        {
            return columns;
        }

        private void addColumn()
        {
            if (tableBuilder != null)
            {
                columns.addAll(tableBuilder.getColumns());
            }
        }

    }

    public FeatureStorageProcessor(Properties properties)
    {
        super(properties);
        this.dataSource = createDataSource(properties);
        this.openBisService = ServiceProvider.getOpenBISService();
    }

    protected DataSource createDataSource(Properties properties)
    {
        return ServiceProvider.getDataSourceProvider().getDataSource(properties);
    }

    @Override
    public File storeData(DataSetInformation dataSetInformation, ITypeExtractor typeExtractor,
            IMailClient mailClient, File incomingDataSetDirectory, File rootDir)
    {
        File storedDataSet =
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        File originalDir = DefaultStorageProcessor.getOriginalDirectory(storedDataSet);
        final File targetFile = new File(originalDir, incomingDataSetDirectory.getName());
        transform(targetFile, storedDataSet, dataSetInformation);

        return storedDataSet;
    }

    protected void transform(File originalDataSet, File targetFolderForTransformedDataSet,
            DataSetInformation dataSetInformation)
    {
        List<String> lines = FileUtilities.loadToStringList(originalDataSet);
        if (lines.isEmpty())
        {
            throw new UserFailureException("Empty file: " + originalDataSet.getName());
        }
        String barCode = extractBarCode(lines.get(0));
        ColumnsBuilder columnsBuilder = convertLinesIntoColumns(lines);
        String columnsString = convertColumnsToString(barCode, columnsBuilder);
        File originalDirectory =
                DefaultStorageProcessor.getOriginalDirectory(targetFolderForTransformedDataSet);
        File file = new File(originalDirectory, originalDataSet.getName() + ".txt");
        FileUtilities.writeToFile(file, columnsString);

        try
        {
            loadDataSetIntoDatabase(lines, dataSetInformation);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    private void loadDataSetIntoDatabase(List<String> lines, DataSetInformation dataSetInformation)
            throws IOException
    {
        GenedataFormatToCanonicalFeatureVector convertor =
                new GenedataFormatToCanonicalFeatureVector(lines, LAYER_PREFIX);
        ArrayList<CanonicalFeatureVector> fvecs = convertor.convert();

        dataAccessObject = createDAO();
        FeatureVectorUploader uploader =
                new FeatureVectorUploader(dataAccessObject,
                        createScreeningDatasetInfo(dataSetInformation));
        uploader.uploadFeatureVectors(fvecs);
    }

    private ScreeningContainerDatasetInfo createScreeningDatasetInfo(
            DataSetInformation dataSetInformation)
    {
        Sample sampleOrNull = tryFindSampleForDataSet(dataSetInformation);
        return ScreeningContainerDatasetInfo.createBasicScreeningDatasetInfo(dataSetInformation,
                sampleOrNull);
    }

    private Sample tryFindSampleForDataSet(DataSetInformation dataSetInformation)
    {
        Sample sampleOrNull = dataSetInformation.tryToGetSample();
        if (null == sampleOrNull)
        {
            // check the parent data sets for a sample
            List<String> parentDataSetCodes = dataSetInformation.getParentDataSetCodes();
            for (String dataSetCode : parentDataSetCodes)
            {
                ExternalData externalData = openBisService.tryGetDataSetForServer(dataSetCode);
                if (externalData != null && externalData.getSample() != null)
                {
                    sampleOrNull = externalData.getSample();
                    break;
                }
            }
        }
        return sampleOrNull;
    }

    protected IImagingUploadDAO createDAO()
    {
        return QueryTool.getQuery(dataSource, IImagingUploadDAO.class);
    }

    private String convertColumnsToString(String barCode, ColumnsBuilder columnsBuilder)
    {
        List<Column> columns = columnsBuilder.getColumns();
        StringBuilder builder = new StringBuilder();
        builder.append("barcode").append(DELIMITER).append("row").append(DELIMITER).append("col");
        for (Column column : columns)
        {
            builder.append(DELIMITER).append(column.getHeader());
        }
        for (int i = 0, n = columnsBuilder.getNumberOfWells(); i < n; i++)
        {
            builder.append('\n').append(barCode);
            builder.append(DELIMITER).append(columnsBuilder.getRowLetter(i));
            builder.append(DELIMITER).append(columnsBuilder.getColumnNumber(i));
            for (Column column : columns)
            {
                builder.append(DELIMITER).append(column.getValues().get(i));
            }
        }
        String columnsString = builder.toString();
        return columnsString;
    }

    private ColumnsBuilder convertLinesIntoColumns(List<String> lines)
    {
        ColumnsBuilder columnsBuilder = new ColumnsBuilder();
        for (int i = 1; i < lines.size(); i++)
        {
            String line = lines.get(i).trim();
            if (line.length() == 0)
            {
                continue;
            }
            if (line.startsWith(LAYER_PREFIX))
            {
                columnsBuilder.startNewColumn(extractLayer(line, i));
            } else if (line.startsWith("1"))
            {
                columnsBuilder.checkColumnsLine(i, line);
            } else
            {
                columnsBuilder.addRow(i, line);
            }
        }
        columnsBuilder.finish();
        return columnsBuilder;
    }

    @Override
    public void commit()
    {
        super.commit();

        if (null == dataAccessObject)
        {
            return;
        }

        dataAccessObject.commit();
        closeDataAccessObject();
    }

    /**
     * Close the DAO and set it to null to make clear that it is not initialized.
     */
    private void closeDataAccessObject()
    {
        dataAccessObject.close();
        dataAccessObject = null;
    }

    @Override
    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        // Delete the data from the database
        if (null != dataAccessObject)
        {
            dataAccessObject.rollback();
            closeDataAccessObject();
        }

        return super.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
    }

    private String extractBarCode(String firstLine)
    {
        int indexOfEqual = firstLine.indexOf('=');
        if (indexOfEqual < 0)
        {
            throw error(0, firstLine, "Missing '='");
        }
        return firstLine.substring(indexOfEqual + 1).trim();
    }

    private String extractLayer(String line, int lineIndex)
    {
        String layer = line.substring(LAYER_PREFIX.length());
        if (layer.endsWith(">") == false)
        {
            throw error(lineIndex, line, "Missing '>' at the end");
        }
        return layer.substring(0, layer.length() - 1);
    }

    private UserFailureException error(int lineIndex, String line, String reason)
    {
        return new UserFailureException("Error in line " + lineIndex + 1 + ": " + reason + ": "
                + line);
    }

}
