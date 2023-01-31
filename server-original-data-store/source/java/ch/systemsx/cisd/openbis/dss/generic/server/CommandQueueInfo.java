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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
public class CommandQueueInfo implements Comparable<CommandQueueInfo>
{
    private String queueName;

    private List<CommandInfo> infos = new ArrayList<>();

    public CommandQueueInfo(String queueName)
    {
        this.queueName = queueName;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public void addInfo(CommandInfo info)
    {
        infos.add(info);
        Collections.sort(infos);
    }

    public List<CommandInfo> getInfos()
    {
        return infos;
    }

    @Override
    public int compareTo(CommandQueueInfo info)
    {
        return queueName.compareTo(info.queueName);
    }

}
