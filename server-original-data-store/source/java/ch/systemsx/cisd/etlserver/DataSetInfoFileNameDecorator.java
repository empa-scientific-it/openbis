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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Enriches the extracted data set information by adding a dataset property with a file name of the original dataset.
 * <p>
 * The name of the property used for the file name can be specified in {@link #FILE_NAME_PROPERTY_NAME} property (default value is
 * {@link #FILE_NAME_PROPERTY_NAME_DEFAULT_VALUE}).
 * <p>
 * Additionally extension of the file can be removed from the name if {@link #STRIP_EXTENSION} is set to true (default value is false).
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class DataSetInfoFileNameDecorator
{
    /**
     * Name of the property, which holds the property code in the database, at which the original dataset file name should be stored.
     */
    public static final String FILE_NAME_PROPERTY_NAME = "file-name-property-code";

    public static final String FILE_NAME_PROPERTY_NAME_DEFAULT_VALUE = "FILE_NAME";

    /**
     * Name of the property, which says whether extension of the file should be removed.
     */
    public static final String STRIP_EXTENSION = "strip-extension";

    public static final boolean STRIP_EXTENSION_DEFAULT_VALUE = false;

    private final String fileNamePropertyCode;

    private final boolean stripExtension;

    public DataSetInfoFileNameDecorator(Properties localProperties)
    {
        this.fileNamePropertyCode =
                PropertyUtils.getProperty(localProperties, FILE_NAME_PROPERTY_NAME,
                        FILE_NAME_PROPERTY_NAME_DEFAULT_VALUE);
        this.stripExtension = PropertyUtils.getBoolean(localProperties, STRIP_EXTENSION, false);
    }

    public DataSetInformation enrich(DataSetInformation info, File incomingDataSetPath)
    {
        List<NewProperty> properties = info.getDataSetProperties();
        properties.add(createFileNameProperty(incomingDataSetPath));
        info.setDataSetProperties(properties);
        return info;
    }

    private NewProperty createFileNameProperty(File incomingDataSetPath)
    {
        String fileName = incomingDataSetPath.getName();
        if (stripExtension)
        {
            fileName = FilenameUtils.removeExtension(fileName);
        }
        return new NewProperty(fileNamePropertyCode, fileName);
    }
}
