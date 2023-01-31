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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;

/**
 * Command that lists files in the data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandLs extends AbstractDssCommand<DataSetArguments>
{
    private static class CommandLsExecutor extends AbstractDataSetExecutor<DataSetArguments>
    {
        CommandLsExecutor(DataSetArguments arguments, AbstractDssCommand<DataSetArguments> command)
        {
            super(arguments, command);
        }

        @Override
        protected void handle(FileInfoDssDTO[] fileInfos, IDataSetDss dataSet)
        {
            for (FileInfoDssDTO fileInfo : fileInfos)
            {
                StringBuilder sb = new StringBuilder();
                if (fileInfo.isDirectory())
                {
                    sb.append(" \t");
                } else
                {
                    sb.append(fileInfo.getFileSize());
                    sb.append("\t");
                }
                sb.append(fileInfo.getPathInListing());
                System.out.println(sb.toString());
            }
        }

    }

    CommandLs()
    {
        super(new DataSetArguments());
    }

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new CommandLsExecutor(arguments, this).execute(args);
    }

    @Override
    public String getName()
    {
        return "ls";
    }
}
