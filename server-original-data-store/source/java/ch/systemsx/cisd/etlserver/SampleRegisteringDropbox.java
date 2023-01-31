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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalProperties;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalPropertiesLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.BatchSamplesOperation;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.SampleCodeGenerator;

/**
 * Registers samples from provided file. No data sets are created.
 * 
 * @author Izabela Adamczyk
 */
public class SampleRegisteringDropbox implements IDataSetHandler
// WORKAROUND: {@link IDataSetHandler} has been used because it's the
// fastest way to implement required feature (sample batch registration via dropbox). It would
// be better to be able to configure a special dropbox directly in openBIS.
{

    private static final String USER_KEY = "USER";

    private static final String DEFAULT_SPACE_KEY = "DEFAULT_SPACE";

    private static final String SAMPLE_TYPE_KEY = "SAMPLE_TYPE";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SampleRegisteringDropbox.class);

    private final IEncapsulatedOpenBISService service;

    private String logDir;

    private String samplePrefix;

    public SampleRegisteringDropbox(Properties parentProperties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService service)
    {
        this.service = service;
        Properties specificProperties = getSpecificProperties(parentProperties);
        logDir = PropertyUtils.getMandatoryProperty(specificProperties, "error-log-dir");
        samplePrefix = PropertyUtils.getProperty(specificProperties, "sample-code-prefix", "S");
    }

    private static Properties getSpecificProperties(Properties properties)
    {
        return ExtendedProperties.getSubset(properties, IDataSetHandler.DATASET_HANDLER_KEY + '.',
                true);
    }

    @Override
    public List<DataSetInformation> handleDataSet(File file)
    {
        File marker = new File(file.getParent(), createErrorMarkerFileName(file));
        File logFile = new File(new File(logDir), createErrorLogFileName(file));
        try
        {
            if (marker.exists() || file.getName().endsWith(Constants.ERROR_MARKER_FILE))
            {
                return createReturnValue();
            }
            GlobalProperties properties = GlobalPropertiesLoader.load(file);
            String defaultSpaceIdentifierOrNull = tryExtractSpaceIdentifier(properties);
            String userOrNull = properties.tryGet(USER_KEY);
            SampleType sampleType = extractSampleType(properties);
            logGlobalPropertiesExtracted(file, defaultSpaceIdentifierOrNull, userOrNull, sampleType);
            boolean generateCodesAutomatically = defaultSpaceIdentifierOrNull != null;
            SampleCodeGenerator sampleCodeGeneratorOrNull =
                    tryCreateCodeGenrator(generateCodesAutomatically);
            NamedInputStream stream =
                    new NamedInputStream(new FileInputStream(file), file.getName());
            BatchSamplesOperation info =
                    SampleUploadSectionsParser.prepareSamples(sampleType, null, null, Arrays.asList(stream),
                            defaultSpaceIdentifierOrNull, sampleCodeGeneratorOrNull, true, null,
                            BatchOperationKind.REGISTRATION);
            logSamplesExtracted(file, info);
            service.registerSamples(info.getSamples(), userOrNull);
            logSamplesRegistered(file, info);
        } catch (Throwable ex)
        {

            String message = ex.getMessage();
            try
            {
                FileUtils.touch(marker);
                FileUtils.writeStringToFile(logFile, message);
            } catch (IOException logEx)
            {
                operationLog.error(String.format(
                        "Could not write to error log: [%s]. Message: [%s]", logFile.getPath(),
                        message));
            }
            return createReturnValue();
        }
        FileOperations.getMonitoredInstanceForCurrentThread().deleteRecursively(file);
        logFileDeletion(file);
        return createReturnValue();
    }

    private String createErrorLogFileName(File file)
    {
        return file.getName() + "_" + Constants.USER_LOG_FILE;
    }

    private String createErrorMarkerFileName(File file)
    {
        return file.getName() + Constants.ERROR_MARKER_FILE;
    }

    private ArrayList<DataSetInformation> createReturnValue()
    {
        return new ArrayList<DataSetInformation>();
    }

    private void logSamplesRegistered(File file, BatchSamplesOperation info)
    {
        String message =
                String.format("%s samples extracted from file '%s' and registered",
                        info.getCodes().length, file.getName());
        operationLog.info(message);
    }

    private void logFileDeletion(File file)
    {
        String message =
                String.format("Deleting file '%s' after successfull registration of samples",
                        file.getName());
        operationLog.debug(message);
    }

    private void logSamplesExtracted(File file, BatchSamplesOperation info)
    {
        String message =
                String.format("Samples found in file '%s': %s", file.getName(),
                        info.getCodes().length);
        operationLog.debug(message);
    }

    private void logGlobalPropertiesExtracted(File file, String defaultSpaceIdentifierOrNull,
            String userOrNull, SampleType sampleType)
    {
        String message =
                String.format(
                        "Global properties extracted from file '%s': SAMPLE_TYPE(%s) DEFAULT_SPACE(%s) USER(%s)",
                        file.getName(), sampleType, defaultSpaceIdentifierOrNull, userOrNull);
        operationLog.debug(message);
    }

    private SampleType extractSampleType(GlobalProperties properties)
    {
        String sampleTypeCode = properties.get(SAMPLE_TYPE_KEY);
        SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);
        return sampleType;
    }

    private String tryExtractSpaceIdentifier(GlobalProperties properties)
    {
        String spaceCodeOrNull = properties.tryGet(DEFAULT_SPACE_KEY);
        if (spaceCodeOrNull == null)
        {
            return null;
        }
        return new SpaceIdentifier(spaceCodeOrNull)
                .toString();
    }

    private SampleCodeGenerator tryCreateCodeGenrator(boolean generateCodesAutomatically)
    {
        if (generateCodesAutomatically)
        {
            return new SampleCodeGenerator()
                {

                    @Override
                    public List<String> generateCodes(int size)
                    {
                        return service.generateCodes(samplePrefix, EntityKind.SAMPLE, size);
                    }
                };
        } else
        {
            return null;
        }
    }

}
