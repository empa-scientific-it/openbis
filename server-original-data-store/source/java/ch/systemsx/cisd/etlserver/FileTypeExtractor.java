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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * Implementation of {@link IFileFormatTypeExtractor} which gets the types from the file extension. Some of the types are hard-coded.
 * 
 * @author Piotr Buczek
 */
public class FileTypeExtractor implements IFileFormatTypeExtractor
{
    public static final String FILE_TYPES_NAME = "file-types";

    private static final String TYPE_MAPPING_SEPARATOR = ":";

    private static final String LIST_MAPPING_SEPARATOR = " ";

    @Private
    static final String DEFAULT_TYPE_PROPERTY_KEY = "default-file-type";

    @Private
    static final String DEFAULT_FILE_FORMAT_TYPE = "UNKNOWN";

    @Private
    static final String DIRECTORY_TYPE_PROPERTY_KEY = "directory-file-type";

    @Private
    static final String DIRECTORY_FILE_FORMAT_TYPE = "UNKNOWN";

    private final Map<String/* file extension */, String/* file type */> fileTypeMapping;

    private final String defaultType;

    private final String directoryType;

    public FileTypeExtractor(final Properties properties)
    {
        this.fileTypeMapping = createTypeByFileExtensionMap(properties, FILE_TYPES_NAME);
        this.defaultType =
                normalizeExtension(PropertyUtils.getProperty(properties, DEFAULT_TYPE_PROPERTY_KEY,
                        DEFAULT_FILE_FORMAT_TYPE));
        this.directoryType =
                normalizeExtension(PropertyUtils.getProperty(properties,
                        DIRECTORY_TYPE_PROPERTY_KEY, DIRECTORY_FILE_FORMAT_TYPE));
    }

    public static Map<String, String> createTypeByFileExtensionMap(Properties properties,
            String itemsListPropertyName)
    {
        final String typesMapping = PropertyUtils.getProperty(properties, itemsListPropertyName);
        final Map<String, String> result = new HashMap<String, String>();
        if (StringUtils.isBlank(typesMapping))
        {
            return result;
        }
        String[] mappings =
                PropertyParametersUtil.parseItemisedProperty(typesMapping, FILE_TYPES_NAME);
        for (int i = 0; i < mappings.length; i++)
        {
            String mapping = mappings[i];
            String[] tokens = mapping.split(TYPE_MAPPING_SEPARATOR);

            if (tokens.length != 2 || StringUtils.isBlank(tokens[0])
                    || StringUtils.isBlank(tokens[1]))
            {
                throw ConfigurationFailureException
                        .fromTemplate(
                                "Wrong value of property '%s = %s'. The item '%s' has incorrect format.\n"
                                        + "The value should be a comma separated list of mappings from type to extensions, e.g:\n"
                                        + "type1: file-extension1 file-extension2, type2: file-extension3",
                                FILE_TYPES_NAME, typesMapping, mapping);
            }

            String fileType = tokens[0].toUpperCase();
            String[] fileExtensions = tokens[1].trim().split(LIST_MAPPING_SEPARATOR);
            for (String extension : fileExtensions)
            {
                result.put(normalizeExtension(extension), fileType);
            }
        }
        return result;
    }

    @Override
    public final FileFormatType getFileFormatType(final File incomingDataSetPath)
    {
        String fileType;
        if (incomingDataSetPath.isDirectory())
        {
            fileType = directoryType;
        } else
        {
            String fileExtension = getExtension(incomingDataSetPath);
            fileType = getFileTypeCode(fileExtension);
        }
        return new FileFormatType(fileType);
    }

    private String getFileTypeCode(String fileExtension)
    {
        String fileType = getMappedType(fileExtension);
        if (fileType == null)
        {
            return (defaultType == null) ? fileExtension : defaultType;
        } else
        {
            return fileType;
        }
    }

    protected String getMappedType(String fileExtension)
    {
        return fileTypeMapping.get(fileExtension);
    }

    protected static String getExtension(final File incomingDataSetPath)
    {
        String extension = FilenameUtils.getExtension(incomingDataSetPath.getName());
        return normalizeExtension(extension);
    }

    private static String normalizeExtension(String extension)
    {
        return extension.toUpperCase();
    }
}
