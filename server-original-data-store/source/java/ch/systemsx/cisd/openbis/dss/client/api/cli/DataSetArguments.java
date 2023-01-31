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

package ch.systemsx.cisd.openbis.dss.client.api.cli;

import java.util.List;

import ch.systemsx.cisd.args4j.Option;

/**
 * Command line arguments for dss commands that refer to data sets. The format is:
 * <p>
 * <code>
 * [options] DATA_SET_CODE [PATH]
 * </code>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class DataSetArguments extends GlobalArguments
{
    @Option(name = "r", longName = "recursive", usage = "Recurse into directories")
    private boolean recursive = false;

    public boolean isRecursive()
    {
        return recursive;
    }

    public String getDataSetCode()
    {
        List<String> args = getArguments();
        if (args.size() < 1)
        {
            return "";
        }
        return args.get(0);
    }

    public String getRequestedPath()
    {
        List<String> args = getArguments();
        String path;
        if (args.size() < 2)
        {
            path = "/";
        } else
        {
            path = args.get(1);
        }
        return path;
    }

    @Override
    public boolean allAdditionalMandatoryArgumentsPresent()
    {
        if (getDataSetCode().length() < 1)
        {
            return false;
        }
        return true;
    }
}
