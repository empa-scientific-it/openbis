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

package ch.systemsx.cisd.etlserver.cifex;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo.DataSetUploadInfoHelper;

/**
 * @author Izabela Adamczyk
 */
public class CifexExtractorHelper
{
    private static final String COMMENT_KEY = "comment";

    public static final String REQUEST_PROPERTIES_FILE = "request.properties";

    private static final String UPLOADING_USER_EMAIL_KEY = "user-email";

    public static DataSetUploadInfo getDataSetUploadInfo(File incomingDataSetPath)
    {
        Properties properties = loadProperties(incomingDataSetPath, REQUEST_PROPERTIES_FILE);
        String comment = properties.getProperty(COMMENT_KEY);
        DataSetUploadInfo info = DataSetUploadInfoHelper.extractFromCifexComment(comment);
        return info;
    }

    public static String getUploadingUserEmail(File incomingDataSetPath)
    {
        Properties properties = loadProperties(incomingDataSetPath, REQUEST_PROPERTIES_FILE);
        return properties.getProperty(UPLOADING_USER_EMAIL_KEY);
    }

    private static Properties loadProperties(File incomingDataSetPath, String fileName)
    {
        File propertiesFile = new File(incomingDataSetPath, fileName);
        if (propertiesFile.isFile())
        {
            return PropertyIOUtils.loadProperties(propertiesFile.getPath());
        } else
        {
            throw new UserFailureException("Request properties file '" + propertiesFile
                    + "' does not exist or is not a 'normal' file.");
        }
    }
}
