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

package eu.basysbio.cisd.dss;

import static eu.basysbio.cisd.dss.DataColumnHeaderValidator.SEPARATOR;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IOutputStream;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractPostRegistrationDataSetHandlerForFileBasedUndo;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class TimeSeriesDataSetHandler extends AbstractPostRegistrationDataSetHandlerForFileBasedUndo
{
    static final String UPLOADER_EMAIL_KEY = "UPLOADER_EMAIL";

    static final String DATA_SET_TYPE = "TIME_SERIES";

    static final String DATA_FILE_TYPE = ".data.txt";

    private static final Pattern DATA_COLUMN_HEADER_PATTERN =
            Pattern.compile(".*(" + SEPARATOR + ".*)+");

    static final String EXPERIMENT_CODE_TEMPLATE_KEY = "experiment-code-template";
    static final String DEFAULT_EXPERIMENT_CODE_TEMPLATE = "{0}_{1}_{2}";
    
    static final String SAMPLE_CODE_TEMPLATE_KEY = "sample-code-template";
    static final String DEFAULT_SAMPLE_CODE_TEMPLATE = "{0}_{1}_{2}";
    
    static final String SAMPLE_TYPE_CODE_KEY = "sample-type-code";
    static final String DEFAULT_SAMPLE_TYPE_CODE = "TIME_POINT";
    
    static final String TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY = "time-point-data-set-drop-box-path";
    
    static final String TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR_KEY = "time-point-data-set-file-name-separator";
    static final String DEFAULT_TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR = ".";
    
    static final String DATA_SET_PROPERTIES_FILE_NAME_KEY = "data-set-properties-file-name";

    static final String TRANSLATION_KEY = "translation.";
    
    private static final class DataColumnHeader
    {
        private static final int HEADER_PARTS = 12;
        
        private static final int TIME_POINT_INDEX = 3;
        private static final int TIME_POINT_TYPE_INDEX = 4;
        
        private final String experimentCode;
        private final String cultivationMethod;
        private final String biologicalReplicateCode;
        private final int timePoint;
        private final String timePointType;
        private final String technicalReplicateCode;
        private final String celLoc;
        private final String dataSetType;
        private final String valueType;
        private final String scale;
        private final String biID;
        private final String controlledGene;
        private final MessageFormat experimentCodeFormat;
        private final MessageFormat sampleCodeFormat;

        DataColumnHeader(String header, MessageFormat experimentCodeFormat, MessageFormat sampleCodeFormat)
        {
            this.experimentCodeFormat = experimentCodeFormat;
            this.sampleCodeFormat = sampleCodeFormat;
            String[] parts = header.split(SEPARATOR);
            if (parts.length < HEADER_PARTS)
            {
                throw new IllegalArgumentException(HEADER_PARTS
                        + " elements of the following header separated by '" + SEPARATOR
                        + "' expected: " + header);
            }
            experimentCode = parts[0];
            cultivationMethod = parts[1];
            biologicalReplicateCode = parts[2];
            timePoint = parseTimePoint(parts[TIME_POINT_INDEX], header);
            timePointType = parts[TIME_POINT_TYPE_INDEX];
            technicalReplicateCode = parts[5];
            celLoc = parts[6];
            dataSetType = parts[7];
            valueType = parts[8];
            scale = parts[9];
            biID = parts[10];
            controlledGene = parts[11];
        }

        private int parseTimePoint(String value, String header)
        {
            try
            {
                return Util.parseIntegerWithPlusSign(value);
            } catch (NumberFormatException ex)
            {
                throw new UserFailureException((TIME_POINT_INDEX + 1) + ". part [" + value
                        + "] of the following header isn't an integer number: " + header);
            }
        }

        String createExperimentCode()
        {
            return experimentCodeFormat.format(new Object[]
                { experimentCode, cultivationMethod, biologicalReplicateCode });
        }

        String createSampleCode()
        {
            return sampleCodeFormat.format(new Object[]
                { createExperimentCode(), timePointType, Integer.toString(timePoint) });
        }
    }
    
    private final MessageFormat experimentCodeFormat;
    private final MessageFormat sampleCodeFormat;
    private final IEncapsulatedOpenBISService service;
    private final String sampleTypeCode;

    private final File dropBox;

    private final String dataSetPropertiesFileName;

    private final DataSetTypeTranslator translation;

    private final String timePointDataSetFileSeparator;

    TimeSeriesDataSetHandler(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(FileOperations.getInstance());
        this.service = service;
        sampleTypeCode = properties.getProperty(SAMPLE_TYPE_CODE_KEY, DEFAULT_SAMPLE_TYPE_CODE);
        experimentCodeFormat =
                new MessageFormat(properties.getProperty(EXPERIMENT_CODE_TEMPLATE_KEY,
                        DEFAULT_EXPERIMENT_CODE_TEMPLATE));
        sampleCodeFormat =
                new MessageFormat(properties.getProperty(SAMPLE_CODE_TEMPLATE_KEY,
                        DEFAULT_SAMPLE_CODE_TEMPLATE));
        String dropBoxPath =
                PropertyUtils.getMandatoryProperty(properties,
                        TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY);
        dropBox = new File(dropBoxPath);
        if (dropBox.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Drop box for time-point data sets does not exists or isn't a folder: "
                            + dropBox.getAbsolutePath());
        }
        timePointDataSetFileSeparator =
                properties.getProperty(TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR_KEY,
                        DEFAULT_TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR);
        dataSetPropertiesFileName =
                PropertyUtils.getMandatoryProperty(properties, DATA_SET_PROPERTIES_FILE_NAME_KEY);
        translation =
                new DataSetTypeTranslator(ExtendedProperties.getSubset(properties, TRANSLATION_KEY,
                        true));
    }

    public void handle(File originalData, DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier = dataSetInformation.getExperimentIdentifier();
        if (experimentIdentifier == null)
        {
            throw new UserFailureException(
                    "Data set should be registered for an experiment and not for a sample.");
        }
        DataSetType dataSetType = dataSetInformation.getDataSetType();
        if (dataSetType == null || dataSetType.getCode().equals(DATA_SET_TYPE) == false)
        {
            throw new UserFailureException("Data has to be uploaded for data set type "
                    + DATA_SET_TYPE + " instead of " + dataSetType + ".");
        }
        if (originalData.isFile())
        {
            cleaveFileIntoDataSets(originalData, dataSetInformation);
        } else
        {
            File[] tsvFiles = originalData.listFiles(new FilenameFilter()
                {

                    public boolean accept(File dir, String name)
                    {
                        String lowerCaseName = name.toLowerCase();
                        return lowerCaseName.endsWith(".txt") || lowerCaseName.endsWith(".tsv");
                    }
                });
            for (File tsvFile : tsvFiles)
            {
                cleaveFileIntoDataSets(tsvFile, dataSetInformation);
            }
        }
    }

    private void cleaveFileIntoDataSets(File tsvFile, DataSetInformation dataSetInformation)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table = new TabSeparatedValueTable(reader, fileName);
            List<Column> columns = table.getColumns();
            List<Column> commonColumns = new ArrayList<Column>();
            List<Column> dataColumns = new ArrayList<Column>();
            for (Column column : columns)
            {
                String header = column.getHeader();
                if (DATA_COLUMN_HEADER_PATTERN.matcher(header).matches())
                {
                    dataColumns.add(column);
                } else
                {
                    commonColumns.add(column);
                }
            }
            assertExperiment(dataSetInformation, dataColumns);
            for (Column dataColumn : dataColumns)
            {
                createDataSet(commonColumns, dataColumn, dataSetInformation);
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

    private void assertExperiment(DataSetInformation dataSetInformation, List<Column> dataColumns)
    {
        String code = dataSetInformation.getExperimentIdentifier().getExperimentCode();
        Set<String> invalidExperimentCodes = new LinkedHashSet<String>();
        Set<String> experimentCodes = new LinkedHashSet<String>();
        for (Column dataColumn : dataColumns)
        {
            DataColumnHeader dataColumnHeader =
                    new DataColumnHeader(dataColumn.getHeader(), experimentCodeFormat,
                            sampleCodeFormat);
            String experimentCode = dataColumnHeader.createExperimentCode();
            experimentCodes.add(experimentCode);
            if (code.equalsIgnoreCase(experimentCode) == false)
            {
                invalidExperimentCodes.add(experimentCode);
            }
        }
        if (invalidExperimentCodes.isEmpty() == false)
        {
            if (experimentCodes.size() == 1 && invalidExperimentCodes.size() == 1)
            {
                throw new UserFailureException("Data should be uploaded for experiment '"
                        + invalidExperimentCodes.iterator().next() + "' instead of '" + code
                        + "'.");
            } else
            {
                throw new UserFailureException(
                        "Data columns found for more than one experiment: " + experimentCodes);
            }
        }
    }

    private void createDataSet(List<Column> commonColumns, Column dataColumn,
            DataSetInformation dataSetInformation)
    {
        DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader(), experimentCodeFormat, sampleCodeFormat);
        Experiment experiment = getExperiment(dataColumnHeader, dataSetInformation);
        String sampleCode = dataColumnHeader.createSampleCode().toUpperCase();
        createSampleIfNecessary(sampleCode, dataColumnHeader.timePoint, experiment);
        
        String dataSetFolderName =
                sampleCode + timePointDataSetFileSeparator
                        + dataColumnHeader.technicalReplicateCode + timePointDataSetFileSeparator
                        + dataColumnHeader.celLoc + timePointDataSetFileSeparator
                        + dataColumnHeader.dataSetType + timePointDataSetFileSeparator
                        + dataColumnHeader.valueType + timePointDataSetFileSeparator
                        + dataColumnHeader.scale + timePointDataSetFileSeparator
                        + dataColumnHeader.biID + timePointDataSetFileSeparator
                        + dataColumnHeader.controlledGene;
        File dataSetFolder = new File(dropBox, dataSetFolderName);
        boolean success = getFileOperations().mkdirs(dataSetFolder);
        if (success == false)
        {
            HashSet<String> filesInDropBox = new HashSet<String>(Arrays.asList(getFileOperations().list(dropBox)));
            if (filesInDropBox.contains(dataSetFolder.getName()))
            {
                throw new UserFailureException("There exists already a folder '" + dataSetFolder.getAbsolutePath()
                        + "'.");
            } else
            {
                throw new EnvironmentFailureException("Folder '" + dataSetFolder.getAbsolutePath()
                        + "' couldn't be created.");
            }
        }
        addFileForUndo(dataSetFolder);
        String dataFileName = translation.translate(dataColumnHeader.dataSetType) + DATA_FILE_TYPE;
        File dataFile = new File(dataSetFolder, dataFileName);
        List<Column> columns = new ArrayList<Column>(commonColumns);
        columns.add(dataColumn);
        writeAsTSVFile(dataFile, columns);
        writeDataSetProperties(dataSetFolder, dataColumnHeader, dataSetInformation.tryGetUploadingUserEmail());
        File markerFile = new File(dropBox, Constants.IS_FINISHED_PREFIX + dataSetFolderName);
        success = getFileOperations().createNewFile(markerFile);
        if (success == false)
        {
            throw new EnvironmentFailureException("Marker file '" + markerFile.getAbsolutePath()
                    + "' couldn't be created.");
        }
    }

    private void writeDataSetProperties(File dataSetFolder, DataColumnHeader dataColumnHeader, String userEmail)
    {
        File dataSetPropertiesFile = new File(dataSetFolder, dataSetPropertiesFileName);
        TableBuilder builder = new TableBuilder("property", "value");
        if (userEmail != null)
        {
            builder.addRow(UPLOADER_EMAIL_KEY, userEmail);
        }
        builder.addRow("TECHNICAL_REPLICATE_CODE", dataColumnHeader.technicalReplicateCode);
        builder.addRow("CEL_LOC", dataColumnHeader.celLoc);
        builder.addRow("VALUE_TYPE", dataColumnHeader.valueType);
        builder.addRow("SCALE", dataColumnHeader.scale);
        builder.addRow("BI_ID", dataColumnHeader.biID);
        builder.addRow("CG", dataColumnHeader.controlledGene);
        builder.addRow("TIME_SERIES_DATA_SET_TYPE", dataColumnHeader.dataSetType);
        writeAsTSVFile(dataSetPropertiesFile, builder.getColumns());
    }
    
    private void writeAsTSVFile(File tsvFile, List<Column> columns)
    {
        IOutputStream outputStream = getFileOperations().getIOutputStream(tsvFile);
        TSVOutputWriter writer = new TSVOutputWriter(outputStream);
        try
        {
            writer.write(columns);
        } finally
        {
            writer.close();
        }
    }

    private void createSampleIfNecessary(String sampleCode, int timePoint, Experiment experiment)
    {
        ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(experiment.getId()));
        List<Sample> samples = service.listSamples(criteria);
        for (Sample sample : samples)
        {
            if (sample.getCode().equals(sampleCode))
            {
                return;
            }
        }
        NewSample sample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);
        sample.setSampleType(sampleType);
        sample.setExperimentIdentifier(experiment.getIdentifier());
        String groupIdentifier = experiment.getProject().getGroup().getIdentifier();
        sample.setIdentifier(groupIdentifier
                + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + sampleCode);
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("TIME_POINT");
        property.setPropertyType(propertyType);
        property.setValue(Integer.toString(timePoint));
        sample.setProperties(new EntityProperty[] {property});
        service.registerSample(sample);
    }

    private Experiment getExperiment(DataColumnHeader dataColumnHeader,
            DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier = createExperimentIdentifier(dataColumnHeader, dataSetInformation);
        Experiment experiment = service.tryToGetExperiment(experimentIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for experiment identifier " + experimentIdentifier);
        }
        return experiment;
    }

    private ExperimentIdentifier createExperimentIdentifier(DataColumnHeader dataColumnHeader,
            DataSetInformation dataSetInformation)
    {
        String experimentCode = dataColumnHeader.createExperimentCode();
        ExperimentIdentifier experimentIdentifier = dataSetInformation.getExperimentIdentifier();
        if (experimentIdentifier == null)
        {
            throw new UserFailureException(
                    "Data set should be registered for an experiment and not for a sample.");
        }
        return new ExperimentIdentifier(experimentIdentifier, experimentCode);
    }


}
