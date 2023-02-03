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

package ch.systemsx.cisd.dbmigration;

/**
 * File type enumeration.
 * 
 * @author Franz-Josef Elmer
 */
public enum MassUploadFileType
{
    CSV(".csv"), TSV(".tsv");

    private final String fileType;

    private MassUploadFileType(String fileType)
    {
        this.fileType = fileType;
    }

    /**
     * Returns the file type string including the '.'.
     */
    public final String getFileType()
    {
        return fileType;
    }

    /**
     * Returns <code>true</code> if the specify file is of this type.
     */
    public boolean isOfType(String fileName)
    {
        return fileName.endsWith(fileType);
    }
}
