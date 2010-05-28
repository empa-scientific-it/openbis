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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * This oracle knows the extensions of the different types of data files provided by CSB.
 * 
 * @author Piotr Buczek
 */
class DataSetTypeOracle
{
    /**
     * The different kinds of data set types known to the oracle.
     * 
     * @author Piotr Buczek
     */
    static enum DataSetTypeInfo
    {
        GB("gb"), SEQUENCING("ab1"), VERIFICATION(null);

        private final String fileExtension;

        DataSetTypeInfo(String fileExtension)
        {
            this.fileExtension = fileExtension;
        }

        public String tryGetFileExtension()
        {
            return fileExtension;
        }

        public String getDataSetTypeCode()
        {
            return name();
        }
    }

    private static Map<String, DataSetTypeInfo> typeInfoByExtension =
            new HashMap<String, DataSetTypeInfo>();

    static
    {
        typeInfoByExtension.put(DataSetTypeInfo.GB.tryGetFileExtension(), DataSetTypeInfo.GB);
        typeInfoByExtension.put(DataSetTypeInfo.SEQUENCING.tryGetFileExtension(),
                DataSetTypeInfo.SEQUENCING);
    }

    /**
     * Scan the folder and return the metadata file for the folder along with its associated type.
     * <p>
     * Returns type UNKNOWN if the folder cannot be identified and throws an error if the
     * identification is ambiguous.
     */
    public static DataSetTypeInfo extractDataSetTypeInfo(File incomingDataSetPath)
            throws UserFailureException
    {
        if (incomingDataSetPath.isDirectory())
        {
            throw UserFailureException.fromTemplate(
                    "Failed to extract type from directory '%s'. Expected a normal file.",
                    incomingDataSetPath);
        }

        final String fileName = incomingDataSetPath.getName().toLowerCase();
        final String fileExtension = FilenameUtils.getExtension(fileName);

        DataSetTypeInfo result = typeInfoByExtension.get(fileExtension);
        return result == null ? DataSetTypeInfo.VERIFICATION : result;
    }

}
