/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.admin;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Move data sets to another share.
 *
 * @author Franz-Josef Elmer
 */
public class MoveDataSetsCommand extends AbstractCommand
{
    static final class MoveDataSetsCommandArguments extends CommonArguments
    {
        Set<String> getDataSetCodes()
        {
            if (arguments.isEmpty())
            {
                return Collections.emptySet();
            }
            return new TreeSet<String>(arguments.subList(1, arguments.size()));
        }

        String getShareId()
        {
            return arguments.size() < 2 ? "" : arguments.get(0);
        }

        @Override
        protected boolean allAdditionalMandatoryArgumentsPresent()
        {
            return arguments.size() >= 2;
        }
    }

    private MoveDataSetsCommandArguments arguments;

    MoveDataSetsCommand()
    {
        super("move-to");
        arguments = new MoveDataSetsCommandArguments();
    }

    @Override
    protected MoveDataSetsCommandArguments getArguments()
    {
        return arguments;
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<share id> <data set code 1> [<data set code 2> <data set code 3> ...]";
    }

    @Override
    void execute()
    {
        String shareId = arguments.getShareId();
        Set<String> dataSetCodes = arguments.getDataSetCodes();
        for (String dataSetCode : dataSetCodes)
        {
            try
            {
                service.shuffleDataSet(sessionToken, dataSetCode, shareId);
                System.out.println("Data set " + dataSetCode + " successfully moved to share " + shareId + ".");
            } catch (UserFailureException ex)
            {
                System.err.println(ex.getMessage());
            }
        }
    }
}
