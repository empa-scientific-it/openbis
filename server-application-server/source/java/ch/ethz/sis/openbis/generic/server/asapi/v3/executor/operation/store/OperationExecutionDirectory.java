/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import java.io.File;

import ch.systemsx.cisd.common.security.MD5ChecksumCalculator;

/**
 * @author pkupczyk
 */
public class OperationExecutionDirectory
{

    private String executionStorePath;

    private String executionCode;

    public OperationExecutionDirectory(String executionStorePath, String executionCode)
    {
        this.executionStorePath = executionStorePath;
        this.executionCode = executionCode;
    }

    public File getFile()
    {
        File storeDir = new File(executionStorePath);
        String checksum = MD5ChecksumCalculator.calculate(executionCode);
        final File dirLevel1 = new File(storeDir, checksum.substring(0, 2));
        final File dirLevel2 = new File(dirLevel1, checksum.substring(2, 4));
        final File dirLevel3 = new File(dirLevel2, checksum.substring(4, 6));
        final File executionDir = new File(dirLevel3, executionCode);
        return executionDir;
    }

    public String getRelativePath()
    {
        File storeDir = new File(executionStorePath);
        File executionDir = getFile();

        // path relative to the store directory
        String relativePath = executionDir.getAbsolutePath().substring(storeDir.getAbsolutePath().length());

        if (relativePath.startsWith(File.separator))
        {
            return relativePath.substring(File.separator.length());
        } else
        {
            return relativePath;
        }
    }

}
