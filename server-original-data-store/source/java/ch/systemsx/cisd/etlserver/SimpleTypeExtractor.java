/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.Properties;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * Implementation of {@link ITypeExtractor} which gets the types from the properties argument of the constructor.
 * 
 * @author Franz-Josef Elmer
 */
public class SimpleTypeExtractor implements ITypeExtractor
{
    public static final String FILE_FORMAT_TYPE_KEY = "file-format-type";

    public static final String LOCATOR_TYPE_KEY = "locator-type";

    public static final String DATA_SET_TYPE_KEY = "data-set-type";

    public static final String PROCESSOR_TYPE_KEY = "processor-type";

    public static final String IS_MEASURED_KEY = "is-measured";

    private FileFormatType fileFormatType;

    private LocatorType locatorType;

    private DataSetType dataSetType;

    private String processorType;

    private boolean measured;

    public SimpleTypeExtractor(final Properties properties)
    {
        String code =
                properties.getProperty(FILE_FORMAT_TYPE_KEY,
                        FileFormatType.DEFAULT_FILE_FORMAT_TYPE_CODE);
        fileFormatType = new FileFormatType(code);
        code = properties.getProperty(LOCATOR_TYPE_KEY, LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
        locatorType = new LocatorType(code);
        code = properties.getProperty(DATA_SET_TYPE_KEY, DataSetTypeCode.UNKNOWN.getCode());
        dataSetType = new DataSetType(code);
        processorType = properties.getProperty(PROCESSOR_TYPE_KEY);
        measured = "true".equals(properties.getProperty(IS_MEASURED_KEY, "true"));

    }

    //
    // IProcedureAndDataTypeExtractor
    //

    @Override
    public final FileFormatType getFileFormatType(final File incomingDataSetPath)
    {
        return fileFormatType;
    }

    @Override
    public final LocatorType getLocatorType(final File incomingDataSetPath)
    {
        return locatorType;
    }

    @Override
    public final DataSetType getDataSetType(final File incomingDataSetPath)
    {
        return dataSetType;
    }

    @Override
    public String getProcessorType(File incomingDataSetPath)
    {
        return processorType;
    }

    @Override
    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return measured;
    }

}
