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

package ch.systemsx.cisd.cina.dss.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;

import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderMetadata;
import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderType;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * CINA uses the data store server to register experiments and samples in addition to data sets. The
 * CinaDataSetInfoExtractor follows the conventions agreed to by the CINA project to determine if
 * the file represents an experiment, sample, or data set and takes according action.
 * <p>
 * In the case of data representing experiments and samples, the extractor may need to register the
 * experiment/sample with openBIS before continuing to process the data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaDataSetInfoExtractor implements IDataSetInfoExtractor
{
    public CinaDataSetInfoExtractor(final Properties globalProperties)
    {

    }

    public DataSetInformation getDataSetInformation(File incomingDataSetFile,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();

        final FolderOracle folderOracle = new FolderOracle();
        FolderMetadata metadata = folderOracle.getFolderMetadataForFolder(incomingDataSetFile);
        FolderType folderType = metadata.getType();

        switch (folderType)
        {
            case DATA_SET:
                processDataSetFolder(metadata.tryGetMarkerFile(), dataSetInformation,
                        openbisService);
                break;
            case EXPERIMENT:
                processExperimentFolder(metadata.tryGetMarkerFile(), dataSetInformation,
                        openbisService);
                break;
            case SAMPLE:
                processSampleFolder(metadata.tryGetMarkerFile(), dataSetInformation,
                        openbisService);
                break;
            case UNKNOWN:
                // Ignore it
                break;
        }

        return dataSetInformation;
    }

    // Not currently used, may be used in the future
    // private static final String NON_EXISTENT_SAMPLE_CODE = "JUNK";
    // /**
    // * Configure the dataset information to be recognized as something to be deleted -- this is
    // * possible by giving it a non-existent sample code
    // */
    // private void configureFolderForDeletion(final DataSetInformation dataSetInformation)
    // {
    // // Configure the data set information to be recognized as something to be deleted -- this is
    // // possible by giving it a non-existent sample code
    // dataSetInformation.setSampleCode(NON_EXISTENT_SAMPLE_CODE);
    // dataSetInformation.setGroupCode("CINA");
    // }

    /**
     * See if the incoming data set folder is contains an experiment and, if so, register the
     * experiment.
     */
    private void processExperimentFolder(final File metadataFile,
            final DataSetInformation dataSetInformation, IEncapsulatedOpenBISService openbisService)
    {
        try
        {
            HashMap<String, String> experimentMetadata = convertMetadataFileToMap(metadataFile);
            ExperimentRegistrationInformationExtractor extractor =
                    new ExperimentRegistrationInformationExtractor(dataSetInformation, experimentMetadata,
                            getEntityCodeSuffix(), openbisService);
            extractor.processMetadataAndFillDataSetInformation();
        } catch (IOException ex)
        {
            UserFailureException userFailure =
                    new UserFailureException("Could not register experiment", ex);
            throw userFailure;
        }
    }

    /**
     * See if the incoming data set folder is contains a sample and, if so, register the sample.
     */
    private void processSampleFolder(final File metadataFile,
            final DataSetInformation dataSetInformation, IEncapsulatedOpenBISService openbisService)
    {
        try
        {
            HashMap<String, String> sampleMetadata = convertMetadataFileToMap(metadataFile);
            SampleRegistrationInformationExtractor extractor =
                    new SampleRegistrationInformationExtractor(dataSetInformation, sampleMetadata,
                            getEntityCodeSuffix(), openbisService);
            extractor.processMetadataAndFillDataSetInformation();
        } catch (IOException ex)
        {
            UserFailureException userFailure =
                    new UserFailureException("Could not register sample", ex);
            throw userFailure;
        }
    }

    /**
     * See if the incoming data set folder is contains a data set and, if so, register the data set.
     */
    private void processDataSetFolder(final File metadataFile,
            final DataSetInformation dataSetInformation, IEncapsulatedOpenBISService openbisService)
    {
    }

    /**
     * Create a suffix for the entity code.
     */
    private String getEntityCodeSuffix()
    {
        Calendar now = GregorianCalendar.getInstance();
        StringBuffer sb = new StringBuffer();
        sb.append(now.get(Calendar.YEAR));
        sb.append(String.format("%02d", now.get(Calendar.MONTH) + 1));
        sb.append(String.format("%02d", now.get(Calendar.DAY_OF_MONTH)));
        sb.append("-");
        sb.append(now.get(Calendar.HOUR_OF_DAY));
        sb.append(now.get(Calendar.MINUTE));
        sb.append("-");
        sb.append(now.get(Calendar.MILLISECOND));
        return sb.toString();
    }

    /**
     * Parse the metadata file and return it as a hashmap. Currently properties files are supported,
     * but CINA may prefer an XML file using the XML schema that LabView supports.
     */
    private HashMap<String, String> convertMetadataFileToMap(File metadataFile) throws IOException,
            FileNotFoundException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(metadataFile));

        HashMap<String, String> map = new HashMap<String, String>();
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        return map;
    }
}
