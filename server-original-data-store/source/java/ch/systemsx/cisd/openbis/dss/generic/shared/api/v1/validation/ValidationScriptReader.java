/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A helper class that reads validation scripts.
 * 
 * @author Kaloyan Enimanev
 */
public class ValidationScriptReader
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ValidationScriptReader.class);

    /**
     * Reads and appends a collection of Jython validation files into a single interpretable script.
     */
    public static String tryReadValidationScript(String[] scriptPaths)
    {
        if (scriptPaths == null)
        {
            return null;
        }

        StringBuilder concatenatedScripts = new StringBuilder();
        for (String scriptPath : scriptPaths)
        {
            File scriptFile = new File(scriptPath);
            if (false == scriptFile.exists())
            {
                operationLog.warn("Invalid validation script [" + scriptPath
                        + "] specified in the configuration.");
            } else
            {
                concatenatedScripts.append(FileUtilities.loadToString(scriptFile)).append("\n");
            }
        }

        String concatenated = concatenatedScripts.toString();
        if (StringUtils.isBlank(concatenated))
        {
            return StringUtils.EMPTY;
        } else
        {
            return insertStandardImportClauses(concatenated);
        }
    }

    private static String insertStandardImportClauses(String scriptString)
    {
        String standardImports = "from " + ValidationError.class.getCanonicalName() + " import *";
        return standardImports + "\n" + scriptString;
    }

}
