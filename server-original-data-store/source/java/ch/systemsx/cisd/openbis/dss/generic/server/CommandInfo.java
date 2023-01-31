/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public class CommandInfo implements Comparable<CommandInfo>
{
    private String commandType;

    private String description;

    private List<String> dataSetCodes;

    public CommandInfo(String commandType, String description, List<String> dataSetCodes)
    {
        this.commandType = commandType;
        this.description = description;
        this.dataSetCodes = dataSetCodes;
    }

    public String getCommandType()
    {
        return commandType;
    }

    public String getDescription()
    {
        return description;
    }

    public List<String> getDataSetCodes()
    {
        return dataSetCodes;
    }

    @Override
    public int compareTo(CommandInfo info)
    {
        return commandType.compareTo(info.commandType);
    }

}
